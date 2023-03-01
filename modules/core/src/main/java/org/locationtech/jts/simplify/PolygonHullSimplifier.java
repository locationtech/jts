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
package org.locationtech.jts.simplify;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.math.MathUtil;

/**
 * Computes topology-preserving simplified hulls of polygonal geometry.
 * Both outer and inner hulls can be computed.
 * Outer hulls contain the input geometry and are larger in area.
 * Inner hulls are contained by the input geometry and are smaller in area.
 * In both the hull vertices are a subset of the input vertices.
 * The hull construction attempts to minimize the area difference
 * with the input geometry.
 * Hulls are generally concave if the input is.
 * Computed hulls are topology-preserving: 
 * they do not contain any self-intersections or overlaps, 
 * so the result polygonal geometry is valid.
 * <p>
 * Polygons with holes and MultiPolygons are supported. 
 * The result has the same geometric type and structure as the input.
 * <p>
 * The number of vertices in the computed hull is determined by a target parameter.
 * Two parameters are supported:
 * <ol>
 * <li><b>Vertex Number fraction:</b> the fraction of the input vertices retained in the result.
 * Value 1 produces the original geometry.
 * Smaller values produce less concave results.
 * For outer hulls, value 0 produces the convex hull (with triangles for any holes).
 * For inner hulls, value 0 produces a triangle (if no holes are present).
 * </li>
 * <li><b>Area Delta ratio:</b> the ratio of the change in area to the input area.
 * Value 0 produces the original geometry.
 * Larger values produce less concave results.
 * </li>
 * </ol> 
 * The algorithm ensures that the result does not cause the target parameter 
 * to be exceeded.  This allows computing outer or inner hulls
 * with a small area delta ratio as an effective way of removing 
 * narrow gores and spikes.   
 * 
 * @author Martin Davis
 *
 */
public class PolygonHullSimplifier {
  
  /**
   * Computes a topology-preserving simplified hull of a polygonal geometry,
   * with hull shape determined by a target parameter 
   * specifying the fraction of the input vertices retained in the result.
   * Larger values compute less concave results.
   * A value of 1 produces the convex hull; a value of 0 produces the original geometry.
   * Either outer or inner hulls can be computed.
   * 
   * @param geom the polygonal geometry to process
   * @param isOuter indicates whether to compute an outer or inner hull
   * @param vertexNumFraction the target fraction of number of input vertices in result
   * @return the hull geometry
   */
  public static Geometry hull(Geometry geom, boolean isOuter, double vertexNumFraction) {
    PolygonHullSimplifier hull = new PolygonHullSimplifier(geom, isOuter);
    hull.setVertexNumFraction( Math.abs(vertexNumFraction));
    return hull.getResult();
  }

  /**
   * Computes a topology-preserving simplified hull of a polygonal geometry,
   * with hull shape determined by a target parameter 
   * specifying the ratio of maximum difference in area to original area.
   * Larger values compute less concave results.
   * A value of 0 produces the original geometry.
   * Either outer or inner hulls can be computed.
   * 
   * @param geom the polygonal geometry to process
   * @param isOuter indicates whether to compute an outer or inner hull
   * @param areaDeltaRatio the target ratio of area difference to original area
   * @return the hull geometry
   */
  public static Geometry hullByAreaDelta(Geometry geom, boolean isOuter, double areaDeltaRatio) {
    PolygonHullSimplifier hull = new PolygonHullSimplifier(geom, isOuter);
    hull.setAreaDeltaRatio( Math.abs(areaDeltaRatio));
    return hull.getResult();
  }
  
  private Geometry inputGeom;
  private boolean isOuter;
  private double vertexNumFraction = -1;
  private double areaDeltaRatio = -1;
  private GeometryFactory geomFactory;
  
  /**
   * Creates a new instance
   * to compute a simplified hull of a polygonal geometry.
   * An outer or inner hull is computed 
   * depending on the value of <code>isOuter</code>. 
   * 
   * @param inputGeom the polygonal geometry to process
   * @param isOuter indicates whether to compute an outer or inner hull
   */
  public PolygonHullSimplifier(Geometry inputGeom, boolean isOuter) {
    this.inputGeom = inputGeom; 
    this.geomFactory = inputGeom.getFactory();
    this.isOuter = isOuter;
    if (! (inputGeom instanceof Polygonal)) {
      throw new IllegalArgumentException("Input geometry must be  polygonal");
    }
  }

  /**
   * Sets the target fraction of input vertices
   * which are retained in the result.
   * The value should be in the range [0,1].
   * 
   * @param vertexNumFraction a fraction of the number of input vertices 
   */
  public void setVertexNumFraction(double vertexNumFraction) {
    double frac = MathUtil.clamp(vertexNumFraction, 0, 1);
    this.vertexNumFraction = frac; 
  }
  
  /**
   * Sets the target maximum ratio of the change in area of the result to the input area.
   * The value must be 0 or greater.
   * 
   * @param areaDeltaRatio a ratio of the change in area of the result
   */
  public void setAreaDeltaRatio(double areaDeltaRatio) {
    this.areaDeltaRatio = areaDeltaRatio; 
  }
  
  /**
   * Gets the result polygonal hull geometry.
   * 
   * @return the polygonal geometry for the hull
   */
  public Geometry getResult() {
    //-- handle trivial parameter values
    if (vertexNumFraction == 1 || areaDeltaRatio == 0) {
      return inputGeom.copy();
    }
    
    if (inputGeom instanceof MultiPolygon) {
      /**
       * Only outer hulls where there is more than one polygon
       * can potentially overlap.
       * Shell outer hulls could overlap adjacent shell hulls 
       * or hole hulls surrounding them; 
       * hole outer hulls could overlap contained shell hulls.
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
    throw new IllegalArgumentException("Input geometry must be polygonal");
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

    //TODO: investigate if reordering input elements improves result
    
    //-- prepare element polygon hulls and index
    for (int i = 0 ; i < multiPoly.getNumGeometries(); i++) {
      Polygon poly = (Polygon) multiPoly.getGeometryN(i);
      List<RingHull> ringHulls = initPolygon(poly, hullIndex);
      polyHulls[i] = ringHulls;
    }
    
    //-- compute hull polygons
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
    
    double areaTotal = 0.0;
    if (areaDeltaRatio >= 0) {
      areaTotal = ringArea(poly);
    }
    hulls.add( createRingHull( poly.getExteriorRing(), isOuter, areaTotal, hullIndex));
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      //Assert: interior ring is not empty
      hulls.add( createRingHull( poly.getInteriorRingN(i), ! isOuter, areaTotal, hullIndex));
    }
    return hulls;
  }
  
  private double ringArea(Polygon poly) {
    double area = Area.ofRing( poly.getExteriorRing().getCoordinateSequence());
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      area += Area.ofRing( poly.getInteriorRingN(i).getCoordinateSequence());
    }
    return area;
  }

  private RingHull createRingHull(LinearRing ring, boolean isOuter, double areaTotal, RingHullIndex hullIndex) {
    RingHull ringHull = new RingHull(ring, isOuter);
    if (vertexNumFraction >= 0) {
      int targetVertexCount = (int) Math.ceil(vertexNumFraction * (ring.getNumPoints() - 1));
      ringHull.setMinVertexNum(targetVertexCount);
    }
    else if (areaDeltaRatio >= 0) {
      double ringArea = Area.ofRing(ring.getCoordinateSequence());
      double ringWeight = ringArea / areaTotal;
      double maxAreaDelta = ringWeight * areaDeltaRatio * ringArea;
      ringHull.setMaxAreaDelta(maxAreaDelta);
    }
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
