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
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.triangulate.polygon.VertexSequencePackedRtree;

/**
 * 
 * @author Martin Davis
 *
 */
public class PolygonConcaveHull {
  
  public static Geometry hull(Geometry geom, double vertexCountFraction) {
    Coordinate[] pts = geom.getCoordinates();
    PolygonConcaveHull hull = new PolygonConcaveHull(pts, vertexCountFraction);
    return hull.getResult();
  }
  
  /**
   * The polygon vertices are provided in CW orientation. 
   * Thus for convex interior angles 
   * the vertices forming the angle are in CW orientation.
   */
  private final Coordinate[] vertex;
  private VertexRing vertexRing;
  
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
  public PolygonConcaveHull(Coordinate[] polyShell, double vertexSizeFraction) {
    this.vertex = polyShell; 
    targetVertexCount = (int) ((polyShell.length - 1) * vertexSizeFraction);
  }

  public Geometry getResult() {
    compute();
    return toGeometry();
  }
  
  private void init(Coordinate[] vertex) { 
    //-- ensure ring is CW
    if (Orientation.isCCW(vertex)) {
      vertex = vertex.clone();
      CoordinateArrays.reverse(vertex);
    }
    
    vertexRing = new VertexRing(vertex);
    vertexIndex = new VertexSequencePackedRtree(vertex);
    
    cornerQueue = new PriorityQueue<Corner>();
    for (int i = 0; i < vertexRing.size(); i++) {
      addCorner(i, cornerQueue);
    }
  }

  private void addCorner(int i, PriorityQueue<Corner> cornerQueue) {
    //-- convex corners are left untouched
    if (vertexRing.isConvex(i)) 
      return;
    //-- corner is concave or flat - both can be removed
    Corner corner = new Corner(i, 
        vertexRing.prev(i),
        vertexRing.next(i),
        vertexRing.area(i));
    cornerQueue.add(corner);
  }
  
  public void compute() {    
    init(vertex);
    
    while (! cornerQueue.isEmpty() 
        && vertexRing.size() > targetVertexCount
        && vertexRing.size() > 3) {
      Corner corner = cornerQueue.poll();
      //-- a corner may no longer be valid due to removal of adjacent corners
      if (vertexRing.isRemoved(corner))
        continue;
      //System.out.println(corner.toLineString(vertexList));
      /**
       * Concave/Flat corner - remove it if safe.
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
      int vertIndex = result[i];
      //-- skip if already removed
      if (! vertexRing.hasVertex(vertIndex))
        continue;
      //-- skip vertices of corner
      if (corner.isVertex(vertIndex))
        continue;
      
      Coordinate v = vertexRing.getCoordinate(vertIndex);
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

  private static class VertexRing {
    
    private static final int NO_VERTEX_INDEX = -1;

    private final Coordinate[] vertex;
    private int[] next = null;
    private int[] prev = null;
    private int size;
    
    public VertexRing(Coordinate[] pts) {
      vertex = pts;
      size = pts.length - 1;
      next = createNextLinks(size);
      prev = createPrevLinks(size);
    }

    public int size() {
      return size;
    }

    public Coordinate getCoordinate(int index) {
      return vertex[index];
    }

    private static int[] createNextLinks(int size) {
      int[] next = new int[size];
      for (int i = 0; i < size; i++) {
        next[i] = i + 1;
      }
      next[size - 1] = 0;
      return next;
    }
    
    private static int[] createPrevLinks(int size) {
      int[] prev = new int[size];
      for (int i = 0; i < size; i++) {
        prev[i] = i - 1;
      }
      prev[0] = size - 1;
      return prev;
    }
    
    public boolean isConvex(int index) {
      Coordinate pp = vertex[prev[index]];
      Coordinate p = vertex[index];
      Coordinate pn = vertex[next[index]];
      return Orientation.CLOCKWISE == Orientation.index(pp, p, pn);
    }

    public int next(int i) {
      return next[i];
    }

    public int prev(int i) {
      return prev[i];
    }

    public boolean hasVertex(int index) {
      return index < prev.length 
          && prev[index] != NO_VERTEX_INDEX;
    }
    public double area(int index) {
      Coordinate pp = vertex[prev[index]];
      Coordinate p = vertex[index];
      Coordinate pn = vertex[next[index]];
      return Triangle.area(pp,  p,  pn);
    }
    
    public void remove(int index) {
      int iprev = prev[index];
      int inext = next[index];
      next[iprev] = inext;
      prev[inext] = iprev;
      prev[index] = NO_VERTEX_INDEX;
      next[index] = NO_VERTEX_INDEX;
      size--;
    }

    public boolean isRemoved(Corner corner) {
      return isCornerRemoved(corner.getIndex(), corner.getPrev(), corner.getNext());
    }
    
    public boolean isCornerRemoved(int index, int iprev, int inext) {
      return prev[index] != iprev
          || next[index] != inext;
    }
    
    public Coordinate[] getCoordinates() {
      CoordinateList coords = new CoordinateList();
      for (int i = 0; i < vertex.length - 1; i++) {
        if (prev[i] != NO_VERTEX_INDEX) {
          coords.add(vertex[i].copy(), false);
        }
      }
      coords.closeRing();
      return coords.toCoordinateArray();
    }
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

    public int getNext() {
      return next;
    }

    public int getPrev() {
      return prev;
    }

    public int getIndex() {
      return index;
    }
    
    @Override
    public int compareTo(Corner o) {
      return Double.compare(area, o.area);
    }
    
    public Envelope envelope(VertexRing vertexList) {
      Coordinate pp = vertexList.getCoordinate(prev);
      Coordinate p = vertexList.getCoordinate(index);
      Coordinate pn = vertexList.getCoordinate(next);
      Envelope env = new Envelope(pp, pn);
      env.expandToInclude(p);
      return env;
    }
    
    public boolean intersects(Coordinate v, VertexRing vertexList) {
      Coordinate pp = vertexList.getCoordinate(prev);
      Coordinate p = vertexList.getCoordinate(index);
      Coordinate pn = vertexList.getCoordinate(next);
      return Triangle.intersects(pp, p, pn, v);
    }
    
    public Geometry toLineString(VertexRing vertexList) {
      Coordinate pp = vertexList.getCoordinate(prev);
      Coordinate p = vertexList.getCoordinate(index);
      Coordinate pn = vertexList.getCoordinate(next);
      return (new GeometryFactory()).createLineString(
          new Coordinate[] { safeCoord(pp), safeCoord(p), safeCoord(pn) });
    }

    private static Coordinate safeCoord(Coordinate p) {
      if (p ==null) return new Coordinate(Double.NaN, Double.NaN);
      return p;
    }
  }
}