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

/**
 * Locates the paths to facets (vertices and segments) of 
 * a {@link Geometry} which are within a given tolerance
 * of a query point.
 * 
 *  
 * @author Martin Davis
 *
 */
public class FacetLocater 
{
	/**
	 * Creates a list containing all the vertex {@link GeometryLocation}s
	 * in the input collection.
	 * 
	 * @param locations the source collection
	 * @return a list of the vertex locations, if any
	 */
	public static List filterVertexLocations(Collection locations)
	{
		ArrayList vertexLocs = new ArrayList();
		for (Iterator i = locations.iterator(); i.hasNext(); ) {
			GeometryLocation loc = (GeometryLocation) i.next();
			if (loc.isVertex()) vertexLocs.add(loc);
		}
		return vertexLocs;
	}
	
  private Geometry parentGeom;
  private List locations = new ArrayList();
  private Coordinate queryPt;
  private double tolerance = 0.0; 
  
  public FacetLocater(Geometry parentGeom) {
    this.parentGeom = parentGeom;
  }
  
  public List getLocations(Coordinate queryPt, double tolerance)
  {
  	this.queryPt = queryPt;
  	this.tolerance = tolerance;
    findLocations(parentGeom, locations);
    return locations;
  }
  
  private void findLocations(Geometry geom, List locations)
  {
    findLocations(new Stack(), parentGeom, locations);
  }
    
  private void findLocations(Stack path, Geometry geom, List locations)
  {
  	if (geom instanceof GeometryCollection) {
  		for (int i = 0; i < geom.getNumGeometries(); i++ ) {
  			Geometry subGeom = geom.getGeometryN(i);
  			path.push(new Integer(i));
  			findLocations(path, subGeom, locations);
  			path.pop();
  		}
  	}
  	else if (geom instanceof Polygon) { 
  			findLocations(path, (Polygon) geom, locations);

  	}
  	else {
  		CoordinateSequence seq;
  	
  		if (geom instanceof LineString) {
   		 seq = ((LineString) geom).getCoordinateSequence();
  		}
  		else if (geom instanceof Point) {
  		 seq = ((Point) geom).getCoordinateSequence();
  		}
  		else {
  			throw new IllegalStateException("Unknown geometry type: " + geom.getClass().getName());
  		}
  		findLocations(path, geom, seq, locations);
  	}
  }
  
  private void findLocations(Stack path, Polygon poly, List locations)
  {
		path.push(new Integer(0));
		findLocations(path, 
				poly.getExteriorRing(),
				poly.getExteriorRing().getCoordinateSequence(), locations);
		path.pop();
		
		for (int i = 0; i < poly.getNumInteriorRing(); i++ ) {
			path.push(new Integer(i + 1));
			findLocations(path, 
					poly.getInteriorRingN(i), 
					poly.getInteriorRingN(i).getCoordinateSequence(), locations);
			path.pop();
		}
  }

  private void findLocations(Stack path, Geometry compGeom, CoordinateSequence seq, List locations)
  {
  	findVertexLocations(path, compGeom, seq, locations);
  	findSegmentLocations(path, compGeom, seq, locations);
  }

  private void findVertexLocations(Stack path, Geometry compGeom, CoordinateSequence seq, List locations)
  {
  	for (int i = 0; i < seq.size(); i++) {
      Coordinate p = seq.getCoordinate(i);
      double dist = p.distance(queryPt);
      if (dist <= tolerance) 
      	locations.add(new GeometryLocation(parentGeom, compGeom, toIntArray(path), i, true, p));
  	}
  }

  private void findSegmentLocations(Stack path, Geometry compGeom, CoordinateSequence seq, List locations)
  {
  	LineSegment seg = new LineSegment();
  	for (int i = 0; i < seq.size() - 1; i++) {
      seg.p0 = seq.getCoordinate(i);
      seg.p1 = seq.getCoordinate(i+1);
      double dist = seg.distance(queryPt);
      if (dist <= tolerance) 
      	locations.add(new GeometryLocation(parentGeom, compGeom, toIntArray(path), i, false, seg.p0));
  	}
  }

	public static int[] toIntArray(Vector path)
	{
		int[] index = new int[path.size()];
		int i = 0;
		for (Iterator it = path.iterator(); it.hasNext(); ) {
			Integer pathIndex = (Integer) it.next();
			index[i++] = pathIndex.intValue();
		}
		return index;
	}

}
