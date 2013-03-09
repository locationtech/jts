/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
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
