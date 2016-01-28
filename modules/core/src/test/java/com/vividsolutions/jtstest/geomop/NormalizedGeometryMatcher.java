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

package com.vividsolutions.jtstest.geomop;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testrunner.GeometryResult;

public class NormalizedGeometryMatcher
implements GeometryMatcher
{
	private double tolerance;
	
	public NormalizedGeometryMatcher()
	{
		
	}
	
	public void setTolerance(double tolerance)
	{
		this.tolerance = tolerance;
	}
	
	public boolean match(Geometry a, Geometry b)
	{
    Geometry aClone = (Geometry)a.clone();
    Geometry bClone =(Geometry) b.clone();
    aClone.normalize();
    bClone.normalize();
    return aClone.equalsExact(bClone, tolerance);
	}

}
