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
 * by connecting holes to the exterior shell or to another hole. 
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
  
  private static final double EPS = 1.0E-4;
  
  private List<Coordinate> shellCoords;
  // orderedCoords is a copy of shellCoords for sort purposes
  private TreeSet<Coordinate> orderedCoords;
  // Key: starting end of the cut; Value: list of the other end of the cut
  private HashMap<Coordinate, ArrayList<Coordinate>> cutMap;
  private SegmentSetMutualIntersector polygonIntersector;

  private Polygon inputPolygon;

  public PolygonHoleJoiner(Polygon inputPolygon) {
    this.inputPolygon = inputPolygon;
    polygonIntersector = createPolygonIntersector(inputPolygon);
  }

  /**
   * Computes the joined ring.
   * 
   * @return the points in the joined ring
   */
  public Coordinate[] compute() {
    //--- copy the input polygon shell coords
    shellCoords = ringCoordinates(inputPolygon.getExteriorRing());
    if ( inputPolygon.getNumInteriorRing() != 0 ) {
      joinHoles();
    }
    return shellCoords.toArray(new Coordinate[0]);
  }

  private static List<Coordinate> ringCoordinates(LinearRing ring) {
    Coordinate[] coords = ring.getCoordinates();
    List<Coordinate> coordList = new ArrayList<Coordinate>();
    for (Coordinate p : coords) {
      coordList.add(p);
    }
    return coordList;
  }
  
  private void joinHoles() {
    orderedCoords = new TreeSet<Coordinate>();
    orderedCoords.addAll(shellCoords);
    cutMap = new HashMap<Coordinate, ArrayList<Coordinate>>();
    List<LinearRing> orderedHoles = sortHoles(inputPolygon);
    for (int i = 0; i < orderedHoles.size(); i++) {
      joinHole(orderedHoles.get(i));
    }
  }

  /**
   * Joins a single hole to the current shellRing.
   * 
   * @param hole the hole to join
   */
  private void joinHole(LinearRing hole) {
    /**
     * 1) Get a list of HoleVertex Index. 
     * 2) Get a list of ShellVertex. 
     * 3) Get the pair that has the shortest distance between them. 
     * This pair is the endpoints of the cut 
     * 4) The selected ShellVertex may occurs multiple times in
     * shellCoords[], so find the proper one and add the hole after it.
     */
    final Coordinate[] holeCoords = hole.getCoordinates();
    List<Integer> holeLeftVerticesIndex = getLeftMostVertex(hole);
    Coordinate holeCoord = holeCoords[holeLeftVerticesIndex.get(0)];
    List<Coordinate> shellCoordsList = getLeftShellVertex(holeCoord);
    Coordinate shellCoord = shellCoordsList.get(0);
    int shortestHoleVertexIndex = 0;
    //--- pick the shell-hole vertex pair that gives the shortest distance
    if ( Math.abs(shellCoord.x - holeCoord.x) < EPS ) {
      double shortest = Double.MAX_VALUE;
      for (int i = 0; i < holeLeftVerticesIndex.size(); i++) {
        for (int j = 0; j < shellCoordsList.size(); j++) {
          double currLength = Math.abs(shellCoordsList.get(j).y - holeCoords[holeLeftVerticesIndex.get(i)].y);
          if ( currLength < shortest ) {
            shortest = currLength;
            shortestHoleVertexIndex = i;
            shellCoord = shellCoordsList.get(j);
          }
        }
      }
    }
    int shellVertexIndex = getShellCoordIndex(shellCoord,
        holeCoords[holeLeftVerticesIndex.get(shortestHoleVertexIndex)]);
    addHoleToShell(shellVertexIndex, holeCoords, holeLeftVerticesIndex.get(shortestHoleVertexIndex));
  }

  /**
   * Get the ith shellvertex in shellCoords[] that the current should add after
   * 
   * @param shellVertex Coordinate of the shell vertex
   * @param holeVertex  Coordinate of the hole vertex
   * @return the ith shellvertex
   */
  private int getShellCoordIndex(Coordinate shellVertex, Coordinate holeVertex) {
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
      if ( shellCoords.get(i).equals2D(coord, EPS) ) {
        if ( numSkip == 0 )
          return i;
        numSkip--;
      }
    }
    throw new IllegalStateException("Vertex is not in shellcoords");
  }

  /**
   * Gets a list of shell vertices that could be used to join with the hole.
   * This list contains only one item if the chosen vertex does not share the same
   * x value with holeCoord
   * 
   * @param holeCoord the hole coordinates
   * @return a list of candidate join vertices
   */
  private List<Coordinate> getLeftShellVertex(Coordinate holeCoord) {
    ArrayList<Coordinate> list = new ArrayList<Coordinate>();
    Coordinate closest = orderedCoords.higher(holeCoord);
    while (closest.x == holeCoord.x) {
      closest = orderedCoords.higher(closest);
    }
    do {
      closest = orderedCoords.lower(closest);
    } while (!isJoinable(holeCoord, closest) && !closest.equals(orderedCoords.first()));
    list.add(closest);
    if ( closest.x != holeCoord.x )
      return list;
    double chosenX = closest.x;
    list.clear();
    while (chosenX == closest.x) {
      list.add(closest);
      closest = orderedCoords.lower(closest);
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
   * Add hole at proper position in shell coordinate list.
   * Also adds hole points to ordered coordinates.
   * 
   * @param shellVertexIndex
   * @param holeCoords
   * @param holeVertexIndex
   */
  private void addHoleToShell(int shellVertexIndex, Coordinate[] holeCoords, int holeVertexIndex) {
    List<Coordinate> newCoords = new ArrayList<Coordinate>();
    newCoords.add(new Coordinate(shellCoords.get(shellVertexIndex)));
    final int nPts = holeCoords.length - 1;
    int i = holeVertexIndex;
    do {
      newCoords.add(new Coordinate(holeCoords[i]));
      i = (i + 1) % nPts;
    } while (i != holeVertexIndex);
    newCoords.add(new Coordinate(holeCoords[holeVertexIndex]));
    shellCoords.addAll(shellVertexIndex, newCoords);
    orderedCoords.addAll(newCoords);
  }

  /**
   * Sort the holes by minimum X, minimum Y.
   * 
   * @param poly polygon that contains the holes
   * @return a list of ordered hole geometry
   */
  private List<LinearRing> sortHoles(final Polygon poly) {
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
   * @return index of the left most vertex
   */
  private List<Integer> getLeftMostVertex(LinearRing ring) {
    Coordinate[] coords = ring.getCoordinates();
    ArrayList<Integer> list = new ArrayList<Integer>();
    double minX = ring.getEnvelopeInternal().getMinX();
    for (int i = 0; i < coords.length; i++) {
      if ( Math.abs(coords[i].x - minX) < EPS ) {
        list.add(i);
      }
    }
    return list;
  }
    
  private SegmentSetMutualIntersector createPolygonIntersector(Polygon polygon) {
    List<SegmentString> polySegStrings = SegmentStringUtil.extractSegmentStrings(polygon);
    return new MCIndexSegmentSetMutualIntersector(polySegStrings);
  }
  
  /**
   * 
   * @author mdavis
   *
   */
  private static class EnvelopeComparator implements Comparator<Geometry> {
    public int compare(Geometry o1, Geometry o2) {
      Envelope e1 = o1.getEnvelopeInternal();
      Envelope e2 = o2.getEnvelopeInternal();
      return e1.compareTo(e2);
    }
  }
      
}
