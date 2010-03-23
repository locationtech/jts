package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.precision.*;

public class PrecisionFunctions 
{

	public static Geometry reducePrecisionPointwise(Geometry geom, double scaleFactor)
	{
		PrecisionModel pm = new PrecisionModel(scaleFactor);
		Geometry reducedGeom = SimpleGeometryPrecisionReducer.reduce(geom, pm);
		return reducedGeom;
	}
	
	public static Geometry reducePrecision(Geometry geom, double scaleFactor)
	{
		PrecisionModel pm = new PrecisionModel(scaleFactor);
		Geometry reducedGeom = GeometryPrecisionReducer.reduce(geom, pm);
		return reducedGeom;
	}
	
}
