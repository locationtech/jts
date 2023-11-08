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
package org.locationtech.jts.triangulate.polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.PolygonNodeTopology;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.MCIndexSegmentSetMutualIntersector;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentSetMutualIntersector;
import org.locationtech.jts.noding.SegmentString;

/**
 * Transforms a polygon with holes into a single self-touching (invalid) ring
 * by joining holes to the exterior shell or to another hole
 * with out-and-back line segments. 
 * The holes are added in order of their envelopes (leftmost/lowest first). 
 * As the result shell develops, a hole may be added to what was
 * originally another hole.
 * <p>
 * There is no attempt to optimize the quality of the join lines.
 * In particular, holes may be joined by lines longer than is optimal.
 * However, holes which touch the shell or other holes are joined at the touch point.
 * <p>
 * The class does not require the input polygon to have normal
 * orientation (shell CW and rings CCW).
 * The output ring is always CW.
 */
public class PolygonHoleJoiner {
  
  /**
   * Joins the shell and holes of a polygon 
   * and returns the result as an (invalid) Polygon.
   * 
   * @param inputPolygon the polygon to join
   * @return the result polygon
   */
  public static Polygon joinAsPolygon(Polygon polygon) {
    return polygon.getFactory().createPolygon(join(polygon));
  }
  
  /**
   * Joins the shell and holes of a polygon 
   * and returns the result as sequence of Coordinates.
   * 
   * @param inputPolygon the polygon to join
   * @return the result coordinates
   */
  public static Coordinate[] join(Polygon polygon) {
    PolygonHoleJoiner joiner = new PolygonHoleJoiner(polygon);
    return joiner.compute();
  }
  
  private Polygon inputPolygon;
  //-- normalized, sorted and noded polygon rings
  private Coordinate[] shellRing;
  private Coordinate[][] holeRings;
  
  //-- indicates whether a hole should be testing for touching
  private boolean[] isHoleTouchingHint;
  
  private List<Coordinate> joinedRing;
  // a sorted and searchable version of the joinedRing
  private TreeSet<Coordinate> joinedPts;
  private SegmentSetMutualIntersector boundaryIntersector;

  /**
   * Creates a new hole joiner.
   * 
   * @param polygon the polygon to join
   */
  public PolygonHoleJoiner(Polygon polygon) {
    this.inputPolygon = polygon;
  }

  /**
   * Computes the joined ring.
   * 
   * @return the points in the joined ring
   */
  public Coordinate[] compute() {
    extractOrientedRings(inputPolygon);
    if (holeRings.length > 0) 
      nodeRings();
    joinedRing = copyToList(shellRing);
    if (holeRings.length > 0) 
      joinHoles();
    return CoordinateArrays.toCoordinateArray(joinedRing);
  }
  
  private void extractOrientedRings(Polygon polygon) {
    shellRing = extractOrientedRing(polygon.getExteriorRing(), true);
    List<LinearRing> holes = sortHoles(polygon);
    holeRings = new Coordinate[holes.size()][];
    for (int i = 0; i < holes.size(); i++) {
      holeRings[i] = extractOrientedRing(holes.get(i), false);
    }
  }

  private static Coordinate[] extractOrientedRing(LinearRing ring, boolean isCW) {
    Coordinate[] pts = ring.getCoordinates();
    boolean isRingCW = ! Orientation.isCCW(pts);
    if (isCW == isRingCW)
      return pts;
      //-- reverse a copy of the points
    Coordinate[] ptsRev = pts.clone();
    CoordinateArrays.reverse(ptsRev);
    return ptsRev;
  }

  private void nodeRings() {
    PolygonNoder noder = new PolygonNoder(shellRing, holeRings);
    noder.node();
    if (noder.isShellNoded()) {
      shellRing = noder.getNodedShell();
    }
    for (int i = 0; i < holeRings.length; i++) {
      if (noder.isHoleNoded(i)) {
        holeRings[i] = noder.getNodedHole(i);
      }
    }
    isHoleTouchingHint = noder.getHolesTouching();
  }
  
