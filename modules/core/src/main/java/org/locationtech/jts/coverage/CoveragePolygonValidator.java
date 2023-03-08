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
package org.locationtech.jts.coverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.noding.MCIndexSegmentSetMutualIntersector;

/**
 * Validates that a polygon forms a valid polygonal coverage 
 * with the set of polygons surrounding it.  
 * If the polygon is coverage-valid an empty {@link LineString} is returned.
 * Otherwise, the result is a linear geometry containing 
 * the target polygon boundary linework causing the invalidity.
 * <p>
 * A polygon is coverage-valid if:
 * <ol>
 * <li>The polygon interior does not intersect the interior of other polygons.
 * <li>If the polygon boundary intersects another polygon boundary, the vertices
 * and line segments of the intersection match exactly.
 * </ol>
 * <p>
 * The algorithm detects the following coverage errors:
 * <ol>
 * <li>the polygon is a duplicate of another one
 * <li>a polygon boundary segment is collinear with and overlaps an adjacent segment 
 *     but is not equal to it
 * <li>a polygon boundary segment touches an adjacent segment at a non-vertex point
 * <li>a polygon boundary segment crosses into an adjacent polygon
 * <li>a polygon boundary segment is in the interior of an adjacent polygon 
 * </ol>
 * <p>
 * If any of these errors is present, the target polygon
 * does not form a valid coverage with the adjacent polygons.
 * <p>
 * The validity rules does not preclude properly-noded gaps between coverage polygons.
 * However, this class can detect narrow gaps, 
 * by specifying a maximum gap width using {@link #setGapWidth(double)}.
 * Note that this will also identify narrow gaps separating disjoint coverage regions, 
 * and narrow gores.
 * In some situations it may also produce false positives 
 * (i.e. linework identified as part of a gap which is wider than the given width).
 * To fully identify gaps it maybe necessary to use {@link CoverageUnion} and analyze
 * the holes in the result to see if they are acceptable.
 * <p>
 * A polygon may be coverage-valid with respect to 
 * a set of surrounding polygons, but the collection as a whole may not
 * form a clean coverage.  For example, the target polygon boundary may be fully matched
 * by adjacent boundary segments, but the adjacent set contains polygons 
 * which are not coverage-valid relative to other ones in the set.
 * A coverage is valid only if every polygon in the coverage is coverage-valid.
 * Use {@link CoverageValidator} to validate an entire set of polygons.
 * 
 * @see CoverageValidator
 * 
 * @author Martin Davis
 *
 */
public class CoveragePolygonValidator {
  
  /**
   * Validates that a polygon is coverage-valid  against the
   * surrounding polygons in a polygonal coverage.
   *  
   * @param targetPolygon the polygon to validate
   * @param adjPolygons the adjacent polygons
   * @return a linear geometry containing the segments causing invalidity (if any)
   */
  public static Geometry validate(Geometry targetPolygon, Geometry[] adjPolygons) {
    CoveragePolygonValidator v = new CoveragePolygonValidator(targetPolygon, adjPolygons);
    return v.validate();
  }
  
  /**
   * Validates that a polygon is coverage-valid against the
   * surrounding polygons in a polygonal coverage,
   * and forms no gaps narrower than a specified width.
   * <p>
   * The set of surrounding polygons should include all polygons which
   * are within the gap width distance of the target polygon.
   *  
   * @param targetPolygon the polygon to validate
   * @param adjPolygons a collection of the adjacent polygons
   * @param gapWidth the maximum width of invalid gaps
   * @return a linear geometry containing the segments causing invalidity (if any)
   */  
  public static Geometry validate(Geometry targetPolygon, Geometry[] adjPolygons, double gapWidth) {
    CoveragePolygonValidator v = new CoveragePolygonValidator(targetPolygon, adjPolygons);
    v.setGapWidth(gapWidth);
    return v.validate();
  }
  
  private Geometry targetGeom;
  private double gapWidth = 0.0;
  private GeometryFactory geomFactory;
  private IndexedPointInAreaLocator[] adjPolygonLocators;
  private Geometry[] adjGeoms;

  /**
   * Create a new validator.
   * <p>
   * If the gap width is specified, the set of surrounding polygons 
   * should include all polygons which
   * are within the gap width distance of the target polygon.
   * 
   * @param geom the geometry to validate
   * @param adjGeoms the adjacent polygons in the polygonal coverage
   */
  public CoveragePolygonValidator(Geometry geom, Geometry[] adjGeoms) {
    this.targetGeom = geom;
    this.adjGeoms = adjGeoms;
    geomFactory = targetGeom.getFactory();
  }
  
