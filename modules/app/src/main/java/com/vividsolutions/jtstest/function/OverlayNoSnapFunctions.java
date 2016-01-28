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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.overlay.OverlayOp;

public class OverlayNoSnapFunctions {
	public static Geometry intersection(Geometry a, Geometry b)		{		return OverlayOp.overlayOp(a, b, OverlayOp.INTERSECTION);	}
	public static Geometry union(Geometry a, Geometry b)					{		return OverlayOp.overlayOp(a, b, OverlayOp.UNION);	}
	public static Geometry symDifference(Geometry a, Geometry b)	{		return OverlayOp.overlayOp(a, b, OverlayOp.SYMDIFFERENCE);	}
	public static Geometry difference(Geometry a, Geometry b)			{		return OverlayOp.overlayOp(a, b, OverlayOp.DIFFERENCE);	}
	public static Geometry differenceBA(Geometry a, Geometry b)		{		return OverlayOp.overlayOp(b, a, OverlayOp.DIFFERENCE);	}

}
