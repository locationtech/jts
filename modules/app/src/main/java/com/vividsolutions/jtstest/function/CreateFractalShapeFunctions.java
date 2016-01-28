/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package com.vividsolutions.jtstest.function;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.shape.fractal.KochSnowflakeBuilder;
import org.locationtech.jts.shape.fractal.SierpinskiCarpetBuilder;

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