  /**
   * Sets the maximum gap width, if narrow gaps are to be detected.
   * 
   * @param gapWidth the maximum width of gaps to detect
   */
  public void setGapWidth(double gapWidth) {
    this.gapWidth = gapWidth;
  }
  
  /**
   * Validates the coverage polygon against the set of adjacent polygons
   * in the coverage.
   * 
   * @param adjGeoms the surrounding polygons in the coverage
   * @return a linear geometry containing the segments causing invalidity (if any)
   */
  public Geometry validate() {
    List<Polygon> adjPolygons = extractPolygons(adjGeoms);
    adjPolygonLocators = new IndexedPointInAreaLocator[adjPolygons.size()];
    
    if (hasDuplicateGeom(targetGeom, adjPolygons)) {
      //TODO: convert to LineString copies
      return targetGeom.getBoundary();
    }
    
    List<CoverageRing> targetRings = CoverageRing.createRings(targetGeom);
    List<CoverageRing> adjRings = CoverageRing.createRings(adjPolygons);

    /**
     * Mark matching segments first.
     * Matched segments are not considered for further checks. 
     * This improves performance substantially for mostly-valid coverages.
     */
    Envelope targetEnv = targetGeom.getEnvelopeInternal().copy();
    targetEnv.expandBy(gapWidth);
    markMatchedSegments(targetRings, adjRings, targetEnv);

    //-- check if target is fully matched and thus forms a clean coverage 
    //if (CoverageRing.isMatched(targetRings))
    //  return createEmptyResult();
    
    /**
     * Here we know target has at least one unmatched segment.
     * Do further checks to see if any of them are are invalid.
     */
    markInvalidInteractingSegments(targetRings, adjRings, gapWidth);
    markInvalidInteriorSegments(targetRings, adjPolygons);
    
    return createInvalidLines(targetRings);
  }

  private static List<Polygon> extractPolygons(Geometry[] geoms) {
    List<Polygon> polygons = new ArrayList<Polygon>();
    for (Geometry geom : geoms) {
        PolygonExtracter.getPolygons(geom, polygons);
    }
    return polygons;
  }

  private Geometry createEmptyResult() {
    return geomFactory.createLineString();
  }

  /**
   * Check if an adjacent geometry is a duplicate of the target.
   * This situation is not detected by segment alignment checking, 
   * since all segments are matches.

   * @param geom
   * @param adjPolygons 
   * @return
   */
  private boolean hasDuplicateGeom(Geometry geom, List<Polygon> adjPolygons) {
    for (Polygon adjPoly : adjPolygons) {
      if (adjPoly.getEnvelopeInternal().equals(geom.getEnvelopeInternal())) {
        if (adjPoly.equalsTopo(geom))
          return true;
      }
    }
    return false;
  }

  /**
   * Marks matched segments.
   * This improves the efficiency of validity testing, since in valid coverages 
   * all segments (except exterior ones) will be matched, 
   * and hence do not need to be tested further.
   * In fact, the entire target polygon may be marked valid,
   * which allows avoiding some further tests.
   * Segments matched between adjacent polygons are also marked valid, 
   * since this prevents them from being detected as misaligned,
   * if this is being done.
   * 
   * @param targetRings the target rings
   * @param adjRngs the adjacent rings
   * @param targetEnv the tolerance envelope of the target
   */
  private void markMatchedSegments(List<CoverageRing> targetRings,
      List<CoverageRing> adjRngs, Envelope targetEnv) {
    Map<CoverageRingSegment, CoverageRingSegment> segmentMap = new HashMap<CoverageRingSegment, CoverageRingSegment>();
    markMatchedSegments(targetRings, targetEnv, segmentMap);
    markMatchedSegments(adjRngs, targetEnv, segmentMap);
  }
  
  /**
   * Adds ring segments to the segment map, 
   * and detects if they match an existing segment.
   * Matched segments are marked.
   * 
   * @param rings
   * @param envLimit
   * @param segMap
   */
  private void markMatchedSegments(List<CoverageRing> rings, Envelope envLimit, 
      Map<CoverageRingSegment, CoverageRingSegment> segmentMap) {
    for (CoverageRing ring : rings) {
      for (int i = 0; i < ring.size() - 1; i++) {
        Coordinate p0 = ring.getCoordinate(i);
        Coordinate p1 = ring.getCoordinate(i + 1);
        //-- skip segments which lie outside the limit envelope
        if (! envLimit.intersects(p0, p1)) {
          continue;
        }
        //-- if segments match, mark them as matched
        CoverageRingSegment seg = CoverageRingSegment.create(ring, i);
        if (segmentMap.containsKey(seg)) {
          CoverageRingSegment segMatch = segmentMap.get(seg);
          /**
           * Since inputs should be valid, 
           * the segments are assumed to be in different rings.
           */
          segMatch.markMatched();
          seg.markMatched();
        }
        else {
          segmentMap.put(seg, seg);
        }
      }
    }
  }

