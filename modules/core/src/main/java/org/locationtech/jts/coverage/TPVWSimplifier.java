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

import java.util.List;
import java.util.PriorityQueue;

import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.VertexSequencePackedRtree;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.simplify.LinkedLine;

/**
 * Computes a Topology-Preserving Visvalingam-Whyatt simplification
 * of a set of input lines.
 * The simplified lines will contain no more intersections than are present
 * in the original input.
 * Line and ring endpoints are preserved, except for rings 
 * which are flagged as "free".
 * Rings which are smaller than the tolerance area
 * may be removed entirely, as long as they are flagged as removable.
 * <p>
 * The amount of simplification is determined by a tolerance value, 
 * which is a non-zero quantity. 
 * It is the square root of the area tolerance used 
 * in the Visvalingam-Whyatt algorithm.
 * This equates roughly to the maximum
 * distance by which a simplified line can change from the original.
 * 
 * @author mdavis
 *
 */
class TPVWSimplifier {
  
  /**
   * Simplifies a set of lines, preserving the topology of the lines between
   * themselves and a set of linear constraints.
   * The endpoints of lines are preserved.
   * The endpoint of rings are preserved as well, unless
   * the ring is indicated as "free" via a bit flag with the same index.
   * 
   * @param lines the lines to simplify
   * @param freeRings flags indicating which ring edges do not have node endpoints
   * @param constraintLines the linear constraints (may be null)
   * @param distanceTolerance the simplification tolerance
   * @return the simplified lines
   */
  public static void simplify(Edge[] edges, 
      CornerArea cornerArea,
      double removableSizeFactor) {
    TPVWSimplifier simp = new TPVWSimplifier(edges);
    simp.setCornerArea(cornerArea);
    simp.setRemovableRingSizeFactor(removableSizeFactor);
    simp.simplify();
  }
 
  private CornerArea cornerArea;
  private double removableSizeFactor = 1.0;
  private Edge[] edges;
  
  public TPVWSimplifier(Edge[] edges) {
    this.edges = edges;
  }
  
  public void setRemovableRingSizeFactor(double removableSizeFactor) {
    this.removableSizeFactor = removableSizeFactor;
  }
  
  public void setCornerArea(CornerArea cornerArea) {
    this.cornerArea = cornerArea;
  }
 
  private void simplify() {
    EdgeIndex edgeIndex = new EdgeIndex();
    add(edges, edgeIndex);

    for (int i = 0 ; i < edges.length; i++) {
      Edge edge = edges[i];
      edge.simplify(cornerArea, edgeIndex);
    }
  }

  private void add(Edge[] edges, EdgeIndex edgeIndex) {
    for (Edge edge : edges) {
      //-- don't include removed edges in index
      edge.updateRemoved(removableSizeFactor);
      if (! edge.isRemoved()) {
        //-- avoid fluffing up removed edges
        edge.init();
        edgeIndex.add(edge);
      }
    }
  }
  
  public static class Edge {
    private static final int MIN_EDGE_SIZE = 2;
    private static final int MIN_RING_SIZE = 4;
    
    private LinkedLine linkedLine;
    private boolean isFreeRing;
    private int nPts;
    private Coordinate[] pts;
    private VertexSequencePackedRtree vertexIndex;
    private Envelope envelope;
    private boolean isRemoved = false;
    private boolean isRemovable;
    private double distanceTolerance = 0.0;

    /**
     * Creates a new edge.
     * The endpoints of the edge are preserved during simplification,
     * unless it is a ring and the {@Link #isFreeRing} flag is set.
     * 
     * @param pts the line or ring
     * @param distanceTolerance 
     * @param isFreeRing whether a ring endpoint can be removed
     * @param isFreeRing 
     * @param isRemovable 
     */
    Edge(Coordinate[] pts, double distanceTolerance, boolean isFreeRing, boolean isRemovable) {
      this.envelope = CoordinateArrays.envelope(pts);
      this.pts = pts;
      this.nPts = pts.length;
      this.isFreeRing = isFreeRing;
      this.isRemovable = isRemovable;
      this.distanceTolerance  = distanceTolerance;
    }

    public void updateRemoved(double removableSizeFactor) {
      if (! isRemovable)
        return;
      double areaTolerance = distanceTolerance * distanceTolerance;
      isRemoved = CoordinateArrays.isRing(pts) 
          && Area.ofRing(pts) < removableSizeFactor * areaTolerance;
    }
    
    public void init() {
      linkedLine = new LinkedLine(pts);      
    }
    
    public double getTolerance() {
      return distanceTolerance;
    }
    
    public boolean isRemoved() {
      return isRemoved;
    }
    
    private Coordinate getCoordinate(int index) {
      return pts[index];
    }
  
    public Coordinate[] getCoordinates() {
      if (isRemoved) {
        return new Coordinate[0];
      }
      return linkedLine.getCoordinates();
    }
    
    public Envelope getEnvelope() {
      return envelope;
    }
    
    public int size() {
      return linkedLine.size();
    }
    
