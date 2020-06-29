/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.operation.distance;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;

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
  private Geometry geom = null;
  private CoordinateSequence pts;
  private int start;
  private int end;
  
  /**
   * Creates a new sequence of facets based on a {@link CoordinateSequence}
   * contained in the given {@link Geometry}.
   * 
   * @param geom the geometry containing the facets 
   * @param pts the sequence containing the facet points
   * @param start the index of the start point
   * @param end the index of the end point + 1
   */
  public FacetSequence(Geometry geom, CoordinateSequence pts, int start, int end) 
  {
    this.geom = geom;
    this.pts = pts;
    this.start = start;
    this.end = end;
  } 
  
  /**
   * Creates a new sequence of facets based on a {@link CoordinateSequence}.
   * 
   * @param pts the sequence containing the facet points
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
   * Creates a new sequence for a single point from a {@link CoordinateSequence}.
   * 
   * @param pts the sequence containing the facet point
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
  
  /**
   * Computes the distance between this and another
   * <tt>FacetSequence</tt>.
   * 
   * @param facetSeq the sequence to compute the distance to
   * @return the minimum distance between the sequences
   */
  public double distance(FacetSequence facetSeq)
  {
    boolean isPoint = isPoint();
    boolean isPointOther = facetSeq.isPoint();
    double distance;
    
    if (isPoint && isPointOther) {
      Coordinate pt = pts.getCoordinate(start);
      Coordinate seqPt = facetSeq.pts.getCoordinate(facetSeq.start);
      distance = pt.distance(seqPt);
    }
    else if (isPoint) {
      Coordinate pt = pts.getCoordinate(start);      
      distance = computeDistancePointLine(pt, facetSeq, null);
    }
    else if (isPointOther) {
      Coordinate seqPt = facetSeq.pts.getCoordinate(facetSeq.start);
      distance = computeDistancePointLine(seqPt, this, null);
    }
    else {
      distance = computeDistanceLineLine(facetSeq, null);
    }
    return distance;
  }
  
  /**
   * Computes the locations of the nearest points between this sequence
   * and another sequence.
   * The locations are presented in the same order as the input sequences.
   *
   * @return a pair of {@link GeometryLocation}s for the nearest points
   */
  public GeometryLocation[] nearestLocations(FacetSequence facetSeq)
  {
    boolean isPoint = isPoint();
    boolean isPointOther = facetSeq.isPoint();
    GeometryLocation[] locs = new GeometryLocation[2];
    
    if (isPoint && isPointOther) {
      Coordinate pt = pts.getCoordinate(start);
      Coordinate seqPt = facetSeq.pts.getCoordinate(facetSeq.start);
      locs[0] = new  GeometryLocation(geom, start, new Coordinate(pt));
      locs[1] = new  GeometryLocation(facetSeq.geom, facetSeq.start, new Coordinate(seqPt));
    }
    else if (isPoint) {
      Coordinate pt = pts.getCoordinate(start);      
      computeDistancePointLine(pt, facetSeq, locs);
    }
    else if (isPointOther) {
      Coordinate seqPt = facetSeq.pts.getCoordinate(facetSeq.start);
      computeDistancePointLine(seqPt, this, locs);
      // unflip the locations
      GeometryLocation tmp = locs[0];
      locs[0] = locs[1];
      locs[1] = tmp;
    }
    else {
      computeDistanceLineLine(facetSeq, locs);
    }
    return locs;    
  }

  private double computeDistanceLineLine(FacetSequence facetSeq, GeometryLocation[] locs)
  {
    // both linear - compute minimum segment-segment distance
    double minDistance = Double.MAX_VALUE;

    for (int i = start; i < end - 1; i++) {
      Coordinate p0 = pts.getCoordinate(i);
      Coordinate p1 = pts.getCoordinate(i + 1);
      for (int j = facetSeq.start; j < facetSeq.end - 1; j++) {
        Coordinate q0 = facetSeq.pts.getCoordinate(j);
        Coordinate q1 = facetSeq.pts.getCoordinate(j + 1);
        
        double dist = Distance.segmentToSegment(p0, p1, q0, q1);
        if (dist < minDistance) {
          minDistance = dist;
          if (locs != null) updateNearestLocationsLineLine(i, p0, p1, facetSeq, j, q0, q1, locs);
          if (minDistance <= 0.0) return minDistance;
        }
      }
    }
    return minDistance;
  }

  private void updateNearestLocationsLineLine(int i, Coordinate p0, Coordinate p1, FacetSequence facetSeq, int j,
      Coordinate q0, Coordinate q1, GeometryLocation[] locs) {
    LineSegment seg0 = new LineSegment(p0, p1);
    LineSegment seg1 = new LineSegment(q0, q1);
    Coordinate[] closestPt = seg0.closestPoints(seg1);
    locs[0] = new GeometryLocation(geom, i, new Coordinate(closestPt[0]));
    locs[1] = new GeometryLocation(facetSeq.geom, j, new Coordinate(closestPt[1]));    
  }
  
  private double computeDistancePointLine(Coordinate pt, FacetSequence facetSeq, GeometryLocation[] locs) 
  {
    double minDistance = Double.MAX_VALUE;

    for (int i = facetSeq.start; i < facetSeq.end - 1; i++) {
      Coordinate q0 = facetSeq.pts.getCoordinate(i);
      Coordinate q1 = facetSeq.pts.getCoordinate(i + 1);
      double dist = Distance.pointToSegment(pt, q0, q1);
      if (dist < minDistance) {
        minDistance = dist;
        if (locs != null) updateNearestLocationsPointLine(pt, facetSeq, i, q0, q1, locs);
        if (minDistance <= 0.0) return minDistance;
      }
    }
    return minDistance;
  }
  
  private void updateNearestLocationsPointLine(Coordinate pt, 
      FacetSequence facetSeq, int i, Coordinate q0, Coordinate q1, 
      GeometryLocation[] locs) {
    locs[0] = new GeometryLocation(geom, start, new Coordinate(pt));
    LineSegment seg = new LineSegment(q0, q1);
    Coordinate segClosestPoint = seg.closestPoint(pt);
    locs[1] = new  GeometryLocation(facetSeq.geom, i, new Coordinate(segClosestPoint));
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
