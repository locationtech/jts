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
import org.locationtech.jts.precision.CommonBitsOp;
import org.locationtech.jts.precision.EnhancedPrecisionOp;

public class OverlayCommonBitsRemovedFunctions {
	public static Geometry intersection(Geometry a, Geometry b)		{		return op().intersection(a, b);	}
	public static Geometry union(Geometry a, Geometry b)					{		return op().union(a, b);	}
	public static Geometry symDifference(Geometry a, Geometry b)	{		return op().symDifference(a, b);	}
	public static Geometry difference(Geometry a, Geometry b)			{		return op().difference(a, b);	}
	public static Geometry differenceBA(Geometry a, Geometry b)		{		return op().difference(b, a);	}

	private static CommonBitsOp op() { return new CommonBitsOp(true); }
}
