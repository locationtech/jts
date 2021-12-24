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
    Geometry geomNorm = geom.norm();
    Coordinate[] pts = geomNorm.getCoordinates();
    PolygonConcaveHull hull = new PolygonConcaveHull(pts, vertexCountFraction);
    return hull.getResult();
  }
  
  /**
   * The polygon vertices are provided in CW orientation. 
   * Thus for convex interior angles 
   * the vertices forming the angle are in CW orientation.
   */
  private final Coordinate[] vertex;
  
  private VertexRing vertexList;
  
  /**
   * Indexing vertices improves ear intersection testing performance a lot.
   * The polyShell vertices are contiguous, so are suitable for an SPRtree.
   */
  private VertexSequencePackedRtree vertexCoordIndex;

  private int targetVertexCount;

  private PriorityQueue<HullCorner> cornerQueue;

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
  
  private void init() {
/*
    vertexSize = vertex.length - 1;
    vertexNext = createNextLinks(vertexSize);
    vertexPrev = createPrevLinks(vertexSize);
    vertexFirst = 0;
  */  
    vertexList = new VertexRing(vertex);
    vertexCoordIndex = new VertexSequencePackedRtree(vertex);
    
    cornerQueue = new PriorityQueue<HullCorner>();
    for (int i = 0; i < vertex.length - 1; i++) {
      addCorner(i, cornerQueue);
    }
  }

  private void addCorner(int i, PriorityQueue<HullCorner> cornerQueue) {
    if (vertexList.isConvex(i)) 
      return;
    //-- corner is concave or flat - both can be removed
    HullCorner corner = new HullCorner(i, 
        vertexList.prev(i),
        vertexList.next(i),
        vertexList.area(i));
    cornerQueue.add(corner);
  }
  
  public void compute() {    
    init();
    
    while (vertexList.size() > targetVertexCount && ! cornerQueue.isEmpty()) {
      HullCorner corner = cornerQueue.poll();
      //System.out.println(corner.toLineString(vertexList));
      //-- a corner in the queue may no longer be present due to other removal
      if (vertexList.isRemoved(corner))
        continue;

      /**
       * Concave corner - remove it if safe.
       */
      if ( isRemovable(corner) ) {
        removeCorner(corner, cornerQueue);
      }
    }
  }
  
  private void removeCorner(HullCorner corner, PriorityQueue<HullCorner> cornerQueue) {
    int index = corner.getIndex();
    int prev = vertexList.prev(index);
    int next = vertexList.next(index);
    remove(vertexCoordIndex, vertexList.getCoordinate(index));
    vertexList.remove(index);
    addCorner(prev, cornerQueue);
    addCorner(next, cornerQueue);
  }

  private void remove(VertexSequencePackedRtree vertexIndex, Coordinate p) {
    int[] result = vertexIndex.query(new Envelope(p, p));
    if (result.length != 1) {
      throw new IllegalStateException("duplicate or no coordinates found during removal");
    }
    vertexIndex.remove(result[0]);
  }

  private boolean isRemovable(HullCorner corner) {
    return ! hasIntersectingVertex(corner);
  }

  /**
   * Finds another vertex intersecting the corner triangle, if any.
   * Uses the vertex spatial index for efficiency.
   * 
   * @param cornerIndex the index of the corner apex vertex
   * @param corner the corner vertices
   * @return the index of an intersecting or duplicate vertex, or {@link #NO_VERTEX_INDEX} if none
   */
  private boolean hasIntersectingVertex(HullCorner corner) {
    Envelope cornerEnv = corner.envelope(vertexList);
    int[] result = vertexCoordIndex.query(cornerEnv);
    
    for (int i = 0; i < result.length; i++) {
      int vertIndex = result[i];
      if (corner.isVertex(vertIndex))
        continue;
      
      Coordinate v = vertexList.getCoordinate(vertIndex);

      //--- does corner contain vertex?
      if (corner.intersects(v, vertexList))
        return true;
    }
    return false;
  }
  
  public Polygon toGeometry() {
    GeometryFactory fact = new GeometryFactory();
    Coordinate[] coords = vertexList.getCoordinates();
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

    public void remove(int index) {
      vertex[index] = null;
      int iprev = prev[index];
      int inext = next[index];
      next[iprev] = inext;
      prev[inext] = iprev;
      prev[index] = NO_VERTEX_INDEX;
      next[index] = NO_VERTEX_INDEX;
      size--;
    }
    
    public boolean isRemoved(int index) {
      return vertex[index] == null;
    }

    public boolean isRemoved(HullCorner corner) {
      return isCornerRemoved(corner.getIndex(), corner.getPrev(), corner.getNext());
    }
    
    public boolean isCornerRemoved(int index, int iprev, int inext) {
      return prev[index] != iprev
          || next[index] != inext;
    }
    
    public double area(int index) {
      Coordinate pp = vertex[prev[index]];
      Coordinate p = vertex[index];
      Coordinate pn = vertex[next[index]];
      return Triangle.area(pp,  p,  pn);
    }
    
    public Coordinate[] getCoordinates() {
      Coordinate[] coords = new Coordinate[size + 1];
      int index = 0;
      for (Coordinate v : vertex) {
        if (v != null) {
          coords[index++] = v;
        }
      }
      return coords;
    }
  }
  private static class HullCorner implements Comparable<HullCorner> {
    private int index;
    private int prev;
    private int next;
    private double area;

    public HullCorner(int i, int prev, int next, double area) {
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
    public int compareTo(HullCorner o) {
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