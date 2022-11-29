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
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.locationtech.jts.algorithm.PolygonNodeTopology;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.MCIndexSegmentSetMutualIntersector;
import org.locationtech.jts.noding.SegmentIntersectionDetector;
import org.locationtech.jts.noding.SegmentSetMutualIntersector;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.noding.SegmentStringUtil;

/**
 * Transforms a polygon with holes into a single self-touching (invalid) ring
 * by joining holes to the exterior shell or to another hole. 
 * The holes are added from the lowest upwards. 
 * As the resulting shell develops, a hole might be added to what was
 * originally another hole.
 * <p>
 * There is no attempt to optimize the quality of the join lines.
 * In particular, a hole which already touches at a vertex may be
 * joined at a different vertex.
 */
public class PolygonHoleJoiner {
  
  public static Polygon joinAsPolygon(Polygon inputPolygon) {
    return inputPolygon.getFactory().createPolygon(join(inputPolygon));
  }
  
  public static Coordinate[] join(Polygon inputPolygon) {
    PolygonHoleJoiner joiner = new PolygonHoleJoiner(inputPolygon);
    return joiner.compute();
  }
  
  private List<Coordinate> shellCoords;
  // a sorted and searchable version of the shellCoords
  private TreeSet<Coordinate> shellCoordsSorted;
  // Key: starting end of the cut; Value: list of the other end of the cut
  private HashMap<Coordinate, ArrayList<Coordinate>> cutMap;
  private SegmentSetMutualIntersector polygonIntersector;

  private Polygon inputPolygon;

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
    cutMap = new HashMap<Coordinate, ArrayList<Coordinate>>();
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
    
    List<Integer> holeLeftVerticesIndex = findLeftVertices(hole);
    Coordinate holeLeftCoord = holeCoords[holeLeftVerticesIndex.get(0)];
    List<Coordinate> shellJoinCoords = findJoinableShellVertices(holeLeftCoord);
    
