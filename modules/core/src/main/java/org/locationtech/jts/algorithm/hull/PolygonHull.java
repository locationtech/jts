/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.hull;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygonal;

/**
 * Computes hulls which respect the boundaries of polygonal geometry.
 * Both outer and inner hulls can be computed.
 * Outer hulls contain the input geometry and are larger in area.
 * Inner hulls are contained by the input geometry and are smaller in area.
 * In both the hull vertices are a subset of the input vertices.
 * The vertices are selected in a way which minimizes the area difference
 * between the hull and the input geometry.
 * Hulls are generally concave if the input is, 
 * except for extremal values of the target parameter.
 * Polygons with holes and MultiPolygons are supported. 
 * <p>
 * The number of vertices in the computed hull is determined by a target parameter.
 * The target criterion is the fraction of the input vertices included in the hull. 
 * A value of 1 produces the original geometry.
 * A fraction of 0 produces the convex hull (for an outer hull) 
 * or a triangle (for an inner hull). 
 * A positive value computes an outer hull, a negative one computes an inner hull.
 * <p>
 * The algorithm ensures that computed hulls do not 
 * contain any self-intersections or overlaps, 
 * so the result polygonal geometry is valid.
 * The result has the same geometric type and structure as the input.
 * 
 * @author Martin Davis
 *
 */
public class PolygonHull {
  
  /**
   * Computes a boundary-respecting hull of a polygonal geometry,
   * with hull shape determined by a target parameter of fractional 
   * vertex count.
   * An outer hull is computed if the parameter is positive, 
   * an inner hull is computed if it is negative.
   * 
   * @param geom the polygonal geometry to process
   * @param vertexCountFraction the target fraction of number of vertices
   * @return the hull geometry
   */
  public static Geometry hull(Geometry geom, double vertexCountFraction) {
    PolygonHull hull = new PolygonHull(geom, vertexCountFraction);
    return hull.getResult();
  }

  private Geometry inputGeom;
  private boolean isOuter;
  private double vertexCountFraction;
  private GeometryFactory geomFactory;
  
  /**
   * Creates a new instance.
   * An outer hull is computed if the parameter is positive, 
   * an inner hull is computed if it is negative.
   * 
   * @param inputGeom the polygonal geometry to process
   * @param vertexCountFraction the fraction of number of vertices to target
   */
  public PolygonHull(Geometry inputGeom, double vertexCountFraction) {
    this.inputGeom = inputGeom; 
    this.geomFactory = inputGeom.getFactory();
    this.isOuter = vertexCountFraction >= 0;
    this.vertexCountFraction = Math.abs(vertexCountFraction); 
    if (! (inputGeom instanceof Polygonal)) {
      throw new IllegalArgumentException("Input geometry must be polygonal");
    }
  }

  /**
   * Gets the result polygonal hull geometry.
   * 
   * @return the polygonal geometry for the hull
   */
  public Geometry getResult() {
    if (inputGeom instanceof MultiPolygon) {
      /**
       * Only outer hulls where there is more than one polygon
       * can potentially overlap.
       * Shell hulls could overlap adjacent shells or holes containing them; 
       * hole hulls could overlap contained shells.
       */
      boolean isOverlapPossible = isOuter && inputGeom.getNumGeometries() > 1;
      if (isOverlapPossible) {
        return computeMultiPolygonAll((MultiPolygon) inputGeom);
      }
      else {
        return computeMultiPolygonEach((MultiPolygon) inputGeom);
      }
    }
    else if (inputGeom instanceof Polygon) {
      return computePolygon((Polygon) inputGeom);
    }
    throw new IllegalArgumentException("Input must be polygonal");
  }

