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
import org.locationtech.jts.simplify.Corner;
import org.locationtech.jts.simplify.LinkedLine;

/**
 * Computes a Topology-Preserving Visvalingnam-Whyatt simplification
 * of a set of input lines.
 * The simplified lines will contain no more intersections than are present
 * in the original input, and line endpoints are preserved.
 * <p>
 * The amount of simplification is determined by a tolerance value, 
 * which is a non-zero quantity. 
 * It is the square root of the area tolerance used 
 * in the Visvalingam-Whyatt algorithm.
 * This equates roughly to the maximum
 * distance by which a simplfied line can change from the original.
 * 
 * @author mdavis
 *
 */
class TPVWSimplifier {

  /**
   * Simplifies a set of lines, preserving the topology of the lines.
   * 
   * @param lines the lines to simplify
   * @param constraints the linear constraints
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
   * 
   * @param lines the lines to simplify
   * @param constraints the linear constraints
   * @param distanceTolerance the simplification tolerance
   * @return the simplified lines
   */
  public static MultiLineString simplify(MultiLineString lines, 
      MultiLineString constraints, double distanceTolerance) {
    TPVWSimplifier simp = new TPVWSimplifier(lines, distanceTolerance);
    simp.setConstraints(constraints);
    MultiLineString result = (MultiLineString) simp.simplify();
    return result;
  }
 
  private MultiLineString input;
  private double areaTolerance;
  private GeometryFactory geomFactory;
  private MultiLineString constraints = null;

  private TPVWSimplifier(MultiLineString lines, double distanceTolerance) {
    this.input = lines;
    this.areaTolerance = distanceTolerance * distanceTolerance;
    geomFactory = input.getFactory();
  }
  
  private void setConstraints(MultiLineString constraints) {
    this.constraints = constraints;
  }

  private Geometry simplify() {
    List<Edge> edges = createEdges(input);
    List<Edge> constraintEdges = createEdges(constraints);
    
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

  private List<Edge> createEdges(MultiLineString lines) {
    List<Edge> edges = new ArrayList<Edge>();
    if (lines == null)
      return edges;
    for (int i = 0 ; i < lines.getNumGeometries(); i++) {
      LineString line = (LineString) lines.getGeometryN(i);
      edges.add(new Edge(line, areaTolerance));
    }
    return edges;
  }
  
  private static class Edge {
    private double areaTolerance;
    private LinkedLine linkedLine;
    private int minEdgeSize;

    private VertexSequencePackedRtree vertexIndex;
    private Envelope envelope;
    
    Edge(LineString inputLine, double areaTolerance) {
      this.areaTolerance = areaTolerance;
      this.envelope = inputLine.getEnvelopeInternal();
      Coordinate[] pts = inputLine.getCoordinates();
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
      for (int i = 1; i < linkedLine.size() - 1; i++) {
        addCorner(i, cornerQueue);
      }
      return cornerQueue;
    }
    
    private void addCorner(int i, PriorityQueue<Corner> cornerQueue) {
      if (! linkedLine.isCorner(i))
        return;
      Corner corner = new Corner(linkedLine, i);
      if (corner.getArea() <= areaTolerance) {
        cornerQueue.add(corner);
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
     * @param hull the hull to test
     * @return true if there is an intersecting vertex
     */
    private boolean hasIntersectingVertex(Corner corner, Envelope cornerEnv, 
        Edge edge) {
      int[] result = edge.query(cornerEnv);
      for (int i = 0; i < result.length; i++) {
        int index = result[i];
        
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