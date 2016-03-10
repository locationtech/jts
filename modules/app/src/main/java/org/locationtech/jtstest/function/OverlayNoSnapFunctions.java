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
import org.locationtech.jts.operation.overlay.OverlayOp;

public class OverlayNoSnapFunctions {
	public static Geometry intersection(Geometry a, Geometry b)		{		return OverlayOp.overlayOp(a, b, OverlayOp.INTERSECTION);	}
	public static Geometry union(Geometry a, Geometry b)					{		return OverlayOp.overlayOp(a, b, OverlayOp.UNION);	}
	public static Geometry symDifference(Geometry a, Geometry b)	{		return OverlayOp.overlayOp(a, b, OverlayOp.SYMDIFFERENCE);	}
	public static Geometry difference(Geometry a, Geometry b)			{		return OverlayOp.overlayOp(a, b, OverlayOp.DIFFERENCE);	}
	public static Geometry differenceBA(Geometry a, Geometry b)		{		return OverlayOp.overlayOp(b, a, OverlayOp.DIFFERENCE);	}

}