  /**
   * Computes hulls for MultiPolygon elements for 
   * the cases where hulls might overlap.
   * 
   * @param multiPoly the MultiPolygon to process
   * @return the hull geometry
   */
  private Geometry computeMultiPolygonAll(MultiPolygon multiPoly) {
    RingHullIndex hullIndex = new RingHullIndex();
    int nPoly = multiPoly.getNumGeometries();
    @SuppressWarnings("unchecked")
    List<RingHull>[] polyHulls = (List<RingHull>[]) new ArrayList[nPoly];

    for (int i = 0 ; i < multiPoly.getNumGeometries(); i++) {
      Polygon poly = (Polygon) multiPoly.getGeometryN(i);
      List<RingHull> ringHulls = initPolygon(poly, hullIndex);
      polyHulls[i] = ringHulls;
    }
    
    List<Polygon> polys = new ArrayList<Polygon>();
    for (int i = 0 ; i < multiPoly.getNumGeometries(); i++) {
      Polygon poly = (Polygon) multiPoly.getGeometryN(i);
      Polygon hull = polygonHull(poly, polyHulls[i], hullIndex);
      polys.add(hull);
    }
    return geomFactory.createMultiPolygon(GeometryFactory.toPolygonArray(polys));
  }

  private Geometry computeMultiPolygonEach(MultiPolygon multiPoly) {
    List<Polygon> polys = new ArrayList<Polygon>();
    for (int i = 0 ; i < multiPoly.getNumGeometries(); i++) {
      Polygon poly = (Polygon) multiPoly.getGeometryN(i);
      Polygon hull = computePolygon(poly);
      polys.add(hull);
    }
    return geomFactory.createMultiPolygon(GeometryFactory.toPolygonArray(polys));
  }

  private Polygon computePolygon(Polygon poly) {
    RingHullIndex hullIndex = null;
    /**
     * For a single polygon overlaps are only possible for inner hulls
     * and where holes are present.
     */
    boolean isOverlapPossible = ! isOuter && poly.getNumInteriorRing() > 0;
    if (isOverlapPossible) hullIndex = new RingHullIndex();
    List<RingHull> hulls = initPolygon(poly, hullIndex);
    Polygon hull = polygonHull(poly, hulls, hullIndex);
    return hull;
  }

  /**
   * Create all ring hulls for the rings of a polygon, 
   * so that all are in the hull index if required.
   * 
   * @param poly the polygon being processed
   * @param hullIndex the hull index if present, or null
   * @return the list of ring hulls
   */
  private List<RingHull> initPolygon(Polygon poly, RingHullIndex hullIndex) {
    List<RingHull> hulls = new ArrayList<RingHull>();
    if (poly.isEmpty()) 
      return hulls;
    
    hulls.add( createRingHull( poly.getExteriorRing(), isOuter, hullIndex));
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      //Assert: interior ring is not empty
      hulls.add( createRingHull( poly.getInteriorRingN(i), ! isOuter, hullIndex));
    }
    return hulls;
  }
  
  private RingHull createRingHull(LinearRing ring, boolean isOuter, RingHullIndex hullIndex) {
    int targetVertexCount = (int) Math.ceil(vertexCountFraction * (ring.getNumPoints() - 1));
    RingHull ringHull = new RingHull(ring, isOuter, targetVertexCount);
    if (hullIndex != null) hullIndex.add(ringHull);
    return ringHull;
  }

  private Polygon polygonHull(Polygon poly, List<RingHull> ringHulls, RingHullIndex hullIndex) {
    if (poly.isEmpty()) 
      return geomFactory.createPolygon();
    
    int ringIndex = 0;
    LinearRing shellHull = ringHulls.get(ringIndex++).getHull(hullIndex);
    List<LinearRing> holeHulls = new ArrayList<LinearRing>();
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      LinearRing hull = ringHulls.get(ringIndex++).getHull(hullIndex);
      //TODO: handle empty
      holeHulls.add(hull);
    }
    LinearRing[] resultHoles = GeometryFactory.toLinearRingArray(holeHulls);
    return geomFactory.createPolygon(shellHull, resultHoles);
  }
}
