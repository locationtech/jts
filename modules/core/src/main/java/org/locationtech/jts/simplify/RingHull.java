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

import java.util.List;
import java.util.PriorityQueue;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.index.VertexSequencePackedRtree;

/**
 * Computes the outer or inner hull of a ring.
 * 
 * @author Martin Davis
 *
 */
class RingHull {
  
  private LinearRing inputRing;
  private int targetVertexNum = -1;
  private double targetAreaDelta = -1;

  /**
   * The ring vertices are oriented so that
   * for corners which are to be kept 
   * the vertices forming the corner are in CW orientation.
   */
  private LinkedRing vertexRing;
  private double areaDelta = 0;
  
  /**
   * Indexing vertices improves corner intersection testing performance.
   * The ring vertices are contiguous, so are suitable for a
   * {@link VertexSequencePackedRtree}.
   */
  private VertexSequencePackedRtree vertexIndex;

  private PriorityQueue<Corner> cornerQueue;

  /**
   * Creates a new instance.
   * 
   * @param ring the ring vertices to process
   * @param isOuter whether the hull is outer or inner
   */
  public RingHull(LinearRing ring, boolean isOuter) {
    this.inputRing = ring; 
    init(ring.getCoordinates(), isOuter);
  }
  
  public void setMinVertexNum(int minVertexNum) {
    targetVertexNum = minVertexNum;
  }
  
  public void setMaxAreaDelta(double maxAreaDelta) {
    targetAreaDelta = maxAreaDelta;
  }
  
  public Envelope getEnvelope() {
    return inputRing.getEnvelopeInternal();
  }
  
  public VertexSequencePackedRtree getVertexIndex() {
    return vertexIndex;
  }
  
  public LinearRing getHull(RingHullIndex hullIndex) {
    compute(hullIndex);
    Coordinate[] hullPts = vertexRing.getCoordinates();
    return inputRing.getFactory().createLinearRing(hullPts);
  }
  
  private void init(Coordinate[] ring, boolean isOuter) {
    /**
     * Ensure ring is oriented according to outer/inner:
     * - outer, CW
     * - inner: CCW 
     */
    boolean orientCW = isOuter;
    if (orientCW == Orientation.isCCW(ring)) {
      ring = ring.clone();
      CoordinateArrays.reverse(ring);
    }
    
    vertexRing = new LinkedRing(ring);
    vertexIndex = new VertexSequencePackedRtree(ring);
    //-- remove duplicate final vertex
    vertexIndex.remove(ring.length-1);
    
    cornerQueue = new PriorityQueue<Corner>();
    for (int i = 0; i < vertexRing.size(); i++) {
      addCorner(i, cornerQueue);
    }
  }

  private void addCorner(int i, PriorityQueue<Corner> cornerQueue) {
    //-- convex corners are left untouched
    if (isConvex(vertexRing, i)) 
      return;
    //-- corner is concave or flat - both can be removed
    Corner corner = new Corner(i, 
        vertexRing.prev(i),
        vertexRing.next(i),
        area(vertexRing, i));
    cornerQueue.add(corner);
  }
  
  public static boolean isConvex(LinkedRing vertexRing, int index) {
    Coordinate pp = vertexRing.prevCoordinate(index);
    Coordinate p = vertexRing.getCoordinate(index);
    Coordinate pn = vertexRing.nextCoordinate(index);
    return Orientation.CLOCKWISE == Orientation.index(pp, p, pn);
  }

  public static double area(LinkedRing vertexRing, int index) {
    Coordinate pp = vertexRing.prevCoordinate(index);
    Coordinate p = vertexRing.getCoordinate(index);
    Coordinate pn = vertexRing.nextCoordinate(index);
    return Triangle.area(pp, p, pn);
  }
  
  public void compute(RingHullIndex hullIndex) {        
    while (! cornerQueue.isEmpty() 
        && vertexRing.size() > 3) {
      Corner corner = cornerQueue.poll();
      //-- a corner may no longer be valid due to removal of adjacent corners
      if (corner.isRemoved(vertexRing))
        continue;
      if (isAtTarget(corner))
        return;
      //System.out.println(corner.toLineString(vertexList));
      /**
       * Corner is concave or flat - remove it if possible.
       */
      if ( isRemovable(corner, hullIndex) ) {
        removeCorner(corner, cornerQueue);
      }
    }
  }

  private boolean isAtTarget(Corner corner) {
    if (targetVertexNum >= 0) {
      return vertexRing.size() < targetVertexNum;
    }
    if (targetAreaDelta >= 0) {
      //-- include candidate corder to avoid overshooting target
      // (important for very small target area deltas)
      return areaDelta + corner.getArea() > targetAreaDelta;
    }
    //-- no target set
    return true;
  }
  
