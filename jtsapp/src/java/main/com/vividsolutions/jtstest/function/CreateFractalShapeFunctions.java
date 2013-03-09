package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.shape.fractal.KochSnowflakeBuilder;
import com.vividsolutions.jts.shape.fractal.SierpinskiCarpetBuilder;

public class CreateFractalShapeFunctions 
{

	public static Geometry kochSnowflake(Geometry g, int n)
	{
		KochSnowflakeBuilder builder = new KochSnowflakeBuilder(FunctionsUtil.getFactoryOrDefault(g));
		builder.setExtent(FunctionsUtil.getEnvelopeOrDefault(g));
		builder.setNumPoints(n);
		return builder.getGeometry();
	}
	public static Geometry sierpinskiCarpet(Geometry g, int n)
	{
		SierpinskiCarpetBuilder builder = new SierpinskiCarpetBuilder(FunctionsUtil.getFactoryOrDefault(g));
		builder.setExtent(FunctionsUtil.getEnvelopeOrDefault(g));
		builder.setNumPoints(n);
		return builder.getGeometry();
	}
}
