/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.testbuilder.geom;

import org.locationtech.jts.geom.*;

/**
 * Finds a vertex or a point on a segment of a Geometry
 * which lies within a tolerance of a given point.
 * 
 * @author Martin Davis
 *
 */
public class GeometryPointLocater 
{
  public static GeometryLocation locateNonVertexPoint(Geometry geom, Coordinate testPt, double tolerance)
  {
    GeometryPointLocater finder = new GeometryPointLocater(geom);
    GeometryLocation geomLoc = finder.getLocation(testPt, false, tolerance);
    if (geomLoc == null) return null;
    if (geomLoc.isVertex()) return null;
    return geomLoc;
  }

  public static GeometryLocation locateVertex(Geometry geom, Coordinate testPt, double tolerance)
  {
    GeometryPointLocater finder = new GeometryPointLocater(geom);
    GeometryLocation geomLoc = finder.getLocation(testPt, true, tolerance);
    if (geomLoc == null) return null;
    if (geomLoc.isVertex()) return geomLoc;
    return null;
  }

  private Geometry geom;
  private Coordinate locationPt;
  private int segIndex = -1;
  private boolean isVertex = false;
  
  public GeometryPointLocater(Geometry geom) {
    this.geom = geom;
  }
  
  public GeometryLocation getLocation(Coordinate testPt, boolean vertexOnly, double tolerance)
  {
    NearestSegmentLocationFilter filter = new NearestSegmentLocationFilter(testPt, vertexOnly, tolerance);
    geom.apply(filter);
    
    locationPt = filter.getCoordinate();
    segIndex = filter.getIndex();
    isVertex = filter.isVertex();
    
    if (locationPt == null)
      return null;
    
    return new GeometryLocation(geom, 
        filter.getComponent(),
        filter.getIndex(),
        filter.isVertex(),
        filter.getCoordinate());
  }
  
  public int getIndex() { return segIndex; }
  
  public boolean isVertex() { return isVertex; }
  
  static class NearestSegmentLocationFilter implements GeometryComponentFilter
  {
    private double tolerance = 0.0;
    private Coordinate testPt;
    private boolean vertexOnly = false;
    
    private Geometry component = null;
    private int segIndex = -1;
    private Coordinate nearestPt = null;
    private boolean isVertex = false;
    
    private LineSegment seg = new LineSegment();
    
    public NearestSegmentLocationFilter(Coordinate testPt, boolean vertexOnly,  double tolerance)
    {
      this.testPt = testPt;
      this.tolerance = tolerance;
      this.vertexOnly = vertexOnly;
    }

    public void filter(Geometry geom)
    {
      if (! (geom instanceof LineString)) return;
      if (nearestPt != null)
        return;

      LineString lineStr = (LineString) geom;
      CoordinateSequence seq = lineStr.getCoordinateSequence();
      for (int i = 0; i < seq.size(); i++) {
        if (i != seq.size() - 1)
          checkSegment(lineStr, seq, i);
        else
          checkVertex(lineStr, seq, i);
        
        // check if done
        if (nearestPt != null) {
          //  found matching location!
          component = lineStr;
          break;
        }
      }
    }
      
    
    private void checkSegment(LineString lineStr, CoordinateSequence seq, int i)
    {
      Coordinate p0 = seq.getCoordinate(i);
      Coordinate p1 = seq.getCoordinate(i+1);
      
      // if point matches endpoint ==> vertex match
      double dist0 = p0.distance(testPt);
      double dist1 = p1.distance(testPt);
      if (dist0 < tolerance) {
        nearestPt = p0;
        segIndex = i;
        isVertex = true;
        return;
      }  
      else if (dist1 < tolerance) {
        nearestPt = p1;
        segIndex = i + 1;   
        isVertex = true;
        return;
      }
      
      // check closeness to segment (if allowing segments)
      if (vertexOnly) return;
      
      seg.p0 = p0;
			seg.p1 = p1;
			double segDist = seg.distance(testPt);
			if (segDist < tolerance) {
				nearestPt = seg.closestPoint(testPt);
				segIndex = i;
				isVertex = false;
			}
    }
    
    private void checkVertex(LineString lineStr, CoordinateSequence seq, int i)
    {
      Coordinate p0 = seq.getCoordinate(i);
      
      double dist0 = p0.distance(testPt);
      if (dist0 < tolerance) {
        nearestPt = p0;
        segIndex = i;
        isVertex = true;
      }  
    }
    
    public Geometry getComponent()
    {
      return component;
    }
    public Coordinate getCoordinate() 
    {
      return nearestPt;
    }
    public int getIndex() { return segIndex; }
    
    public boolean isVertex() { return isVertex; }
    
    public boolean isDone() { return nearestPt != null; }

    public boolean isGeometryChanged() { return false; }
  }

}
