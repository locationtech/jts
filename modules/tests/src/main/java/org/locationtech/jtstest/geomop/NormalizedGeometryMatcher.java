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

package org.locationtech.jtstest.geomop;

import org.locationtech.jts.geom.*;
import org.locationtech.jtstest.testrunner.GeometryResult;


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
