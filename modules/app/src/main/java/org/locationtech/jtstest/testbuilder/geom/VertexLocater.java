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

import java.util.*;

import org.locationtech.jts.geom.*;

public class VertexLocater 
{
  public static Coordinate locateVertex(Geometry geom, Coordinate testPt, double tolerance)
  {
    VertexLocater finder = new VertexLocater(geom);
    return finder.getVertex(testPt, tolerance);
  }

  private Geometry geom;
  private Coordinate vertexPt;
  private int vertexIndex = -1;
  
  public VertexLocater(Geometry geom) {
    this.geom = geom;
  }
  
  public Coordinate getVertex(Coordinate testPt, double tolerance)
  {
  	NearestVertexFilter filter = new NearestVertexFilter(testPt, tolerance);
    geom.apply(filter);
    vertexPt = filter.getVertex();
    vertexIndex = filter.getIndex();
    return vertexPt;
  }
  
  public int getIndex() { return vertexIndex; }
 
  public List getLocations(Coordinate testPt, double tolerance)
  {
  	NearVerticesFilter filter = new NearVerticesFilter(testPt, tolerance);
    geom.apply(filter);
    return filter.getLocations();
  }
  

  static class NearestVertexFilter implements CoordinateSequenceFilter
  {
    private double tolerance = 0.0;
    private Coordinate basePt;
    private Coordinate nearestPt = null;
    private int vertexIndex = -1;
    
    public NearestVertexFilter(Coordinate basePt, double tolerance)
    {
      this.basePt = basePt;
      this.tolerance = tolerance;
    }

    public void filter(CoordinateSequence seq, int i)
    {
      Coordinate p = seq.getCoordinate(i);
      double dist = p.distance(basePt);
      if (dist > tolerance) return;
      
      nearestPt = p;
      vertexIndex = i;

    }
    
    public Coordinate getVertex() 
    {
      return nearestPt;
    }
    public int getIndex() { return vertexIndex; }
    
    public boolean isDone() { return nearestPt != null; }

    public boolean isGeometryChanged() { return false; }
  }
  static class NearVerticesFilter implements CoordinateSequenceFilter
  {
    private double tolerance = 0.0;
    private Coordinate queryPt;
    private List locations = new ArrayList();
    
    public NearVerticesFilter(Coordinate queryPt, double tolerance)
    {
      this.queryPt = queryPt;
      this.tolerance = tolerance;
    }

    public void filter(CoordinateSequence seq, int i)
    {
      Coordinate p = seq.getCoordinate(i);
      double dist = p.distance(queryPt);
      if (dist > tolerance) return;
      
      locations.add(new Location(p, i));

    }
    
    public List getLocations()
    {
    	return locations;
    }
    
    public boolean isDone()
    { 
    	// evaluate all points
    	return false; 
    }

    public boolean isGeometryChanged() { return false; }
  }

  public static class Location
  {
  	private Coordinate pt;
  	private int[] index;
  	
  	Location(Coordinate pt, int index)
  	{
  		this.pt = pt;
  		this.index = new int[1];
  		this.index[0] = index;
  	}
  	
  	public Coordinate getCoordinate()
  	{
  		return pt;
  	}
  	
  	public int[] getIndices() { return index; }
  }
}
