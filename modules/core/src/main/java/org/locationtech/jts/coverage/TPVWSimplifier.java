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

class TPVWSimplifier {

  public static MultiLineString simplify(MultiLineString lines, double tolerance) {
    TPVWSimplifier simp = new TPVWSimplifier(lines, tolerance);
    MultiLineString result = (MultiLineString) simp.simplify();
    return result;
  }
  
  private MultiLineString input;
  private double tolerance;
  private GeometryFactory geomFactory;

  private TPVWSimplifier(MultiLineString lines, double distanceTolerance) {
    this.input = lines;
    this.tolerance = distanceTolerance * distanceTolerance;
    geomFactory = input.getFactory();
  }
  
  private Geometry simplify() {
    List<Edge> edges = createEdges(input);
    
    EdgeIndex edgeIndex = new EdgeIndex();
    edgeIndex.add(edges);
    
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
    for (int i = 0 ; i < lines.getNumGeometries(); i++) {
      LineString line = (LineString) lines.getGeometryN(i);
      edges.add(new Edge(line, tolerance));
    }
    return edges;
  }
  
  private static class Edge {
    private double areaTolerance;
    private LinkedLine linkedLine;
    private int minEdgeSize;

    private PriorityQueue<Corner> cornerQueue;
    private VertexSequencePackedRtree vertexIndex;
    private Envelope envelope;
    
    Edge(LineString inputLine, double tolerance) {
      this.areaTolerance = tolerance;
      this.envelope = inputLine.getEnvelopeInternal();
      Coordinate[] pts = inputLine.getCoordinates();
      linkedLine = new LinkedLine(pts);
      minEdgeSize = linkedLine.isRing() ? 3 : 2;
      
      vertexIndex = new VertexSequencePackedRtree(pts);
      //-- remove ring duplicate final vertex
      if (linkedLine.isRing()) {
        vertexIndex.remove(pts.length-1);
      }
      
      cornerQueue = new PriorityQueue<Corner>();
      for (int i = 1; i < linkedLine.size() - 1; i++) {
        addCorner(i, cornerQueue);
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
    
    private void addCorner(int i, PriorityQueue<Corner> cornerQueue) {
      if (! linkedLine.isCorner(i))
        return;
      Corner corner = new Corner(linkedLine, i);
      if (corner.getArea() <= areaTolerance) {
        cornerQueue.add(corner);
      }
    }
    
    private Coordinate[] simplify(EdgeIndex lineIndex) {        
      while (! cornerQueue.isEmpty() 
          && linkedLine.size() > minEdgeSize) {
        Corner corner = cornerQueue.poll();
        //-- a corner may no longer be valid due to removal of adjacent corners
        if (corner.isRemoved(linkedLine))
          continue;
        //System.out.println(corner.toLineString(edge));
        //-- done when all small corners are removed
        if (corner.getArea() > areaTolerance)
          break;
        if (isRemovable(corner, lineIndex) ) {
          removeCorner(corner, cornerQueue);
        }
      }
      return linkedLine.getCoordinates();
    }
    
    private boolean isRemovable(Corner corner, EdgeIndex lineIndex) {
      Envelope cornerEnv = corner.envelope(linkedLine);
      //-- check nearby lines for violating intersections
      for (Edge line : lineIndex.query(cornerEnv)) {
        if (hasIntersectingVertex(corner, cornerEnv, line)) 
          return false;
        //-- check if base of corner equals line (2-pts)
        if (line != this && line.size() == 2) {
          Coordinate[] linePts = line.linkedLine.getCoordinates();
          if (isEqualSegs(
              corner.getPrev(linkedLine), corner.getNext(linkedLine),
              linePts[0], linePts[1]))
            return false;
        }
      }
      return true;
    }

    private static boolean isEqualSegs(Coordinate p0, Coordinate p1, 
        Coordinate q0, Coordinate q1) {
      if (p0.equals2D(q0) && p1.equals2D(q1)) return true;
      if (p0.equals2D(q1) && p1.equals2D(q0)) return true;
      return false;
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
        Edge line) {
      int[] result = line.query(cornerEnv);
      for (int i = 0; i < result.length; i++) {
        int index = result[i];
        
        Coordinate v = line.getCoordinate(index);
        // ok if corner touches another line - should only happen at endpoints
        if (corner.isVertex(this.linkedLine, v))
            continue;
        
        //--- does corner triangle contain vertex?
        if (corner.intersects(this.linkedLine, v))
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