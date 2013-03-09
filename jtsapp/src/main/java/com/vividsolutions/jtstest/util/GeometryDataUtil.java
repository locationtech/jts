package com.vividsolutions.jtstest.util;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryDataUtil 
{
	public static void setComponentDataToIndex(Geometry geom)
	{
		for (int i = 0; i < geom.getNumGeometries(); i++) {
			Geometry comp = geom.getGeometryN(i);
			comp.setUserData("Component # " + i); 
		}
	}
}
