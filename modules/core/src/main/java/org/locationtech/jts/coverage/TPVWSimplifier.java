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
import java.util.BitSet;
import java.util.List;
import java.util.PriorityQueue;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
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
   * Simplifies a set of lines, preserving the topology of the lines.
   * 
   * @param lines the lines to simplify
   * @param distanceTolerance the simplification tolerance
   * @return the simplified lines
   */
  public static MultiLineString simplify(MultiLineString lines, double distanceTolerance) {
    TPVWSimplifier simp = new TPVWSimplifier(lines, distanceTolerance);
    MultiLineString result = (MultiLineString) simp.simplify();
    return result;
  }
  
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
  public static MultiLineString simplify(MultiLineString lines, BitSet freeRings,
      MultiLineString constraintLines, double distanceTolerance) {
    TPVWSimplifier simp = new TPVWSimplifier(lines, distanceTolerance);
    simp.setFreeRingIndices(freeRings);
    simp.setConstraints(constraintLines);
    MultiLineString result = (MultiLineString) simp.simplify();
    return result;
  }
 
  private MultiLineString inputLines;
  private BitSet isFreeRing;
  private double areaTolerance;
  private GeometryFactory geomFactory;
  private MultiLineString constraintLines = null;

  private TPVWSimplifier(MultiLineString lines, double distanceTolerance) {
    this.inputLines = lines;
    this.areaTolerance = distanceTolerance * distanceTolerance;
    geomFactory = inputLines.getFactory();
  }
  
  private void setConstraints(MultiLineString constraints) {
    this.constraintLines = constraints;
  }

  public void setFreeRingIndices(BitSet isFreeRing) {
    //Assert: bit set has same size as number of lines.
    this.isFreeRing = isFreeRing;
  }

  private Geometry simplify() {
    List<Edge> edges = createEdges(inputLines, this.isFreeRing);
    List<Edge> constraintEdges = createEdges(constraintLines, null);

    EdgeIndex edgeIndex = new EdgeIndex();
    edgeIndex.add(edges);
    edgeIndex.add(constraintEdges);

    LineString[] result = new LineString[edges.size()];
    for (int i = 0 ; i < edges.size(); i++) {
      Edge edge = edges.get(i);
      Coordinate[] ptsSimp = edge.simplify(edgeIndex);
      result[i] = geomFactory.createLineString(ptsSimp);
    }
    return geomFactory.createMultiLineString(result);
  }

  private List<Edge> createEdges(MultiLineString lines, BitSet isFreeRing) {
    List<Edge> edges = new ArrayList<Edge>();
    if (lines == null)
      return edges;
    for (int i = 0 ; i < lines.getNumGeometries(); i++) {
      LineString line = (LineString) lines.getGeometryN(i);
      boolean isFree = isFreeRing == null ? false : isFreeRing.get(i);
      edges.add(new Edge(line, isFree, areaTolerance));
    }
    return edges;
  }
  
  private static class Edge {
    private double areaTolerance;
    private LinkedLine linkedLine;
    private int minEdgeSize;
    private boolean isFreeRing;
    private int nbPts;

    private VertexSequencePackedRtree vertexIndex;
    private Envelope envelope;

    /**
     * Creates a new edge.
     * The endpoints of the edge are preserved during simplification,
     * unless it is a ring and the {@Link #isFreeRing} flag is set.
     * 
     * @param inputLine the line or ring
     * @param isFreeRing whether a ring endpoint can be removed
     * @param areaTolerance the simplification tolerance
     */
    Edge(LineString inputLine, boolean isFreeRing, double areaTolerance) {
      this.areaTolerance = areaTolerance;
      this.isFreeRing = isFreeRing;
      this.envelope = inputLine.getEnvelopeInternal();
      Coordinate[] pts = inputLine.getCoordinates();
      this.nbPts = pts.length;
      linkedLine = new LinkedLine(pts);
      minEdgeSize = linkedLine.isRing() ? 3 : 2;
      
      vertexIndex = new VertexSequencePackedRtree(pts);
      //-- remove ring duplicate final vertex
      if (linkedLine.isRing()) {
        vertexIndex.remove(pts.length-1);
      }
    }

    private Coordinate getCoordinate(int index) {
      return linkedLine.getCoordinate(index);
    }
  
    public Envelope getEnvelope() {
      return envelope;
    }
    
    public int size() {
      return linkedLine.size();
    }
    
    private Coordinate[] simplify(EdgeIndex edgeIndex) {        
      PriorityQueue<Corner> cornerQueue = createQueue();
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
          removeCorner(corner, cornerQueue);
        }
      }
      return linkedLine.getCoordinates();
    }

    private PriorityQueue<Corner> createQueue() {
      PriorityQueue<Corner> cornerQueue = new PriorityQueue<Corner>();
      int minIndex = (linkedLine.isRing() && isFreeRing) ? 0 : 1;
      int maxIndex = nbPts - 1;
      for (int i = minIndex; i < maxIndex; i++) {
        addCorner(i, cornerQueue);
      }
      return cornerQueue;
    }
    
    private void addCorner(int i, PriorityQueue<Corner> cornerQueue) {
      if (isFreeRing || (i != 0 && i != nbPts-1)) {
        Corner corner = new Corner(linkedLine, i);
        if (corner.getArea() <= areaTolerance) {
          cornerQueue.add(corner);
        }
      }
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

    private int[] query(Envelope cornerEnv) {
      return vertexIndex.query(cornerEnv);
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
      int prev = linkedLine.prev(index);
      int next = linkedLine.next(index);
      linkedLine.remove(index);
      vertexIndex.remove(index);

      //-- potentially add the new corners created
      addCorner(prev, cornerQueue);
      addCorner(next, cornerQueue);
    }

    public String toString() {
      return linkedLine.toString();
    }
  }
  
  private static class EdgeIndex {

    STRtree index = new STRtree(); 
    
    public void add(List<Edge> edges) {
      for (Edge edge : edges) {
        add(edge);
      }
    }
    
    public void add(Edge edge) {
      index.insert(edge.getEnvelope(), edge);
    }
    
    public List<Edge> query(Envelope queryEnv) {
      return index.query(queryEnv);
    }
  }
  
}
