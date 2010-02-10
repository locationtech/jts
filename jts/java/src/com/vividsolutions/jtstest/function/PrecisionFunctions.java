package com.vividsolutions.jtstest.function;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.noding.snapround.GeometryNoder;
import com.vividsolutions.jts.precision.SimpleGeometryPrecisionReducer;

public class PrecisionFunctions 
{

	public static Geometry reducePrecisionPointwise(Geometry geom, double scaleFactor)
	{
		PrecisionModel pm = new PrecisionModel(scaleFactor);

		Geometry reducedGeom = SimpleGeometryPrecisionReducer.reduce(geom, pm);
		
		return reducedGeom;
	}
	
}