  private static List<Coordinate> copyToList(Coordinate[] coords) {
    List<Coordinate> coordList = new ArrayList<Coordinate>();
    for (Coordinate p : coords) {
      coordList.add(p.copy());
    }
    return coordList;
  }
  
  private void joinHoles() {
    boundaryIntersector = createBoundaryIntersector(shellRing, holeRings);
    
    joinedPts = new TreeSet<Coordinate>();
    joinedPts.addAll(joinedRing);
    
    for (int i = 0; i < holeRings.length; i++) {
      joinHole(i, holeRings[i]);
    }
  }

  private void joinHole(int index, Coordinate[] holeCoords) {
    //-- check if hole is touching
    if (isHoleTouchingHint[index]) {
      boolean isTouching = joinTouchingHole(holeCoords);
      if (isTouching)
        return;
    }
    joinNonTouchingHole(holeCoords);
  }
  
  /**
   * Joins a hole to the shell only if the hole touches the shell.
   * Otherwise, reports the hole is non-touching.
   * 
   * @param holeCoords the hole to join
   * @return true if the hole was touching, false if not
   */
  private boolean joinTouchingHole(Coordinate[] holeCoords) {
    int holeTouchIndex = findHoleTouchIndex(holeCoords);
    
    //-- hole does not touch
    if (holeTouchIndex < 0)
      return false;
    
    /**
     * Find shell corner which contains the hole,
     * by finding corner which has a hole segment at the join pt in interior
     */
    Coordinate joinPt = holeCoords[holeTouchIndex];
    Coordinate holeSegPt = holeCoords[ prev(holeTouchIndex, holeCoords.length) ];
    
    int joinIndex = findJoinIndex(joinPt, holeSegPt);
    addJoinedHole(joinIndex, holeCoords, holeTouchIndex);
    return true;
  }

  /**
   * Finds the vertex index of a hole where it touches the 
   * current shell (if it does).
   * If a hole does touch, it must touch at a single vertex
   * (otherwise, the polygon is invalid).
   * 
   * @param holeCoords the hole
   * @return the index of the touching vertex, or -1 if no touch
   */
  private int findHoleTouchIndex(Coordinate[] holeCoords) {
    for (int i = 0; i < holeCoords.length; i++) {
      if (joinedPts.contains(holeCoords[i])) 
        return i;
    }
    return -1;
  }
  
  /**
   * Joins a single non-touching hole to the current joined ring.
   * 
   * @param hole the hole to join
   */
  private void joinNonTouchingHole(Coordinate[] holeCoords) {
    int holeJoinIndex = findLowestLeftVertexIndex(holeCoords);
    Coordinate holeJoinCoord = holeCoords[holeJoinIndex];
    Coordinate joinCoord = findJoinableVertex(holeJoinCoord);
    int joinIndex = findJoinIndex(joinCoord, holeJoinCoord);
    addJoinedHole(joinIndex, holeCoords, holeJoinIndex);
  }

  /**
   * Finds a shell vertex that is joinable to the hole join vertex.
   * One must always exist, since the hole join vertex is on the left
   * of the hole, and thus must always have at least one shell vertex visible to it.
   * <p>
   * There is no attempt to optimize the selection of shell vertex 
   * to join to (e.g. by choosing one with shortest distance).
   * 
   * @param holeJoinCoord the hole join vertex
   * @return the shell vertex to join to
   */
  private Coordinate findJoinableVertex(Coordinate holeJoinCoord) {
    //-- find highest shell vertex in half-plane left of hole pt
    Coordinate candidate = joinedPts.higher(holeJoinCoord);
    while (candidate.x == holeJoinCoord.x) {
      candidate = joinedPts.higher(candidate);
    }
    //-- drop back to last vertex with same X as hole
    candidate = joinedPts.lower(candidate);
    
    //-- find rightmost joinable shell vertex
    while (intersectsBoundary(holeJoinCoord, candidate)) {
      candidate = joinedPts.lower(candidate);
      //Assert: candidate is not null, since a joinable candidate always exists 
      if (candidate == null) {
        throw new IllegalStateException("Unable to find joinable vertex");
      }
    } 
    return candidate;
  }

