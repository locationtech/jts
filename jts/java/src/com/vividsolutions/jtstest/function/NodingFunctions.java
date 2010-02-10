package com.vividsolutions.jtstest.function;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.noding.snapround.GeometryNoder;
import com.vividsolutions.jts.precision.SimpleGeometryPrecisionReducer;

public class NodingFunctions 
{

	public static Geometry nodeWithPointwisePrecision(Geometry geom, double scaleFactor)
	{
		PrecisionModel pm = new PrecisionModel(scaleFactor);

		Geometry roundedGeom = SimpleGeometryPrecisionReducer.reduce(geom, pm);

		List geomList = new ArrayList();
		geomList.add(roundedGeom);
		
		GeometryNoder noder = new GeometryNoder(pm);
		List lines = noder.node(geomList);
		
    return FunctionsUtil.getFactoryOrDefault(geom).buildGeometry(lines);
	}
	
}
