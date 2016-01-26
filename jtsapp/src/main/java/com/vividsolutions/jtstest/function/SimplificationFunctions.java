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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import com.vividsolutions.jts.simplify.VWSimplifier;

public class SimplificationFunctions {
	public static Geometry simplifyDP(Geometry g, double distance)	
	{		return DouglasPeuckerSimplifier.simplify(g, distance);	}

  public static Geometry simplifyTP(Geometry g, double distance)  
  {   return TopologyPreservingSimplifier.simplify(g, distance);  }
  
  public static Geometry simplifyVW(Geometry g, double distance)  
  {   return VWSimplifier.simplify(g, distance);  }


}
