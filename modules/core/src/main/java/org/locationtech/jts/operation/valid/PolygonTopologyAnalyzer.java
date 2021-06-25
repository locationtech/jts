/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.valid;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.SegmentString;

/**
 * Analyzes the topology of polygonal geometry
 * to determine whether it is valid.
 * <p>
 * Analyzing polygons with inverted rings (shells or exverted holes)
 * is performed if specified.
 * Inverted rings may cause a disconnected interior due to a self-touch;
 * this is reported by {@link #isInteriorDisconnectedBySelfTouch()}.
 * 
 * @author mdavis
 *
 */
class PolygonTopologyAnalyzer {
  
  /**
   * Finds a self-intersection (if any) in a {@link LinearRing}.
   * 
   * @param ring the ring to analyze
   * @return a self-intersection point if one exists, or null
   */
  public static Coordinate findSelfIntersection(LinearRing ring) {
    PolygonTopologyAnalyzer ata = new PolygonTopologyAnalyzer(ring, false);
    if (ata.hasInvalidIntersection())
      return ata.getInvalidLocation();
    return null;
  }
  
  /**
   * Tests whether a segment p0-p1 is inside or outside a ring.
   * <p>
   * Preconditions:
   * <ul>
   * <li>The segment intersects the ring only at the endpoints
   * <li>One, none or both of the segment endpoints may lie on the ring
   * <li>The ring does not self-cross, but it may self-touch
   * </ul>  
   *  
   * @param p0 a segment vertex
   * @param p1 a segment vertex
   * @param ring the ring to test
   * @return true if the segment lies inside the ring
   */
  public static boolean isSegmentInRing(Coordinate p0, Coordinate p1, LinearRing ring) {
    Coordinate[] ringPts = ring.getCoordinates();
    int loc = PointLocation.locateInRing(p0, ringPts);
    if (loc == Location.EXTERIOR) return false;
    if (loc == Location.INTERIOR) return true;
    
    /**
     * The segment point is on the boundary of the ring.
     * Use the topology at the node to check if the segment
     * is inside or outside the ring.
     */
    return isIncidentSegmentInRing(p0, p1, ringPts);
  }
  
  /**
   * Tests whether a touching segment is interior to a ring.
   * <p>
   * Preconditions:
   * <ul>
   * <li>The segment does not intersect the ring other than at the endpoints
   * <li>The segment vertex p0 lies on the ring
   * <li>The ring does not self-cross, but it may self-touch
   * </ul>
   * This works for both shells and holes, but the caller must know
   * the ring role.
   * 
   * @param p0 the first vertex of the segment
   * @param p1 the second vertex of the segment 
   * @param ringPts the points of the ring
   * @return true if the segment is inside the ring.
   */
  public static boolean isIncidentSegmentInRing(Coordinate p0, Coordinate p1, Coordinate[] ringPts) {
    int index = intersectingSegIndex(ringPts, p0);
    if (index < 0) {
      throw new IllegalArgumentException("Segment vertex does not intersect ring");
    }
    Coordinate rPrev = ringPts[index];
    Coordinate rNext = ringPts[index + 1];
    if (p0.equals2D(ringPts[index])) {
      rPrev = ringPts[ringIndexPrev(ringPts, index)];
    }
    /**
     * If ring orientation is not normalized, flip the corner orientation
     */
    boolean isInteriorOnRight = ! Orientation.isCCW(ringPts);
    if (! isInteriorOnRight) {
      Coordinate temp = rPrev;
      rPrev = rNext;
      rNext = temp;
    }
    return PolygonNode.isInteriorSegment(p0, rPrev, rNext, p1);
  }
  
  /**
   * Computes the index of the segment which intersects a given point.
   * @param ringPts the ring points
   * @param pt the intersection point
   * @return the intersection segment index, or -1 if no intersection is found
   */
  private static int intersectingSegIndex(Coordinate[] ringPts, Coordinate pt) {
    LineIntersector li = new RobustLineIntersector();
    for (int i = 0; i < ringPts.length - 1; i++) {
      li.computeIntersection(pt, ringPts[i], ringPts[i + 1]);
      if (li.hasIntersection()) {
        //-- check if pt is the start point of the next segment
        if (pt.equals2D(ringPts[i + 1])) {
          return i + 1;
        }
        return i;
      }
    }
    return -1;
  }

  private static int ringIndexPrev(Coordinate[] ringPts, int index) {
    int iPrev = index - 1;
    if (index == 0) iPrev = ringPts.length - 2;
    return iPrev;
  }
  
  private boolean isInvertedRingValid;
  
  private PolygonIntersectionAnalyzer intFinder;
  private List<PolygonRing> polyRings = null;
  private Coordinate disconnectionPt = null;

  /**
   * Creates a new analyzer for a {@link Polygon} or {@link MultiPolygon}.
   * 
   * @param geom a Polygon or MultiPolygon
   * @param isInvertedRingValid a flag indicating whether inverted rings are allowed
   */
  public PolygonTopologyAnalyzer(Geometry geom, boolean isInvertedRingValid) {
    this.isInvertedRingValid = isInvertedRingValid;
    analyze(geom);
  }

  public boolean hasInvalidIntersection() {
    return intFinder.isInvalid();
  }

  public int getInvalidCode() {
    return intFinder.getInvalidCode();
  }
  
