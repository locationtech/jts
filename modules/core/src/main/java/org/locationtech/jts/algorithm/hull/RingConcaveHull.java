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
package org.locationtech.jts.algorithm.hull;

import java.util.PriorityQueue;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.triangulate.polygon.VertexSequencePackedRtree;

/**
 * 
 * @author Martin Davis
 *
 */
class RingConcaveHull {
  
  public static Coordinate[] hull(Coordinate[] ring, boolean isOuter, int targetVertexCount) {
    RingConcaveHull hull = new RingConcaveHull(ring, isOuter, targetVertexCount);
    return hull.getHull();
  }
  
  /**
   * The polygon vertices are provided in CW orientation. 
   * Thus for convex interior angles 
   * the vertices forming the angle are in CW orientation.
   */
  private final Coordinate[] vertex;
  private LinkedRing vertexRing;
  
  /**
   * Indexing vertices improves corner intersection testing performance.
   * The polyShell vertices are contiguous, so are suitable for an VSPRtree.
   */
  private VertexSequencePackedRtree vertexIndex;

  private int targetVertexCount;

  private PriorityQueue<Corner> cornerQueue;

  /**
   * Creates a new PolygonConcaveHull instance.
   * 
   * @param polyShell the polygon vertices to process
   */
  private RingConcaveHull(Coordinate[] ring, boolean isOuter, int targetVertexCount) {
    this.vertex = ring; 
    this.targetVertexCount = targetVertexCount;
    init(vertex, isOuter);

  }

  private Coordinate[] getHull() {
    compute();
    return vertexRing.getCoordinates();
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
  
  public void compute() {        
    while (! cornerQueue.isEmpty() 
        && vertexRing.size() > targetVertexCount
        && vertexRing.size() > 3) {
      Corner corner = cornerQueue.poll();
      //-- a corner may no longer be valid due to removal of adjacent corners
      if (corner.isRemoved(vertexRing))
        continue;
      //System.out.println(corner.toLineString(vertexList));
      /**
       * Corner is concave or flat - remove it if possible.
       */
      if ( isRemovable(corner) ) {
        removeCorner(corner, cornerQueue);
      }
    }
  }
  
  private void removeCorner(Corner corner, PriorityQueue<Corner> cornerQueue) {
    int index = corner.getIndex();
    int prev = vertexRing.prev(index);
    int next = vertexRing.next(index);
    vertexRing.remove(index);
    vertexIndex.remove(index);
    //-- potentially add the new corners created
    addCorner(prev, cornerQueue);
    addCorner(next, cornerQueue);
  }

  private boolean isRemovable(Corner corner) {
    return ! hasIntersectingVertex(corner);
  }

  /**
   * Tests if any other current vertices intersect the corner triangle.
   * Uses the vertex spatial index for efficiency.
   * 
   * @param corner the corner vertices
   * @return true if there is an intersecting vertex
   */
  private boolean hasIntersectingVertex(Corner corner) {
    Envelope cornerEnv = corner.envelope(vertexRing);
    int[] result = vertexIndex.query(cornerEnv);
    
    for (int i = 0; i < result.length; i++) {
      int index = result[i];
      //-- skip if already removed
      if (! vertexRing.hasCoordinate(index))
        continue;
      //-- skip vertices of corner
      if (corner.isVertex(index))
        continue;
      
      Coordinate v = vertexRing.getCoordinate(index);
      //--- does corner triangle contain vertex?
      if (corner.intersects(v, vertexRing))
        return true;
    }
    return false;
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