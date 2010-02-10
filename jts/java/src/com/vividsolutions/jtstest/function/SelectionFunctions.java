package com.vividsolutions.jtstest.function;

import java.util.*;
import com.vividsolutions.jts.geom.*;


public class SelectionFunctions 
{
	public static Geometry intersects(Geometry a, Geometry mask)
	{
		List selected = new ArrayList();
		for (int i = 0; i < a.getNumGeometries(); i++ ) {
			Geometry g = a.getGeometryN(i);
			if (mask.intersects(g)) {
				selected.add(g);
			}
		}
		return a.getFactory().buildGeometry(selected);
	}
}
