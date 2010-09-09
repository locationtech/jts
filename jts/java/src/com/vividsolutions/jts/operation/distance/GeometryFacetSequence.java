package com.vividsolutions.jts.operation.distance;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;

/**
 * Represents a sequence of facets (points or line segments)
 * of a {@link Geometry}
 * specified by a subsection of a {@link CoordinateSequence}.
 * 
 * @author Martin Davis
 *
 */
public class GeometryFacetSequence
{
  private CoordinateSequence pts;
  private int start;
  private int end;
  
  // temporary Coordinates to materialize points from the CoordinateSequence
  private Coordinate pt = new Coordinate();
  private Coordinate sectPt = new Coordinate();
  
  /**
   * Creates a new section based on a CoordinateSequence.
   * 
   * @param pts the sequence holding the points in the section
   * @param start the index of the start point
   * @param end the index of the end point + 1
   */
  public GeometryFacetSequence(CoordinateSequence pts, int start, int end) 
  {
    this.pts = pts;
    this.start = start;
    this.end = end;
//  if (end - start == 1)  
//  System.out.println(end - start);
//  if (end == start)  
//  System.out.println(end - start);
  }
  
  /**
   * Creates a new sequence for a single point from a CoordinateSequence.
   * 
   * @param pts the sequence holding the points in the facet sequence
   * @param start the index of the point
   */
  public GeometryFacetSequence(CoordinateSequence pts, int start) 
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
  
  public boolean isPoint()
  {
    return end - start == 1;
  }
  

  public double distance(GeometryFacetSequence sect)
  {
    boolean isPoint = isPoint();
    boolean isPointOther = sect.isPoint();
    
    if (isPoint && isPointOther) {
      pts.getCoordinate(start, pt);
      sect.pts.getCoordinate(sect.start, sectPt);
      return pt.distance(sectPt);
    }
    else if (isPoint) {
      pts.getCoordinate(start, pt);      
      return computePointLineDistance(pt, sect);
    }
    else if (isPointOther) {
      sect.pts.getCoordinate(sect.start, sectPt);
      return computePointLineDistance(sectPt, this);
    }
    return computeLineLineDistance(sect);
    
  }
  
  // temporary Coordinates to materialize points from the CoordinateSequence
  private Coordinate p0 = new Coordinate();
  private Coordinate p1 = new Coordinate();
  private Coordinate q0 = new Coordinate();
  private Coordinate q1 = new Coordinate();

  private double computeLineLineDistance(GeometryFacetSequence sect)
  {
    // both linear - compute minimum segment-segment distance
    double minDistance = Double.MAX_VALUE;

    for (int i = start; i < end - 1; i++) {
      for (int j = sect.start; j < sect.end - 1; j++) {
        pts.getCoordinate(i, p0);
        pts.getCoordinate(i + 1, p1);
        sect.pts.getCoordinate(j, q0);
        sect.pts.getCoordinate(j + 1, q1);
        
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

  private double computePointLineDistance(Coordinate pt, GeometryFacetSequence sect) 
  {
    double minDistance = Double.MAX_VALUE;

    for (int i = sect.start; i < sect.end - 1; i++) {
      sect.pts.getCoordinate(i, q0);
      sect.pts.getCoordinate(i + 1, q1);
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