  /**
   * Gets the join ring vertex index that the hole is joined after.
   * A vertex can occur multiple times in the join ring, so it is necessary
   * to choose the one which forms a corner having the 
   * join line in the ring interior.
   * 
   * @param joinCoord the join ring vertex
   * @param holeJoinCoord the hole join vertex
   * @return the join ring vertex index to join after
   */
  private int findJoinIndex(Coordinate joinCoord, Coordinate holeJoinCoord) {
    //-- linear scan is slow but only done once per hole
    for (int i = 0; i < joinedRing.size() - 1; i++) {
      if (joinCoord.equals2D(joinedRing.get(i))) {
        if (isLineInterior(joinedRing, i, holeJoinCoord)) {
          return i;
        }
      }
    }
    throw new IllegalStateException("Unable to find shell join index with interior join line");
  }
  
  /**
   * Tests if a line between a ring corner vertex and a given point
   * is interior to the ring corner.
   * 
   * @param ring a ring of points
   * @param ringIndex the index of a ring vertex
   * @param linePt the point to be joined to the ring
   * @return true if the line to the point is interior to the ring corner
   */
  private boolean isLineInterior(List<Coordinate> ring, int ringIndex, 
      Coordinate linePt) {
    Coordinate nodePt = ring.get(ringIndex);
    Coordinate shell0 = ring.get( prev(ringIndex, ring.size()) );
    Coordinate shell1 = ring.get( next(ringIndex, ring.size()) );
    return PolygonNodeTopology.isInteriorSegment(nodePt, shell0, shell1, linePt);
  }

  private static int prev(int i, int size) {
    int prev = i - 1;
    if (prev < 0)
      return size - 2;
    return prev;
  }

  private static int next(int i, int size) {
    int next = i + 1;
    if (next > size - 2)
      return 0;
    return next;
  }
  
  /**
   * Add hole vertices at proper position in shell vertex list.
   * This code assumes that if hole touches (shell or other hole),
   * it touches at a node.  This requires an initial noding step.
   * In this case, the code avoids duplicating join vertices.
   * 
   * Also adds hole points to ordered coordinates.
   * 
   * @param joinIndex index of join vertex in shell
   * @param holeCoords the vertices of the hole to be inserted
   * @param holeJoinIndex index of join vertex in hole
   */
  private void addJoinedHole(int joinIndex, Coordinate[] holeCoords, int holeJoinIndex) {
    Coordinate joinPt = joinedRing.get(joinIndex);
    Coordinate holeJoinPt = holeCoords[holeJoinIndex];
    
    //-- check for touching (zero-length) join to avoid inserting duplicate vertices
    boolean isVertexTouch = joinPt.equals2D(holeJoinPt);
    Coordinate addJoinPt = isVertexTouch ? null : joinPt;

    //-- create new section of vertices to insert in shell
    List<Coordinate> newSection = createHoleSection(holeCoords, holeJoinIndex, addJoinPt);
    
    //-- add section after shell join vertex
    int addIndex = joinIndex + 1;
    joinedRing.addAll(addIndex, newSection);
    joinedPts.addAll(newSection);
  }

  /**
   * Creates the new section of vertices for ad added hole,
   * including any required vertices from the shell at the join point,
   * and ensuring join vertices are not duplicated.
   * 
   * @param holeCoords the hole vertices
   * @param holeJoinIndex the index of the join vertex
   * @param joinPt the shell join vertex
   * @return a list of new vertices to be added
   */
  private List<Coordinate> createHoleSection(Coordinate[] holeCoords, int holeJoinIndex, 
      Coordinate joinPt) {
    List<Coordinate> section = new ArrayList<Coordinate>();
    
    boolean isNonTouchingHole = joinPt != null;
    /**
     * Add all hole vertices, including duplicate at hole join vertex
     * Except if hole DOES touch, join vertex is already in shell ring
     */
    if (isNonTouchingHole)
      section.add(holeCoords[holeJoinIndex].copy());
    
    final int holeSize = holeCoords.length - 1;
    int index = holeJoinIndex;
    for (int i = 0; i < holeSize; i++) {
      index = (index + 1) % holeSize;
      section.add(holeCoords[index].copy());
    }
    /**
     * Add duplicate shell vertex at end of the return join line.
     * Except if hole DOES touch, join line is zero-length so do not need dup vertex
     */
    if (isNonTouchingHole) { 
      section.add(joinPt.copy());
    }
    
    return section;
  }

