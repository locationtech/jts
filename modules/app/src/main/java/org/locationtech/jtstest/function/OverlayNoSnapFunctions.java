/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionStrategy;

public class OverlayNoSnapFunctions {
	public static Geometry intersection(Geometry a, Geometry b)		{		return OverlayOp.overlayOp(a, b, OverlayOp.INTERSECTION);	}
	public static Geometry union(Geometry a, Geometry b)					{		return OverlayOp.overlayOp(a, b, OverlayOp.UNION);	}
	public static Geometry symDifference(Geometry a, Geometry b)	{		return OverlayOp.overlayOp(a, b, OverlayOp.SYMDIFFERENCE);	}
	public static Geometry difference(Geometry a, Geometry b)			{		return OverlayOp.overlayOp(a, b, OverlayOp.DIFFERENCE);	}
	public static Geometry differenceBA(Geometry a, Geometry b)		{		return OverlayOp.overlayOp(b, a, OverlayOp.DIFFERENCE);	}

	 public static Geometry unaryUnion(Geometry a) {
	    UnionStrategy unionSRFun = new UnionStrategy() {

	      public Geometry union(Geometry g0, Geometry g1) {
	         return OverlayOp.overlayOp(g0, g1, OverlayOp.UNION );
	      }

	      @Override
	      public boolean isFloatingPrecision() {
	        return true;
	      }
	      
	    };
	    UnaryUnionOp op = new UnaryUnionOp(a);
	    op.setUnionFunction(unionSRFun);
	    return op.union();
	  }
}
