/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.algorithm.distance.*;
import com.vividsolutions.jts.operation.distance.*;
import com.vividsolutions.jts.geom.*;

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
	
}
