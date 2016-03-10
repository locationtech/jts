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

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;


public class OverlayFunctions {
	public static Geometry intersection(Geometry a, Geometry b)		{		return a.intersection(b);	}
	public static Geometry union(Geometry a, Geometry b)					{		return a.union(b);	}
	public static Geometry symDifference(Geometry a, Geometry b)	{		return a.symDifference(b);	}
	public static Geometry difference(Geometry a, Geometry b)			{		return a.difference(b);	}
	public static Geometry differenceBA(Geometry a, Geometry b)		{		return b.difference(a);	}
  public static Geometry unaryUnion(Geometry a)                 {   return a.union(); }
  
  public static Geometry unionUsingGeometryCollection(Geometry a, Geometry b)                 
  {   
    Geometry gc = a.getFactory().createGeometryCollection(
        new Geometry[] { a, b});
    return gc.union(); 
  }

  public static Geometry clip(Geometry a, Geometry mask)
  {
    List geoms = new ArrayList();
    for (int i = 0; i < a.getNumGeometries(); i++) {
      Geometry clip = a.getGeometryN(i).intersection(mask);
      geoms.add(clip);
    }
    return FunctionsUtil.buildGeometry(geoms, a);
  }
  
  
}
