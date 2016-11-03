/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.precision;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;

/**
 * Computes the minimum clearance of a geometry or 
 * set of geometries.
 * <p>
 * The <b>Minimum Clearance</b> is a measure of
 * what magnitude of perturbation of its vertices can be tolerated
 * by a geometry before it becomes topologically invalid.
 * <p>
 * This class uses an inefficient O(N^2) scan.  
 * It is primarily for testing purposes.
 * 
 * 
 * @see MinimumClearance
 * @author Martin Davis
 *
 */
public class SimpleMinimumClearance 
{
  public static double getDistance(Geometry g)
  {
    SimpleMinimumClearance rp = new SimpleMinimumClearance(g);
    return rp.getDistance();
  }
  
  public static Geometry getLine(Geometry g)
  {
    SimpleMinimumClearance rp = new SimpleMinimumClearance(g);
    return rp.getLine();
  }
  
  private Geometry inputGeom;
  private double minClearance;
  private Coordinate[] minClearancePts;
  
  public SimpleMinimumClearance(Geometry geom)
  {
    inputGeom = geom;
  }
  
  public double getDistance()
  {
    compute();
    return minClearance;
  }
  
  public LineString getLine()
  {
    compute();
    return inputGeom.getFactory().createLineString(minClearancePts);
  }
  
  private void compute()
  {
    if (minClearancePts != null) return;
    minClearancePts = new Coordinate[2];
    minClearance = Double.MAX_VALUE;
    inputGeom.apply(new VertexCoordinateFilter());
  }
  
  private void updateClearance(double candidateValue, Coordinate p0, Coordinate p1)
  {
    if (candidateValue < minClearance) {
      minClearance = candidateValue;
      minClearancePts[0] = new Coordinate(p0);
      minClearancePts[1] = new Coordinate(p1);
    }
  }
  
  private void updateClearance(double candidateValue, Coordinate p, 
      Coordinate seg0, Coordinate seg1)
  {
    if (candidateValue < minClearance) {
      minClearance = candidateValue;
      minClearancePts[0] = new Coordinate(p);
      LineSegment seg = new LineSegment(seg0, seg1);
      minClearancePts[1] = new Coordinate(seg.closestPoint(p));
    }
  }
  
  private class VertexCoordinateFilter 
  implements CoordinateFilter
  {
    public VertexCoordinateFilter()
    {
      
    }
    
    public void filter(Coordinate coord) {
      inputGeom.apply(new ComputeMCCoordinateSequenceFilter(coord));
    }
  }
  
  private class ComputeMCCoordinateSequenceFilter 
  implements CoordinateSequenceFilter 
  {
    private Coordinate queryPt;
    
    public ComputeMCCoordinateSequenceFilter(Coordinate queryPt)
    {
      this.queryPt = queryPt;
    }
    public void filter(CoordinateSequence seq, int i) {
      // compare to vertex
      checkVertexDistance(seq.getCoordinate(i));
      
      // compare to segment, if this is one
      if (i > 0) {
        checkSegmentDistance(seq.getCoordinate(i - 1), seq.getCoordinate(i));
      }
    }
    
    private void checkVertexDistance(Coordinate vertex)
    {
      double vertexDist = vertex.distance(queryPt);
      if (vertexDist > 0) {
        updateClearance(vertexDist, queryPt, vertex);
      }
    }
    
    private void checkSegmentDistance(Coordinate seg0, Coordinate seg1)
    {
        if (queryPt.equals2D(seg0) || queryPt.equals2D(seg1))
          return;
        double segDist = CGAlgorithms.distancePointLine(queryPt, seg1, seg0);
        if (segDist > 0) 
          updateClearance(segDist, queryPt, seg1, seg0);
    }
    
    public boolean isDone() {
      return false;
    }
    
    public boolean isGeometryChanged() {
      return false;
    }
    
  }
}
