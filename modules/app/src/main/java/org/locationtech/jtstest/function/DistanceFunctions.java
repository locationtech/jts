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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

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
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    return dist.distance();
  }
  
  public static Geometry hausdorffDistanceLine(Geometry a, Geometry b)  
  {   
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    dist.distance();
    return a.getFactory().createLineString(dist.getCoordinates());
  }

	public static Geometry hausdorffDistanceLineDensified(Geometry a, Geometry b, double frac)	
	{		
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    dist.setDensifyFraction(frac);
    dist.distance();
    return a.getFactory().createLineString(dist.getCoordinates());
	}

	public static Geometry orientedHausdorffDistanceLine(Geometry a, Geometry b)	
	{		
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    dist.orientedDistance();
    return a.getFactory().createLineString(dist.getCoordinates());
	}

	public static double orientedHausdorffDistance(Geometry a, Geometry b)	
	{		
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    return dist.orientedDistance();
	}
	
  public static Geometry orientedHausdorffDistanceLineDensified(Geometry a, Geometry b, double frac)  
  {   
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    dist.setDensifyFraction(frac);
    dist.orientedDistance();
    return a.getFactory().createLineString(dist.getCoordinates());
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
}