  public Coordinate getInvalidLocation() {
    return intFinder.getInvalidLocation();
  }
  
  /**
   * Tests whether the interior of the polygonal geometry is
   * disconnected.
   * If true, the disconnection location is available from 
   * {@link #getDisconnectionLocation()}.
   * 
   * @return true if the interior is disconnected
   */
  public boolean isInteriorDisconnected() {
    /**
     * May already be set by a double-touching hole
     */
    if (disconnectionPt != null) {
      return true;
    }
    if (isInvertedRingValid) {
      checkInteriorDisconnectedBySelfTouch();
      if (disconnectionPt != null) {
        return true;
      }
    }
    checkInteriorDisconnectedByHoleCycle();
    if (disconnectionPt != null) {
      return true;
    }
    return false;
  }

  /**
   * Gets a location where the polyonal interior is disconnected.
   * {@link #isInteriorDisconnected()} must be called first.
   * 
   * @return the location of an interior disconnection, or null
   */
  public Coordinate getDisconnectionLocation() {
    return disconnectionPt;
  } 
  
  /**
   * Tests whether any polygon with holes has a disconnected interior
   * by virtue of the holes (and possibly shell) forming a hole cycle.
   * <p>
   * This is a global check, which relies on determining
   * the touching graph of all holes in a polygon.
   * <p>
   * If inverted rings disconnect the interior
   * via a self-touch, this is checked by the {@link PolygonIntersectionAnalyzer}.
   * If inverted rings are part of a hole cycle
   * this is detected here as well.  
   */
  public void checkInteriorDisconnectedByHoleCycle() {
    /**
     * PolyRings will be null for empty, no hole or LinearRing inputs
     */
    if (polyRings != null) {
      disconnectionPt = PolygonRing.findHoleCycleLocation(polyRings);
    }
  }
  
  /**
   * Tests if an area interior is disconnected by a self-touching ring.
   * This must be evaluated after other self-intersections have been analyzed
   * and determined to not exist, since the logic relies on 
   * the rings not self-crossing (winding).
   * <p>
   * If self-touching rings are not allowed, 
   * then the self-touch will previously trigger a self-intersection error.
   */
  public void checkInteriorDisconnectedBySelfTouch() {
    if (polyRings != null) {
      disconnectionPt = PolygonRing.findInteriorSelfNode(polyRings);
    }
  }
  
  private void analyze(Geometry geom) {
    if (geom.isEmpty()) 
      return;
    List<SegmentString> segStrings = createSegmentStrings(geom, isInvertedRingValid);
    polyRings = getPolygonRings(segStrings);
    intFinder = analyzeIntersections(segStrings);
    
    if (intFinder.hasDoubleTouch()) {
      disconnectionPt = intFinder.getDoubleTouchLocation();
      return;
    }
  }
  
  private PolygonIntersectionAnalyzer analyzeIntersections(List<SegmentString> segStrings)
  {
    PolygonIntersectionAnalyzer segInt = new PolygonIntersectionAnalyzer(isInvertedRingValid);
    MCIndexNoder noder = new MCIndexNoder();
    noder.setSegmentIntersector(segInt);
    noder.computeNodes(segStrings);
    return segInt;
  }

  private static List<SegmentString> createSegmentStrings(Geometry geom, boolean isInvertedRingValid) {
    List<SegmentString> segStrings = new ArrayList<SegmentString>();
    if (geom instanceof LinearRing) {
      LinearRing ring = (LinearRing) geom;
      segStrings.add( createSegString(ring, null));
      return segStrings;
    }
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Polygon poly = (Polygon) geom.getGeometryN(i);
      if (poly.isEmpty()) continue;
      boolean hasHoles = poly.getNumInteriorRing() > 0;
      
      //--- polygons with no holes do not need connected interior analysis
      PolygonRing shellRing = null;
      if (hasHoles || isInvertedRingValid) {
        shellRing = new PolygonRing(poly.getExteriorRing());
      }
      segStrings.add( createSegString(poly.getExteriorRing(), shellRing));
      
      for (int j = 0 ; j < poly.getNumInteriorRing(); j++) {
        LinearRing hole = poly.getInteriorRingN(j);
        if (hole.isEmpty()) continue;
        PolygonRing holeRing = new PolygonRing(hole, j, shellRing);
        segStrings.add( createSegString(hole, holeRing));
      }
    }
    return segStrings;
  }
  
  private static List<PolygonRing> getPolygonRings(List<SegmentString> segStrings) {
    List<PolygonRing> polyRings = null;
    for (SegmentString ss : segStrings) {
      PolygonRing polyRing = (PolygonRing) ss.getData();
      if (polyRing != null) {
        if (polyRings == null) {
          polyRings = new ArrayList<PolygonRing>();
        }
        polyRings.add(polyRing);
      }
    }
    return polyRings;
  }

  private static SegmentString createSegString(LinearRing ring, PolygonRing polyRing) {
    Coordinate[] pts = ring.getCoordinates();
    
    //--- repeated points must be removed for accurate intersection detection
    if (CoordinateArrays.hasRepeatedPoints(pts)) {
      pts = CoordinateArrays.removeRepeatedPoints(pts);
    }
    
    SegmentString ss = new BasicSegmentString(pts, polyRing);
    return ss;
  }

}
