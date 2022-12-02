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
import org.locationtech.jts.algorithm.PolygonNodeTopology;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.MCIndexSegmentSetMutualIntersector;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentSetMutualIntersector;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.noding.SegmentStringUtil;

/**
 * Transforms a polygon with holes into a single self-touching (invalid) ring
 * by joining holes to the exterior shell or to another hole. 
 * The holes are added in order of their envelopes (leftmost/lowest first). 
 * As the resulting shell develops, a hole may be added to what was
 * originally another hole.
 * <p>
 * There is no attempt to optimize the quality of the join lines.
 * In particular, holes may be joined by lines longer than is optimal.
 * However, holes which touch the shell or other holes are connected at the touch point.
 * <p>
 * The class requires the input polygon to have normal orientation
 * (shell CW and rings CCW).
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
  
  private List<Coordinate> shellCoords;
  // a sorted and searchable version of the shellCoords
  private TreeSet<Coordinate> shellCoordsSorted;
  private SegmentSetMutualIntersector polygonIntersector;

  private Polygon inputPolygon;

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
    Polygon polygon = node(inputPolygon);
    //--- copy the input polygon shell coords
    shellCoords = ringCoordinates(polygon.getExteriorRing());
    if (polygon.getNumInteriorRing() != 0) {
      joinHoles(polygon);
    }
    return shellCoords.toArray(new Coordinate[0]);
  }

  private Polygon node(Polygon polygon) {
    if (polygon.getNumInteriorRing() == 0) {
      return polygon;
    }
    //-- force polygon to be fully noded
    //TODO: do this faster!
    return (Polygon) polygon.union(polygon);
  }

  private static List<Coordinate> ringCoordinates(LinearRing ring) {
    Coordinate[] coords = ring.getCoordinates();
    List<Coordinate> coordList = new ArrayList<Coordinate>();
    for (Coordinate p : coords) {
      coordList.add(p);
    }
    return coordList;
  }
  
  private void joinHoles(Polygon polygon) {
    polygonIntersector = createPolygonIntersector(polygon);

    shellCoordsSorted = new TreeSet<Coordinate>();
    shellCoordsSorted.addAll(shellCoords);
    List<LinearRing> orderedHoles = sortHoles(polygon);
    for (int i = 0; i < orderedHoles.size(); i++) {
      joinHole(orderedHoles.get(i));
    }
  }

  /**
   * Joins a single hole to the current shellRing.
   * 
   * 1) Get a list of the leftmost Hole Vertex indices. 
   * 2) Get a list of candidate joining shell vertices. 
   * 3) Get the pair that has the shortest distance between them. 
   * This pair is the endpoints of the cut 
   * 4) The selected ShellVertex may occurs multiple times in
   * shellCoords[], so find the proper one and add the hole after it.
   * 
   * @param hole the hole to join
   */
  private void joinHole(LinearRing hole) {
    final Coordinate[] holeCoords = hole.getCoordinates();
    
    //-- first check if hole is touching
    boolean isTouching = joinTouchingHole(holeCoords);
    if (isTouching)
      return;
    joinNonTouchingHole(holeCoords);
  }
  
  private boolean joinTouchingHole(Coordinate[] holeCoords) {
    //TODO: find fast way to identify touching holes (perhaps during initial noding?)
    int holeTouchIndex = findHoleTouchIndex(holeCoords);
    if (holeTouchIndex < 0)
      return false;
    //-- use a hole segment to find shell join vertex it is interior at
    Coordinate shellJoinPt = holeCoords[holeTouchIndex];
    Coordinate holeSegPt = holeCoords[ prev(holeTouchIndex, holeCoords.length) ];
    
    int shellJoinIndex = findShellJoinIndex(shellJoinPt, holeSegPt);
    addHoleToShell(shellJoinIndex, holeCoords, holeTouchIndex);
    return true;
  }

  private int findHoleTouchIndex(Coordinate[] holeCoords) {
    for (int i = 0; i < holeCoords.length; i++) {
      if (shellCoordsSorted.contains(holeCoords[i])) 
        return i;
    }
    return -1;
  }
  
  private void joinNonTouchingHole(Coordinate[] holeCoords) {
    int holeJoinIndex = findLowestLeftVertexIndex(holeCoords);
    Coordinate holeJoinCoord = holeCoords[holeJoinIndex];
    Coordinate shellJoinCoord = findJoinableShellVertex(holeJoinCoord);
    int shellJoinIndex = findShellJoinIndex(shellJoinCoord, holeJoinCoord);
    addHoleToShell(shellJoinIndex, holeCoords, holeJoinIndex);
  }

  /**
   * Finds a shell vertex that is joinable to the hole join vertex.
   * One must always exist, since the hole join vertex is on the left
   * of the hole, and thus must always have at least one shell vertex visible to it.
   * <p>
   * Note that there is no attempt to optimize the selection of shell vertex 
   * to join to (e.g. by choosing one with shortest distance)
   * 
   * @param holeJoinCoord the hole join vertex
   * @return the shell vertex to join to
   */
  private Coordinate findJoinableShellVertex(Coordinate holeJoinCoord) {
    //-- find highest shell vertex in half-plane left of hole pt
    Coordinate candidate = shellCoordsSorted.higher(holeJoinCoord);
    while (candidate.x == holeJoinCoord.x) {
      candidate = shellCoordsSorted.higher(candidate);
    }
    //-- drop back to last vertex with same X as hole
    candidate = shellCoordsSorted.lower(candidate);
    
    //-- find rightmost joinable shell vertex
    while (intersectsBoundary(holeJoinCoord, candidate)) {
      candidate = shellCoordsSorted.lower(candidate);
      //Assert: candidate is not null, since a joinable candidate always exists 
      if (candidate == null) {
        throw new IllegalStateException("Unable to find joinable shell vertex");
      }
    } 
    return candidate;
  }

  /**
   * Gets the shell vertex index that the hole should be joined after.
   * A shell vertex can occur multiple times, so it is necessary
   * to choose the one which forms a corner having the 
   * join line in the shell interior.
   * 
   * @param shellJoinCoord the shell join vertex
   * @param holeJoinCoord the hole join vertex
   * @return the shell vertex index to join after
   */
  private int findShellJoinIndex(Coordinate shellJoinCoord, Coordinate holeJoinCoord) {
    //-- linear scan is slow but only done once per hole
    for (int i = 0; i < shellCoords.size() - 1; i++) {
      if (shellJoinCoord.equals2D(shellCoords.get(i))) {
        if (isLineInterior(shellCoords, i, holeJoinCoord)) {
          return i;
        }
      }
    }
    throw new IllegalStateException("Unable to find shell join index with interior join line");
  }
  
  private boolean isLineInterior(List<Coordinate> shellCoords, int shellIndex, 
      Coordinate linePt) {
    Coordinate nodePt = shellCoords.get(shellIndex);
    Coordinate shell0 = shellCoords.get( prev(shellIndex, shellCoords.size()) );
    Coordinate shell1 = shellCoords.get( next(shellIndex, shellCoords.size()) );
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
   * @param shellJoinIndex index of join vertex in shell
   * @param holeCoords the vertices of the hole to be inserted
   * @param holeJoinIndex index of join vertex in hole
   */
  private void addHoleToShell(int shellJoinIndex, Coordinate[] holeCoords, int holeJoinIndex) {
    Coordinate shellJoinPt = shellCoords.get(shellJoinIndex);
    Coordinate holeJoinPt = holeCoords[holeJoinIndex];
    
    //-- check for touching (zero-length) join to avoid inserting duplicate vertices
    boolean isVertexTouch = shellJoinPt.equals2D(holeJoinPt);
    Coordinate addShellJoinPt = isVertexTouch ? null : shellJoinPt;

    //-- create new section of vertices to insert in shell
    List<Coordinate> newSection = createHoleSection(holeCoords, holeJoinIndex, addShellJoinPt);
    
    //-- add section after shell join vertex
    int shellAddIndex = shellJoinIndex + 1;
    shellCoords.addAll(shellAddIndex, newSection);
    shellCoordsSorted.addAll(newSection);
  }

  /**
   * Creates the new section of vertices for the added hole.
   * 
   * @param holeCoords
   * @param holeCutIndex
   * @param shellCutPt
   * @return
   */
  private List<Coordinate> createHoleSection(Coordinate[] holeCoords, int holeCutIndex, 
      Coordinate shellCutPt) {
    List<Coordinate> newSection = new ArrayList<Coordinate>();
    
    boolean isHoleDoesNotTouch = shellCutPt != null;
    /**
     * Add all hole vertices, including duplicate at cut vertex
     * Except, if hole DOES touch, cut vertex is already in shell ring
     */
    if (isHoleDoesNotTouch)
      newSection.add(holeCoords[holeCutIndex].copy());
    
    final int holeSize = holeCoords.length - 1;
    
    int index = holeCutIndex;
    for (int i = 0; i < holeSize; i++) {
      index = (index + 1) % holeSize;
      newSection.add(holeCoords[index].copy());
    }
    /**
     * Add duplicate shell vertex at end of the 2nd cut line.
     * Except, if hole DOES touch, cut line is zero-length so does not need end vertex
     */
    if (isHoleDoesNotTouch) { 
      newSection.add(shellCutPt.copy());
    }
    
    return newSection;
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
   * @return true if the line segment interior intersects the polygon boundary
   */
  private boolean intersectsBoundary(Coordinate p0, Coordinate p1) {
    SegmentString segString = new BasicSegmentString(
        new Coordinate[] { p0, p1 }, null);
    List<SegmentString> segStrings = new ArrayList<SegmentString>();
    segStrings.add(segString);
    
    InteriorIntersectionDetector segInt = new InteriorIntersectionDetector();
    polygonIntersector.process(segStrings, segInt);
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
  
  private static SegmentSetMutualIntersector createPolygonIntersector(Polygon polygon) {
    @SuppressWarnings("unchecked")
    List<SegmentString> polySegStrings = SegmentStringUtil.extractSegmentStrings(polygon);
    return new MCIndexSegmentSetMutualIntersector(polySegStrings);
  }
  
  private static class EnvelopeComparator implements Comparator<Geometry> {
    public int compare(Geometry g1, Geometry g2) {
      Envelope e1 = g1.getEnvelopeInternal();
      Envelope e2 = g2.getEnvelopeInternal();
      return e1.compareTo(e2);
    }
  }
      
}