    public void simplify(CornerArea cornerArea, EdgeIndex edgeIndex) {     
      if (isRemoved) {
        return;
      }
      //-- don't simplify
      if (distanceTolerance <= 0.0)
        return;
      
      double areaTolerance = distanceTolerance * distanceTolerance;
      int minEdgeSize = linkedLine.isRing() ? MIN_RING_SIZE : MIN_EDGE_SIZE;

      PriorityQueue<Corner> cornerQueue = createQueue(areaTolerance, cornerArea);
      while (! cornerQueue.isEmpty()
          && size() > minEdgeSize) {
        Corner corner = cornerQueue.poll();
        //-- a corner may no longer be valid due to removal of adjacent corners
        if (corner.isRemoved())
          continue;
        //System.out.println(corner.toLineString(edge));
        //-- done when all small corners are removed
        if (corner.getArea() > areaTolerance)
          break;
        if (isRemovable(corner, edgeIndex) ) {
          removeCorner(corner, areaTolerance, cornerArea, cornerQueue);
        }
      }
    }

    private PriorityQueue<Corner> createQueue(double areaTolerance, CornerArea cornerArea) {
      PriorityQueue<Corner> cornerQueue = new PriorityQueue<Corner>();
      int minIndex = (linkedLine.isRing() && isFreeRing) ? 0 : 1;
      int maxIndex = nPts - 1;
      for (int i = minIndex; i < maxIndex; i++) {
        addCorner(i, areaTolerance, cornerArea, cornerQueue);
      }
      return cornerQueue;
    }
    
    private void addCorner(int i, double areaTolerance, CornerArea cornerArea, PriorityQueue<Corner> cornerQueue) {
      //-- add if this vertex can be a corner
      if (isFreeRing || (i != 0 && i != nPts - 1)) {
        double area = area(i, cornerArea);
        if (area <= areaTolerance) {
          Corner corner = new Corner(linkedLine, i, area);
          cornerQueue.add(corner);
        }
      }
    }
    
    private double area(int index, CornerArea cornerArea) {
      Coordinate pp = linkedLine.prevCoordinate(index);
      Coordinate p = linkedLine.getCoordinate(index);
      Coordinate pn = linkedLine.nextCoordinate(index);
      return cornerArea.area(pp, p, pn);
    }
    
    private boolean isRemovable(Corner corner, EdgeIndex edgeIndex) {
      Envelope cornerEnv = corner.envelope();
      //-- check nearby lines for violating intersections
      //-- the query also returns this line for checking
      for (Edge edge : edgeIndex.query(cornerEnv)) {
        if (hasIntersectingVertex(corner, cornerEnv, edge))
          return false;
        //-- check if corner base equals line (2-pts)
        //-- if so, don't remove corner, since that would collapse to the line
        if (edge != this && edge.size() == 2) {
          Coordinate[] linePts = edge.linkedLine.getCoordinates();
          if (corner.isBaseline(linePts[0], linePts[1]))
            return false;
        }
      }
      return true;
    }

    /**
     * Tests if any vertices in a line intersect the corner triangle.
     * Uses the vertex spatial index for efficiency.
     * 
     * @param corner the corner vertices
     * @param cornerEnv the envelope of the corner
     * @param edge the hull to test
     * @return true if there is an intersecting vertex
     */
    private boolean hasIntersectingVertex(Corner corner, Envelope cornerEnv, 
        Edge edge) {
      int[] result = edge.query(cornerEnv);
      for (int index : result) {

        Coordinate v = edge.getCoordinate(index);
        // ok if corner touches another line - should only happen at endpoints
        if (corner.isVertex(v))
            continue;
        
        //--- does corner triangle contain vertex?
        if (corner.intersects(v))
          return true;
      }
      return false;
    }

    private void initIndex() {
      vertexIndex = new VertexSequencePackedRtree(pts);
      //-- remove ring duplicate final vertex
      if (CoordinateArrays.isRing(pts)) {
        vertexIndex.remove(pts.length-1);
      }
    }
    
    private int[] query(Envelope cornerEnv) {
      if (vertexIndex == null) {
        initIndex();
      }
      return vertexIndex.query(cornerEnv);
    }

    /**
     * Removes a corner by removing the apex vertex from the ring.
     * Two new corners are created with apexes
     * at the other vertices of the corner
     * (if they are non-convex and thus removable).
     * 
     * @param corner the corner to remove
     * @param cornerArea 
     * @param areaTolerance 
     * @param cornerQueue the corner queue
     */
    private void removeCorner(Corner corner, double areaTolerance, CornerArea cornerArea, PriorityQueue<Corner> cornerQueue) {
      int index = corner.getIndex();
      int prev = linkedLine.prev(index);
      int next = linkedLine.next(index);
      linkedLine.remove(index);
      vertexIndex.remove(index);

      //-- potentially add the new corners created
      addCorner(prev, areaTolerance, cornerArea, cornerQueue);
      addCorner(next, areaTolerance, cornerArea, cornerQueue);
    }

    public String toString() {
      return linkedLine.toString();
    }
  }
  
  private static class EdgeIndex {

    STRtree index = new STRtree(); 
    
    public void add(Edge edge) {
      index.insert(edge.getEnvelope(), edge);
    }
    
    public List<Edge> query(Envelope queryEnv) {
      return index.query(queryEnv);
    }
  }
  
}
