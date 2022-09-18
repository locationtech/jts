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
import org.locationtech.jts.simplify.Corner;
import org.locationtech.jts.simplify.LinkedLine;

class TPVWSimplifier {

  public static MultiLineString simplify(MultiLineString lines, double tolerance) {
    TPVWSimplifier simp = new TPVWSimplifier(lines, tolerance);
    MultiLineString linesSimp = (MultiLineString) simp.simplify();
    return linesSimp;
  }
  
  private MultiLineString input;
  private double tolerance;
  private GeometryFactory geomFactory;

  TPVWSimplifier(MultiLineString lines, double distanceTolerance) {
    this.input = lines;
    this.tolerance = distanceTolerance * distanceTolerance;
    geomFactory = input.getFactory();
  }
  
  Geometry simplify() {
    List<Line> lines = new ArrayList<Line>();
    LineIndex lineIndex = new LineIndex();
    LineString[] result = new LineString[input.getNumGeometries()];
    for (int i = 0 ; i < result.length; i++) {
      LineString line = (LineString) input.getGeometryN(i);
      Line lineSimp = new Line(line, tolerance);
      lines.add(lineSimp);
      lineIndex.add(lineSimp);
    }
    for (int i = 0 ; i < result.length; i++) {
      Line lineSimp = lines.get(i);
      Coordinate[] ptsSimp = lineSimp.simplify(lineIndex);
      result[i] = geomFactory.createLineString(ptsSimp);
    }
    return geomFactory.createMultiLineString(result);
  }
  
  private static class Line {
    private double tolerance;
    private LinkedLine line;
    private int minEdgeSize;

    private PriorityQueue<Corner> cornerQueue;
    private VertexSequencePackedRtree vertexIndex;
    private Envelope envelope;
    
    Line(LineString inputLine, double tolerance) {
      this.tolerance = tolerance;
      this.envelope = inputLine.getEnvelopeInternal();
      Coordinate[] pts = inputLine.getCoordinates();
      line = new LinkedLine(pts);
      minEdgeSize = line.isRing() ? 3 : 2;
      
      vertexIndex = new VertexSequencePackedRtree(pts);
      //-- remove ring duplicate final vertex
      if (line.isRing()) {
        vertexIndex.remove(pts.length-1);
      }
      
      cornerQueue = new PriorityQueue<Corner>();
      for (int i = 1; i < line.size() - 1; i++) {
        addCorner(i, cornerQueue);
      }
    }

    private Coordinate getCoordinate(int index) {
      return line.getCoordinate(index);
    }
  
    public Envelope getEnvelope() {
      return envelope;
    }
    
    private void addCorner(int i, PriorityQueue<Corner> cornerQueue) {
      if (! line.isCorner(i))
        return;
      Corner corner = new Corner(line, i);
      if (corner.getArea() <= tolerance) {
        cornerQueue.add(corner);
      }
    }
    
    private Coordinate[] simplify(LineIndex lineIndex) {        
      while (! cornerQueue.isEmpty() 
          && line.size() > minEdgeSize) {
        Corner corner = cornerQueue.poll();
        //-- a corner may no longer be valid due to removal of adjacent corners
        if (corner.isRemoved(line))
          continue;
        //System.out.println(corner.toLineString(edge));
        //-- done when all small corners are removed
        if (corner.getArea() > tolerance)
          break;
        if (isRemovable(corner, lineIndex) ) {
          removeCorner(corner, cornerQueue);
        }
      }
      return line.getCoordinates();
    }
    
    private boolean isRemovable(Corner corner, LineIndex lineIndex) {
      Envelope cornerEnv = corner.envelope(line);
      if (hasIntersectingVertex(corner, cornerEnv, this))
        return false;
      //-- check other rings for intersections
      for (Line line : lineIndex.query(cornerEnv)) {
        //-- this line was already checked above
        if (line == this)
          continue;
        if (hasIntersectingVertex(corner, cornerEnv, line)) 
          return false;
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
        Line line) {
      int[] result = line.query(cornerEnv);
      for (int i = 0; i < result.length; i++) {
        int index = result[i];
        
        Coordinate v = line.getCoordinate(index);
        // ok if corner touches another line - should only happen at endpoints
        if (corner.isVertex(this.line, v))
            continue;
        
        //--- does corner triangle contain vertex?
        if (corner.intersects(this.line, v))
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
      int prev = line.prev(index);
      int next = line.next(index);
      line.remove(index);
      vertexIndex.remove(index);
      
      //-- potentially add the new corners created
      addCorner(prev, cornerQueue);
      addCorner(next, cornerQueue);
    }

  }
  
  private static class LineIndex {

    //TODO: use a proper spatial index
    List<Line> lines = new ArrayList<Line>();
    
    public void add(Line line) {
      lines.add(line);
    }
    
    public List<Line> query(Envelope queryEnv) {
      List<Line> result = new ArrayList<Line>();
      for (Line line : lines) {
        Envelope env = line.getEnvelope();
        if (queryEnv.intersects(env)) {
          result.add(line);
        }
      }
      return result;
    }
    
  }

}