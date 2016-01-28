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

package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.precision.*;

public class PrecisionFunctions 
{
	
	public static Geometry reducePrecisionPointwise(Geometry geom, double scaleFactor)
	{
		PrecisionModel pm = new PrecisionModel(scaleFactor);
		Geometry reducedGeom = GeometryPrecisionReducer.reducePointwise(geom, pm);
		return reducedGeom;
	}
	
	public static Geometry reducePrecision(Geometry geom, double scaleFactor)
	{
		PrecisionModel pm = new PrecisionModel(scaleFactor);
		Geometry reducedGeom = GeometryPrecisionReducer.reduce(geom, pm);
		return reducedGeom;
	}
	
  public static Geometry minClearanceLine(Geometry g)
  {
    return MinimumClearance.getLine(g);
  }
  
  public static double minClearance(Geometry g)
  {
    return MinimumClearance.getDistance(g);
  }
  
  public static Geometry minClearanceSimpleLine(Geometry g)
  {
    return SimpleMinimumClearance.getLine(g);
  }
  
  public static double minClearanceSimple(Geometry g)
  {
    return SimpleMinimumClearance.getDistance(g);
  }
}
