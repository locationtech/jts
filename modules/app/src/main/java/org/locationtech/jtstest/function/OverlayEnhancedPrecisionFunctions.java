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
import org.locationtech.jts.precision.EnhancedPrecisionOp;

public class OverlayEnhancedPrecisionFunctions {
	public static Geometry intersection(Geometry a, Geometry b)		{		return EnhancedPrecisionOp.intersection(a, b);	}
	public static Geometry union(Geometry a, Geometry b)					{		return EnhancedPrecisionOp.union(a, b);	}
	public static Geometry symDifference(Geometry a, Geometry b)	{		return EnhancedPrecisionOp.symDifference(a, b);	}
	public static Geometry difference(Geometry a, Geometry b)			{		return EnhancedPrecisionOp.difference(a, b);	}
	public static Geometry differenceBA(Geometry a, Geometry b)		{		return EnhancedPrecisionOp.difference(b, a);	}

}
