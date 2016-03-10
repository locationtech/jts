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