  /**
   * Sort the hole rings by minimum X, minimum Y.
   * 
   * @param poly polygon that contains the holes
   * @return a list of sorted hole rings
   */
  private static List<LinearRing> sortHoles(final Polygon poly) {
    List<LinearRing> holes = new ArrayList<LinearRing>();
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      holes.add(poly.getInteriorRingN(i));
    }
    Collections.sort(holes, new EnvelopeComparator());
    return holes;
  }
  
  private static class EnvelopeComparator implements Comparator<Geometry> {
    @Override
    public int compare(Geometry g1, Geometry g2) {
      Envelope e1 = g1.getEnvelopeInternal();
      Envelope e2 = g2.getEnvelopeInternal();
      return e1.compareTo(e2);
    }
  }
  
  private static int findLowestLeftVertexIndex(Coordinate[] coords) {
    Coordinate lowestLeftCoord = null;
    int lowestLeftIndex = -1;
    for (int i = 0; i < coords.length - 1; i++) {
      if (lowestLeftCoord == null || coords[i].compareTo(lowestLeftCoord) < 0) {
        lowestLeftCoord = coords[i];
        lowestLeftIndex = i;
      }
    }
    return lowestLeftIndex;
  }
    
  /**
   * Tests whether the interior of a line segment intersects the polygon boundary.
   * If so, the line is not a valid join line.
   * 
   * @param p0 a segment vertex
   * @param p1 the other segment vertex
   * @return true if the segment interior intersects a polygon boundary segment
   */
  private boolean intersectsBoundary(Coordinate p0, Coordinate p1) {
    SegmentString segString = new BasicSegmentString(
        new Coordinate[] { p0, p1 }, null);
    List<SegmentString> segStrings = new ArrayList<SegmentString>();
    segStrings.add(segString);
    
    InteriorIntersectionDetector segInt = new InteriorIntersectionDetector();
    boundaryIntersector.process(segStrings, segInt);
    return segInt.hasIntersection();
  }
  
  /**
   * Detects if a segment has an interior intersection with another segment. 
   */
  private static class InteriorIntersectionDetector implements SegmentIntersector {

    private LineIntersector li = new RobustLineIntersector();
    private boolean hasIntersection = false;

    public boolean hasIntersection() {
      return hasIntersection;
    }
    
    @Override
    public void processIntersections(SegmentString ss0, int segIndex0, SegmentString ss1, int segIndex1) {
      Coordinate p00 = ss0.getCoordinate(segIndex0);
      Coordinate p01 = ss0.getCoordinate(segIndex0 + 1);
      Coordinate p10 = ss1.getCoordinate(segIndex1);
      Coordinate p11 = ss1.getCoordinate(segIndex1 + 1);
      
      li.computeIntersection(p00, p01, p10, p11);
      if (li.getIntersectionNum() == 0) {
        return;
      }
      else if (li.getIntersectionNum() == 1) {
        if (li.isInteriorIntersection())
          hasIntersection = true;
      }     
      else { // li.getIntersectionNum() >= 2 - must be collinear
        hasIntersection = true;
      }
   }

    @Override
    public boolean isDone() {
      return hasIntersection;
    }
  }
  
  private static SegmentSetMutualIntersector createBoundaryIntersector(Coordinate[] shellRing, Coordinate[][] holeRings) {
    List<SegmentString> polySegStrings = new ArrayList<SegmentString>();
    polySegStrings.add(new BasicSegmentString(shellRing, null));
    for (Coordinate[] hole : holeRings) {
      polySegStrings.add(new BasicSegmentString(hole, null));
    }
    return new MCIndexSegmentSetMutualIntersector(polySegStrings);
  }
      
}
