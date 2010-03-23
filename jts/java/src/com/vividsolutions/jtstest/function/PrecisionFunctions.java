package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.precision.*;

public class PrecisionFunctions 
{

	private static Geometry OLDreducePrecisionPointwise(Geometry geom, double scaleFactor)
	{
		PrecisionModel pm = new PrecisionModel(scaleFactor);
		Geometry reducedGeom = SimpleGeometryPrecisionReducer.reduce(geom, pm);
		return reducedGeom;
	}
	
	public static Geometry reducePrecisionPointwise(Geometry geom, double scaleFactor)
	{
		PrecisionModel pm = new PrecisionModel(scaleFactor);
		GeometryPrecisionReducer reducer = new GeometryPrecisionReducer(pm);
		reducer.setPointwise(true);
		Geometry reducedGeom = reducer.reduce(geom);
		return reducedGeom;
	}
	
	public static Geometry reducePrecision(Geometry geom, double scaleFactor)
	{
		PrecisionModel pm = new PrecisionModel(scaleFactor);
		Geometry reducedGeom = GeometryPrecisionReducer.reduce(geom, pm);
		return reducedGeom;
	}
	
}
