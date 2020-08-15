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

import org.locationtech.jts.algorithm.distance.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.distance.*;

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

	public static Geometry discreteHausdorffDistanceLine(Geometry a, Geometry b)	
	{		
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    dist.distance();
    return a.getFactory().createLineString(dist.getCoordinates());
	}

	public static Geometry densifiedDiscreteHausdorffDistanceLine(Geometry a, Geometry b, double frac)	
	{		
    DiscreteHausdorffDistance hausDist = new DiscreteHausdorffDistance(a, b);
    hausDist.setDensifyFraction(frac);
    hausDist.distance();
    return a.getFactory().createLineString(hausDist.getCoordinates());
	}

	public static Geometry discreteOrientedHausdorffDistanceLine(Geometry a, Geometry b)	
	{		
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    dist.orientedDistance();
    return a.getFactory().createLineString(dist.getCoordinates());
	}

	public static double discreteHausdorffDistance(Geometry a, Geometry b)	
	{		
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    return dist.distance();
	}
	
	public static double discreteOrientedHausdorffDistance(Geometry a, Geometry b)	
	{		
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    return dist.orientedDistance();
	}
	
  public static double distanceIndexed(Geometry a, Geometry b) {
    return IndexedFacetDistance.distance(a, b);
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
