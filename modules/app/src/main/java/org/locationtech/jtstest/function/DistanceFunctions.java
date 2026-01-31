/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import org.locationtech.jts.algorithm.distance.DiscreteFrechetDistance;
import org.locationtech.jts.algorithm.distance.DiscreteHausdorffDistance;
import org.locationtech.jts.algorithm.distance.DirectedHausdorffDistance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;
import org.locationtech.jtstest.geomfunction.Metadata;

public class DistanceFunctions {
  public static double distance(Geometry a, Geometry b) {
    return a.distance(b);
  }

  public static boolean isWithinDistance(Geometry a, Geometry b, double dist) {
    return a.isWithinDistance(b, dist);
  }

  public static Geometry nearestPoints(Geometry a, Geometry b) {
    Coordinate[] pts = DistanceOp.nearestPoints(a, b);
    return a.getFactory().createLineString(pts);
  }

  public static double frechetDistance(Geometry a, Geometry b)  
  {   
    return DiscreteFrechetDistance.distance(a, b);
  }

  public static Geometry frechetDistanceLine(Geometry a, Geometry b)  
  {   
    DiscreteFrechetDistance dist = new DiscreteFrechetDistance(a, b);
    return a.getFactory().createLineString(dist.getCoordinates());
  }

  public static double hausdorffDistance(Geometry a, Geometry b)  
  {   
    return DiscreteHausdorffDistance.distance(a, b);
  }
  
  @Metadata(description="Hausdorff distance between A and B")
  public static Geometry hausdorffDistanceLine(Geometry a, Geometry b)  
  {   
    return DiscreteHausdorffDistance.distanceLine(a, b);
  }

  @Metadata(description="Hausdorff distance between A and B, densified")
	public static Geometry hausdorffDistanceLineDensify(Geometry a, Geometry b, 
      @Metadata(title="Densify fraction")
	    double frac)	
	{		
    return DiscreteHausdorffDistance.distanceLine(a, b, frac);
	}

  @Metadata(description="Oriented Hausdorff distance from A to B")
  public static Geometry orientedHausdorffDistanceLine(Geometry a, Geometry b)  
  {   
    return DiscreteHausdorffDistance.orientedDistanceLine(a, b);
  }

  @Metadata(description="Oriented Hausdorff distance from A to B")
  public static Geometry clippedOrientedHausdorffDistanceLine(Geometry a, Geometry b)  
  {   
    //TODO: would this be more efficient done as part of DiscreteHausdorffDistance?
    Geometry clippedLine = LinearReferencingFunctions.project(a, b);
    return DiscreteHausdorffDistance.orientedDistanceLine(clippedLine, b);
  }

  @Metadata(description="Oriented Hausdorff distance from A to B")
	public static double orientedHausdorffDistance(Geometry a, Geometry b)	
	{		
    return DiscreteHausdorffDistance.orientedDistance(a, b);
	}
	
  @Metadata(description="Oriented Hausdorff distance from A to B, densified")
  public static Geometry orientedHausdorffDistanceLineDensify(Geometry a, Geometry b, 
      @Metadata(title="Densify fraction")
      double frac)  
  {   
    return DiscreteHausdorffDistance.orientedDistanceLine(a, b, frac);
  }

  public static double distanceIndexed(Geometry a, Geometry b) {
    return IndexedFacetDistance.distance(a, b);
  }
  
  public static boolean isWithinDistanceIndexed(Geometry a, Geometry b, double distance) {
    return IndexedFacetDistance.isWithinDistance(a, b, distance);
  }
  
  public static Geometry nearestPointsIndexed(Geometry a, Geometry b) {
    Coordinate[] pts =  IndexedFacetDistance.nearestPoints(a, b);
    return a.getFactory().createLineString(pts);
  }
  
  public static Geometry nearestPointsIndexedEachB(Geometry a, Geometry b) {
    IndexedFacetDistance ifd = new IndexedFacetDistance(a);
    
    int n = b.getNumGeometries();
    LineString[] lines = new LineString[n];
    for (int i = 0; i < n; i++) {
      Coordinate[] pts =  ifd.nearestPoints(b.getGeometryN(i));
      lines[i] = a.getFactory().createLineString(pts);
    }
    
    return a.getFactory().createMultiLineString(lines);
  }
  
  @Metadata(description="Directed Hausdorff distance from A to B, up to tolerance")
  public static double dhd(Geometry a, Geometry b, 
      @Metadata(title="Distance tolerance")
      double distTol)  
  {   
    return DirectedHausdorffDistance.distance(a, b, distTol);
  }
  
  @Metadata(description="Directed Hausdorff distance from A to B, up to tolerance")
  public static Geometry dhdLine(Geometry a, Geometry b, 
      @Metadata(title="Distance tolerance")
      double distTol)  
  {   
    return DirectedHausdorffDistance.distanceLine(a, b, distTol);
  }
  
  @Metadata(description="Hausdorff distance between A and B, up to tolerance")
  public static Geometry hdLine(Geometry a, Geometry b, 
      @Metadata(title="Distance tolerance")
      double distTol)  
  {   
    return DirectedHausdorffDistance.hausdorffDistanceLine(a, b, distTol);
  }
}
