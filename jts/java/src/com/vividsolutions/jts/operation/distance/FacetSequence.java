package com.vividsolutions.jts.operation.distance;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;

/**
 * Represents a sequence of facets (points or line segments)
 * of a {@link Geometry}
 * specified by a subsequence of a {@link CoordinateSequence}.
 * 
 * @author Martin Davis
 *
 */
public class FacetSequence
{
  private CoordinateSequence pts;
  private int start;
  private int end;
  
  // temporary Coordinates to materialize points from the CoordinateSequence
  private Coordinate pt = new Coordinate();
  private Coordinate seqPt = new Coordinate();
  
  /**
   * Creates a new section based on a CoordinateSequence.
   * 
   * @param pts the sequence holding the points in the section
   * @param start the index of the start point
   * @param end the index of the end point + 1
   */
  public FacetSequence(CoordinateSequence pts, int start, int end) 
  {
    this.pts = pts;
    this.start = start;
    this.end = end;
  }
  
  /**
   * Creates a new sequence for a single point from a CoordinateSequence.
   * 
   * @param pts the sequence holding the points in the facet sequence
   * @param start the index of the point
   */
  public FacetSequence(CoordinateSequence pts, int start) 
  {
    this.pts = pts;
    this.start = start;
    this.end = start + 1;
  }
  
  public Envelope getEnvelope()
  {
    Envelope env = new Envelope();
    for (int i = start; i < end; i++) {
      env.expandToInclude(pts.getX(i), pts.getY(i));
    }
    return env;
  }
  
  public int size()
  {
    return end - start;
  }
  
  public Coordinate getCoordinate(int index)
  {
    return pts.getCoordinate(start + index);
  }
  
  public boolean isPoint()
  {
    return end - start == 1;
  }
  

  public double distance(FacetSequence facetSeq)
  {
    boolean isPoint = isPoint();
    boolean isPointOther = facetSeq.isPoint();
    
    if (isPoint && isPointOther) {
      pts.getCoordinate(start, pt);
      facetSeq.pts.getCoordinate(facetSeq.start, seqPt);
      return pt.distance(seqPt);
    }
    else if (isPoint) {
      pts.getCoordinate(start, pt);      
      return computePointLineDistance(pt, facetSeq);
    }
    else if (isPointOther) {
      facetSeq.pts.getCoordinate(facetSeq.start, seqPt);
      return computePointLineDistance(seqPt, this);
    }
    return computeLineLineDistance(facetSeq);
    
  }
  
  // temporary Coordinates to materialize points from the CoordinateSequence
  private Coordinate p0 = new Coordinate();
  private Coordinate p1 = new Coordinate();
  private Coordinate q0 = new Coordinate();
  private Coordinate q1 = new Coordinate();

  private double computeLineLineDistance(FacetSequence facetSeq)
  {
    // both linear - compute minimum segment-segment distance
    double minDistance = Double.MAX_VALUE;

    for (int i = start; i < end - 1; i++) {
      for (int j = facetSeq.start; j < facetSeq.end - 1; j++) {
        pts.getCoordinate(i, p0);
        pts.getCoordinate(i + 1, p1);
        facetSeq.pts.getCoordinate(j, q0);
        facetSeq.pts.getCoordinate(j + 1, q1);
        
        double dist = CGAlgorithms.distanceLineLine(p0, p1, q0, q1);
        if (dist == 0.0) 
          return 0.0;
        if (dist < minDistance) {
          minDistance = dist;
        }
      }
    }
    return minDistance;
  }

  private double computePointLineDistance(Coordinate pt, FacetSequence facetSeq) 
  {
    double minDistance = Double.MAX_VALUE;

    for (int i = facetSeq.start; i < facetSeq.end - 1; i++) {
      facetSeq.pts.getCoordinate(i, q0);
      facetSeq.pts.getCoordinate(i + 1, q1);
      double dist = CGAlgorithms.distancePointLine(pt, q0, q1);
      if (dist == 0.0) return 0.0;
      if (dist < minDistance) {
        minDistance = dist;
      }
    }
    return minDistance;
  }
  
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("LINESTRING ( ");
    Coordinate p = new Coordinate();
    for (int i = start; i < end; i++) {
      if (i > start)
        buf.append(", ");
      pts.getCoordinate(i, p);
      buf.append(p.x + " " + p.y);
    }
    buf.append(" )");
    return buf.toString();
  }
}
