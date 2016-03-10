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
package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.locationtech.jts.simplify.VWSimplifier;

public class SimplificationFunctions {
	public static Geometry simplifyDP(Geometry g, double distance)	
	{		return DouglasPeuckerSimplifier.simplify(g, distance);	}

  public static Geometry simplifyTP(Geometry g, double distance)  
  {   return TopologyPreservingSimplifier.simplify(g, distance);  }
  
  public static Geometry simplifyVW(Geometry g, double distance)  
  {   return VWSimplifier.simplify(g, distance);  }


}
