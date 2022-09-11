package org.locationtech.jts.coverage;

import java.util.PriorityQueue;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.simplify.Corner;
import org.locationtech.jts.simplify.LinkedLine;

class VWTPSimplifier {

  public static MultiLineString simplify(MultiLineString lines, double tolerance) {
    VWTPSimplifier simp = new VWTPSimplifier(lines, tolerance);
    MultiLineString linesSimp = (MultiLineString) simp.simplify();
    return linesSimp;
  }
  
  private MultiLineString input;
  private double tolerance;
  private GeometryFactory geomFactory;

  VWTPSimplifier(MultiLineString lines, double tolerance) {
    this.input = lines;
    this.tolerance = tolerance;
    geomFactory = input.getFactory();
  }
  
  public Geometry simplify() {
    LineString[] result = new LineString[input.getNumGeometries()];
    for (int i = 0 ; i < result.length; i++) {
      LineString line = (LineString) input.getGeometryN(i);
      Line lineSimp = new Line(line.getCoordinates(), tolerance);
      Coordinate[] ptsSimp = lineSimp.simplify();
      result[i] = geomFactory.createLineString(ptsSimp);
    }
    return geomFactory.createMultiLineString(result);
  }
  
  private static class Line {
    private double tolerance;
    private LinkedLine line;
    private int minEdgeSize;

    private PriorityQueue<Corner> cornerQueue;
    
    Line(Coordinate[] pts, double tolerance) {
      this.tolerance = tolerance;
      line = new LinkedLine(pts);
      minEdgeSize = line.isRing() ? 3 : 2;
      
      //vertexIndex = new VertexSequencePackedRtree(ring);
      //-- remove duplicate final vertex
      //vertexIndex.remove(ring.length-1);
      
      cornerQueue = new PriorityQueue<Corner>();
      for (int i = 1; i < line.size() - 1; i++) {
        addCorner(i, cornerQueue);
      }
    }

    private Coordinate getCoordinate(int index) {
      return line.getCoordinate(index);
    }
  
    private void addCorner(int i, PriorityQueue<Corner> cornerQueue) {
      if (! line.isCorner(i))
        return;
      Corner corner = new Corner(i, 
          line.prev(i),
          line.next(i),
          area(line, i));
      cornerQueue.add(corner);
    }
    
    private static double area(LinkedLine edge, int index) {
      Coordinate pp = edge.prevCoordinate(index);
      Coordinate p = edge.getCoordinate(index);
      Coordinate pn = edge.nextCoordinate(index);
      return Triangle.area(pp, p, pn);
    }
    
    private Coordinate[] simplify() {        
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
        // (isAtTarget(corner))
        //  return;
        removeCorner(corner, cornerQueue);
      }
      return line.getCoordinates();
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
      
      //-- potentially add the new corners created
      addCorner(prev, cornerQueue);
      addCorner(next, cornerQueue);
    }

  }

}