  /**
   * Removes a corner by removing the apex vertex from the ring.
   * Two new corners are created with apexes
   * at the other vertices of the corner
   * (if they are non-convex and thus removable).
   * 
   * @param corner the corner to remove
   * @param cornerQueue the corner queue
   */
  private void removeCorner(Corner corner, PriorityQueue<Corner> cornerQueue) {
    int index = corner.getIndex();
    int prev = vertexRing.prev(index);
    int next = vertexRing.next(index);
    vertexRing.remove(index);
    vertexIndex.remove(index);
    areaDelta += corner.getArea();
    
    //-- potentially add the new corners created
    addCorner(prev, cornerQueue);
    addCorner(next, cornerQueue);
  }

  private boolean isRemovable(Corner corner, RingHullIndex hullIndex) {
    Envelope cornerEnv = corner.envelope(vertexRing);
    if (hasIntersectingVertex(corner, cornerEnv, this))
      return false;
    //-- no other rings to check
    if (hullIndex == null) 
      return true;
    //-- check other rings for intersections
    for (RingHull hull : hullIndex.query(cornerEnv)) {
      //-- this hull was already checked above
      if (hull == this)
        continue;
      if (hasIntersectingVertex(corner, cornerEnv, hull)) 
        return false;
    }
    return true;
  }

  /**
   * Tests if any vertices in a hull intersect the corner triangle.
   * Uses the vertex spatial index for efficiency.
   * 
   * @param corner the corner vertices
   * @param cornerEnv the envelope of the corner
   * @param hull the hull to test
   * @return true if there is an intersecting vertex
   */
  private boolean hasIntersectingVertex(Corner corner, Envelope cornerEnv, 
      RingHull hull) {
    int[] result = hull.query(cornerEnv);
    for (int i = 0; i < result.length; i++) {
      int index = result[i];
      //-- skip vertices of corner
      if (hull == this && corner.isVertex(index))
        continue;
      
      Coordinate v = hull.getCoordinate(index);
      //--- does corner triangle contain vertex?
      if (corner.intersects(v, vertexRing))
        return true;
    }
    return false;
  }
  
  private Coordinate getCoordinate(int index) {
    return vertexRing.getCoordinate(index);
  }

  private int[] query(Envelope cornerEnv) {
    return vertexIndex.query(cornerEnv);
  }

  void queryHull(Envelope queryEnv, List<Coordinate> pts) {
    int[] result = vertexIndex.query(queryEnv);
    
    for (int i = 0; i < result.length; i++) {
      int index = result[i];
      //-- skip if already removed
      if (! vertexRing.hasCoordinate(index))
        continue;
      Coordinate v = vertexRing.getCoordinate(index);
      pts.add(v);
    }

  }

  public Polygon toGeometry() {
    GeometryFactory fact = new GeometryFactory();
    Coordinate[] coords = vertexRing.getCoordinates();
    return fact.createPolygon(fact.createLinearRing(coords));
  }

  private static class Corner implements Comparable<Corner> {
    private int index;
    private int prev;
    private int next;
    private double area;

    public Corner(int i, int prev, int next, double area) {
      this.index = i;
      this.prev = prev;
      this.next = next;
      this.area = area;
    }

    public boolean isVertex(int index) {
      return index == this.index
          || index == prev
          || index == next;
    }

    public int getIndex() {
      return index;
    }
    
    public double getArea() {
      return area;
    }
    
    /**
     * Orders corners by increasing area
     */
    @Override
    public int compareTo(Corner o) {
      return Double.compare(area, o.area);
    }
    
    public Envelope envelope(LinkedRing ring) {
      Coordinate pp = ring.getCoordinate(prev);
      Coordinate p = ring.getCoordinate(index);
      Coordinate pn = ring.getCoordinate(next);
      Envelope env = new Envelope(pp, pn);
      env.expandToInclude(p);
      return env;
    }
    
    public boolean intersects(Coordinate v, LinkedRing ring) {
      Coordinate pp = ring.getCoordinate(prev);
      Coordinate p = ring.getCoordinate(index);
      Coordinate pn = ring.getCoordinate(next);
      return Triangle.intersects(pp, p, pn, v);
    }
    
    public boolean isRemoved(LinkedRing ring) {
      return ring.prev(index) != prev || ring.next(index) != next;
    }
    
    public LineString toLineString(LinkedRing ring) {
      Coordinate pp = ring.getCoordinate(prev);
      Coordinate p = ring.getCoordinate(index);
      Coordinate pn = ring.getCoordinate(next);
      return (new GeometryFactory()).createLineString(
          new Coordinate[] { safeCoord(pp), safeCoord(p), safeCoord(pn) });
    }

    private static Coordinate safeCoord(Coordinate p) {
      if (p ==null) return new Coordinate(Double.NaN, Double.NaN);
      return p;
    }
  }


}