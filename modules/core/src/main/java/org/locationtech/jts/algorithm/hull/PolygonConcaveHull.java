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
 * Computes concave hulls which respect the boundaries of polygonal geometry.
 * Both outer and inner concave hulls can be produced.
 * Polygons with holes and MultiPolygons are supported. 
 * The algorithm ensures that the generated hulls will not 
 * contain any self-intersections or overlaps and are thus valid.
 * 
 * @author Martin Davis
 *
 */
public class PolygonConcaveHull {
  
  /**
   * Computes a boundary-respecting concave hull of a polygonal geometry.
   * An outer hull is computed if the parameter is positive, 
   * an inner hull is computed if it is negative.
   * 
   * @param geom the polygonal geometry to process
   * @param vertexCountFraction the parameter controlling the detail of the result
   * @return a concave hull geometry
   */
  public static Geometry hull(Geometry geom, double vertexCountFraction) {
    PolygonConcaveHull hull = new PolygonConcaveHull(geom, vertexCountFraction);
    return hull.getResult();
  }

  private Geometry inputGeom;
  private boolean isOuter;
  private double vertexCountFraction;
  private GeometryFactory geomFactory;
  
  /**
   * Creates a new PolygonConcaveHull instance.
   * An outer hull is computed if the parameter is positive, 
   * an inner hull is computed if it is negative.
   * 
   * @param inputGeom the polygonal geometry to process
   * @param vertexCountFraction the fraction of number of vertices to target
   */
  public PolygonConcaveHull(Geometry inputGeom, double vertexCountFraction) {
    this.inputGeom = inputGeom; 
    this.geomFactory = inputGeom.getFactory();
    this.isOuter = vertexCountFraction >= 0;
    this.vertexCountFraction = Math.abs(vertexCountFraction); 
    if (! (inputGeom instanceof Polygonal)) {
      throw new IllegalArgumentException("Input geometry must be polygonal");
    }
  }

  /**
   * Gets the result polygonal concave hull geometry.
   * 
   * @return the polygonal geometry for the concave hull
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
   * @return the concave hull geometry
   */
  private Geometry computeMultiPolygonAll(MultiPolygon multiPoly) {
    RingHullIndex hullIndex = new RingHullIndex();
    int nPoly = multiPoly.getNumGeometries();
    @SuppressWarnings("unchecked")
    List<RingConcaveHull>[] polyHulls = (List<RingConcaveHull>[]) new ArrayList[nPoly];

    for (int i = 0 ; i < multiPoly.getNumGeometries(); i++) {
      Polygon poly = (Polygon) multiPoly.getGeometryN(i);
      List<RingConcaveHull> ringHulls = initPolygon(poly, hullIndex);
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
    List<RingConcaveHull> hulls = initPolygon(poly, hullIndex);
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
  private List<RingConcaveHull> initPolygon(Polygon poly, RingHullIndex hullIndex) {
    List<RingConcaveHull> hulls = new ArrayList<RingConcaveHull>();
    if (poly.isEmpty()) 
      return hulls;
    
    hulls.add( createRingHull( poly.getExteriorRing(), isOuter, hullIndex));
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      //Assert: interior ring is not empty
      hulls.add( createRingHull( poly.getInteriorRingN(i), ! isOuter, hullIndex));
    }
    return hulls;
  }
  
  private RingConcaveHull createRingHull(LinearRing ring, boolean isOuter, RingHullIndex hullIndex) {
    int targetVertexCount = (int) Math.ceil(vertexCountFraction * (ring.getNumPoints() - 1));
    RingConcaveHull ringHull = new RingConcaveHull(ring, isOuter, targetVertexCount);
    if (hullIndex != null) hullIndex.add(ringHull);
    return ringHull;
  }

  private Polygon polygonHull(Polygon poly, List<RingConcaveHull> ringHulls, RingHullIndex hullIndex) {
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