  /**
   * A LineSegment representing a segment in a ring.
   * The segment is normalized so it can be compared with segments
   * in any orientation.
   * 
   * @author mdavis
   *
   */
  private static class CoverageRingSegment extends LineSegment {
    public static CoverageRingSegment create(CoverageRing ring, int index) {
      Coordinate p0 = ring.getCoordinate(index);
      Coordinate p1 = ring.getCoordinate(index + 1);
      return new CoverageRingSegment(p0, p1, ring, index);
    }
    
    private CoverageRing ring;
    private int index;

    public CoverageRingSegment(Coordinate p0, Coordinate p1, CoverageRing ring, int index) {
      super(p0, p1);
      normalize();
      this.ring = ring;
      this.index = index;
    }
    
    public void markMatched() {
      ring.markMatched(index);
    }
  }
  
  //--------------------------------------------------
  
  
  /**
   * Marks invalid target segments which cross an adjacent ring segment, 
   * lie partially in the interior of an adjacent ring, 
   * or are nearly collinear with an adjacent ring segment up to the distance tolerance
   * 
   * @param targetRings the rings with segments to test
   * @param adjRings the adjacent rings
   * @param distanceTolerance the gap distance tolerance, if any
   */
  private void markInvalidInteractingSegments(List<CoverageRing> targetRings, List<CoverageRing> adjRings,
      double distanceTolerance) {
    InvalidSegmentDetector detector = new InvalidSegmentDetector(distanceTolerance);
    MCIndexSegmentSetMutualIntersector segSetMutInt = new MCIndexSegmentSetMutualIntersector(targetRings, distanceTolerance);
    segSetMutInt.process(adjRings, detector);
  }
  
  /**
   * Marks invalid target segments which are fully interior
   * to an adjacent polygon.
   * 
   * @param targetRings the rings with segments to test
   * @param adjPolygons the adjacent polygons
   */
  private void markInvalidInteriorSegments(List<CoverageRing> targetRings, List<Polygon> adjPolygons) {
    for (CoverageRing ring : targetRings) {
      for (int i = 0; i < ring.size() - 1; i++) {
        //-- skip check for segments with known state. 
        if (ring.isKnown(i))
          continue;
        
        /**
         * Check if vertex is in interior of an adjacent polygon.
         * If so, the segments on either side are in the interior.
         * Mark them invalid, unless they are already matched.
         */
        Coordinate p = ring.getCoordinate(i);
        if (isInteriorVertex(p, adjPolygons)) {
          ring.markInvalid(i);
          //-- previous segment may be interior (but may also be matched)
          int iPrev = i == 0 ? ring.size() - 2 : i-1;
          if (! ring.isKnown(iPrev))
            ring.markInvalid(iPrev);
        }
      }
    }
  }
  
  /**
   * Tests if a coordinate is in the interior of some adjacent polygon.
   * Uses the cached Point-In-Polygon indexed locators, for performance.
   * 
   * @param p the coordinate to test
   * @param adjPolygons the list of polygons
   * @return true if the point is in the interior
   */
  private boolean isInteriorVertex(Coordinate p, List<Polygon> adjPolygons) {
    /**
     * There should not be too many adjacent polygons, 
     * and hopefully not too many segments with unknown status
     * so a linear scan should not be too inefficient
     */
    //TODO: try a spatial index?
    for (int i = 0; i < adjPolygons.size(); i++) {
      Polygon adjPoly = adjPolygons.get(i);
     
      if (polygonContainsPoint(i, adjPoly, p))
        return true;
    }
    return false;
  }

  private boolean polygonContainsPoint(int index, Polygon poly, Coordinate pt) {
    if (! poly.getEnvelopeInternal().intersects(pt))
      return false;
    PointOnGeometryLocator pia = getLocator(index, poly);
    return Location.INTERIOR == pia.locate(pt);
  }

  private PointOnGeometryLocator getLocator(int index, Polygon poly) {
    IndexedPointInAreaLocator loc = adjPolygonLocators[index];
    if (loc == null) {
      loc = new IndexedPointInAreaLocator(poly);
      adjPolygonLocators[index] = loc;
    }
    return loc;
  }

  private Geometry createInvalidLines(List<CoverageRing> rings) {
    List<LineString> lines = new ArrayList<LineString>();
    for (CoverageRing ring : rings) {
      ring.createInvalidLines(geomFactory, lines);
    }
    
    if (lines.size() == 0) {
      return createEmptyResult();
    }
    else if (lines.size() == 1) {
      return lines.get(0);
    }
    return geomFactory.createMultiLineString(GeometryFactory.toLineStringArray(lines));
  }  
  
}