    //--- find the shell-hole vertex pair that has the shortest distance
    int holeJoinIndex = 0;
    Coordinate shellJoinCoord = shellJoinCoords.get(0);
    if (shellJoinCoord.x == holeLeftCoord.x) {
      double minJoinLen = Double.MAX_VALUE;
      for (int i = 0; i < holeLeftVerticesIndex.size(); i++) {
        for (int j = 0; j < shellJoinCoords.size(); j++) {
          double currLen = Math.abs(shellJoinCoords.get(j).y - holeCoords[holeLeftVerticesIndex.get(i)].y);
          if ( currLen < minJoinLen ) {
            minJoinLen = currLen;
            holeJoinIndex = holeLeftVerticesIndex.get(i);
            shellJoinCoord = shellJoinCoords.get(j);
          }
        }
      }
    }
    Coordinate holeJoinCoord = holeCoords[holeJoinIndex];
    int shellJoinIndex = findShellJoinIndex(shellJoinCoord, holeJoinCoord);
    addHoleToShell(shellJoinIndex, holeCoords, holeJoinIndex);
  }

  private boolean joinTouchingHole(Coordinate[] holeCoords) {
    //TODO: find fast way to identify touching holes (perhaps during initial noding?)
    int holeTouchIndex = findHoleTouchIndex(holeCoords);
    if (holeTouchIndex < 0)
      return false;
    int shellTouchIndex = findShellTouchIndex(holeCoords, holeTouchIndex);
    addHoleToShell(shellTouchIndex, holeCoords, holeTouchIndex);
    return true;
  }

  private int findShellTouchIndex(Coordinate[] holeCoords, int holeTouchIndex) {
    //-- linear scan is slow but only done once per hole
    Coordinate holeCoord = holeCoords[holeTouchIndex];
    for (int i = 0; i < shellCoords.size(); i++) {
      if (holeCoord.equals2D(shellCoords.get(i))) {
        if (isInterior(shellCoords, i, holeCoords, holeTouchIndex)) {
          return i;
        }
      }
    }
    return -1;
  }

  private boolean isInterior(List<Coordinate> shellCoords, int i, Coordinate[] holeCoords, int holeTouchIndex) {
    Coordinate nodePt = shellCoords.get(i);
    Coordinate shell0 = shellCoords.get( prev(i, shellCoords.size()) );
    Coordinate shell1 = shellCoords.get( next(i, shellCoords.size()) );
    Coordinate hole0 = holeCoords[ prev(holeTouchIndex, holeCoords.length) ];
    return PolygonNodeTopology.isInteriorSegment(nodePt, shell0, shell1, hole0);
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

  private int findHoleTouchIndex(Coordinate[] holeCoords) {
    for (int i = 0; i < holeCoords.length; i++) {
      if (shellCoordsSorted.contains(holeCoords[i])) 
        return i;
    }
    return -1;
  }



  /**
   * Gets the shell vertex index that the hole should join after.
   * 
   * @param shellVertex the shell vertex
   * @param holeVertex  the hole vertex
   * @return the shell vertex index to join after
   */
  private int findShellJoinIndex(Coordinate shellVertex, Coordinate holeVertex) {
    int numSkip = 0;
    ArrayList<Coordinate> newValueList = new ArrayList<Coordinate>();
    newValueList.add(holeVertex);
    if ( cutMap.containsKey(shellVertex) ) {
      for (Coordinate coord : cutMap.get(shellVertex)) {
        if ( coord.y < holeVertex.y ) {
          numSkip++;
        }
      }
      cutMap.get(shellVertex).add(holeVertex);
    } else {
      cutMap.put(shellVertex, newValueList);
    }
    if ( !cutMap.containsKey(holeVertex) ) {
      cutMap.put(holeVertex, new ArrayList<Coordinate>(newValueList));
    }
    return getShellCoordIndexSkip(shellVertex, numSkip);
  }

  /**
   * Find the index of the coordinate in ShellCoords ArrayList,
   * skipping over some number of matches
   * 
   * @param coord
   * @return
   */
  private int getShellCoordIndexSkip(Coordinate coord, int numSkip) {
    for (int i = 0; i < shellCoords.size(); i++) {
      if ( shellCoords.get(i).equals2D(coord) ) {
        if ( numSkip == 0 )
          return i;
        numSkip--;
      }
    }
    throw new IllegalStateException("Vertex is not in shellcoords");
  }

  /**
   * Gets a list of shell vertices that could be used to join a hole
   * (i.e. the joining line does not cross the polygon boundary).
   * 
   * The list contains only one item if the chosen vertex does not have the same
   * X value as holeCoord.
   * Otherwise, the list contains all joinable shell vertices with the same X value.
   * 
   * @param holeCoord the hole coordinates
   * @return a list of candidate join vertices
   */
  private List<Coordinate> findJoinableShellVertices(Coordinate holeCoord) {
    ArrayList<Coordinate> list = new ArrayList<Coordinate>();
    double holeX = holeCoord.x;
    //-- find highest shell vertex in half-plane left of hole pt
    Coordinate closest = shellCoordsSorted.higher(holeCoord);
    while (closest.x == holeX) {
      closest = shellCoordsSorted.higher(closest);
    }
    
    do {
      closest = shellCoordsSorted.lower(closest);
    } while (! isJoinable(holeCoord, closest) && ! closest.equals(shellCoordsSorted.first()));

    list.add(closest);
    if ( closest.x != holeX )
      return list;
    
    while (closest.x == holeX) {
      closest = shellCoordsSorted.lower(closest);
      if ( closest == null )
        return list;
    }
    return list;
  }

  /**
   * Determine if a line segment between a hole vertex
   * and a shell vertex lies inside the input polygon.
   * 
   * @param holeCoord a hole coordinate
   * @param shellCoord a shell coordinate
   * @return true if the line lies inside the polygon
   */
  private boolean isJoinable(Coordinate holeCoord, Coordinate shellCoord) {
    /**
     * Since the line runs between a hole and the shell,
     * it is inside the polygon if it does not cross the polygon boundary.
     */
    boolean isJoinable = ! crossesPolygon(holeCoord, shellCoord);
    /*
    //--- slow code for testing only
    LineString join = geomFact.createLineString(new Coordinate[] { holeCoord, shellCoord });
    boolean isJoinableSlow = inputPolygon.covers(join)
    if (isJoinableSlow != isJoinable) {
      System.out.println(WKTWriter.toLineString(holeCoord, shellCoord));
    }
    //Assert.isTrue(isJoinableSlow == isJoinable);
    */
    return isJoinable;
  }
  
  /**
   * Tests whether a line segment crosses the polygon boundary.
   * 
   * @param p0 a vertex
   * @param p1 a vertex
   * @return true if the line segment crosses the polygon boundary
   */
  private boolean crossesPolygon(Coordinate p0, Coordinate p1) {
    SegmentString segString = new BasicSegmentString(
        new Coordinate[] { p0, p1 }, null);
    List<SegmentString> segStrings = new ArrayList<SegmentString>();
    segStrings.add(segString);
    
    SegmentIntersectionDetector segInt = new SegmentIntersectionDetector();
    segInt.setFindProper(true);
    polygonIntersector.process(segStrings, segInt);
    
    return segInt.hasProperIntersection();
  }
  
  /**
   * Add hole vertices at proper position in shell vertex list.
   * This code assumes that if hole touches (shell or other hole),
   * it touches at a node.  This requires an initial noding step.
   * In this case, the code avoids duplicating join vertices.
   * 
   * Also adds hole points to ordered coordinates.
   * 
   * @param shellCutIndex index of join vertex in shell
   * @param holeCoords the vertices of the hole to be inserted
   * @param holeCutIndex index of join vertex in hole
   */
  private void addHoleToShell(int shellCutIndex, Coordinate[] holeCoords, int holeCutIndex) {
    Coordinate shellCutPt = shellCoords.get(shellCutIndex);
    Coordinate holeCutPt = holeCoords[holeCutIndex];
    
    //-- check for touching (zero-length) join to avoid inserting duplicate vertices
    boolean isVertexTouch = shellCutPt.equals2D(holeCutPt);
    Coordinate addShellCutPt = isVertexTouch ? null : shellCutPt;

    //-- create new section of vertices to insert in shell
    List<Coordinate> newSection = createHoleSection(holeCoords, holeCutIndex, addShellCutPt);
    
    //-- add section after shell join vertex
    int shellAddIndex = shellCutIndex + 1;
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

  /**
   * Gets a list of indices of the leftmost vertices in a ring.
   * 
   * @param geom the hole ring
   * @return indices of the leftmost vertices
   */
  private static List<Integer> findLeftVertices(LinearRing ring) {
    Coordinate[] coords = ring.getCoordinates();
    ArrayList<Integer> leftmostIndex = new ArrayList<Integer>();
    double leftX = ring.getEnvelopeInternal().getMinX();
    for (int i = 0; i < coords.length - 1; i++) {
      if ( coords[i].x == leftX ) {
        leftmostIndex.add(i);
      }
    }
    return leftmostIndex;
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
