/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.shape.fractal.HilbertCurveBuilder;
import org.locationtech.jts.shape.fractal.KochSnowflakeBuilder;
import org.locationtech.jts.shape.fractal.MortonCurveBuilder;
import org.locationtech.jts.shape.fractal.SierpinskiCarpetBuilder;
import org.locationtech.jtstest.geomfunction.Metadata;

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

  @Metadata(description="Generates a Hilbert Curve")
  public static Geometry hilbertCurve(Geometry g,
      @Metadata(title="Number of points")
      int n) {
    HilbertCurveBuilder builder = new HilbertCurveBuilder(FunctionsUtil.getFactoryOrDefault(g));
    if (g != null) {
      builder.setExtent(FunctionsUtil.getEnvelopeOrDefault(g));
    }
    builder.setNumPoints(n);
    return builder.getGeometry();
  }
  
	@Metadata(description="Generates a Hilbert Curve at a given level")
  public static Geometry hilbertCurveAtLevel(Geometry g,
      @Metadata(title="Level (1-16)")
      int level) {
    HilbertCurveBuilder builder = new HilbertCurveBuilder(FunctionsUtil.getFactoryOrDefault(g));
    if (g != null) {
      builder.setExtent(FunctionsUtil.getEnvelopeOrDefault(g));
    }
    builder.setLevel(level);
    return builder.getGeometry();
  }
	
  @Metadata(description="Generates a Morton Curve")
  public static Geometry mortonCurve(Geometry g,
      @Metadata(title="Number of points")
      int n) {
   MortonCurveBuilder builder = new MortonCurveBuilder(FunctionsUtil.getFactoryOrDefault(g));
    if (g != null) {
      builder.setExtent(FunctionsUtil.getEnvelopeOrDefault(g));
    }
    builder.setNumPoints(n);
    return builder.getGeometry();
  }
  
  @Metadata(description="Generates a Morton Curve at a given level")
  public static Geometry mortonCurveAtLevel(Geometry g,
      @Metadata(title="Level (1-16)")
      int level) {
   MortonCurveBuilder builder = new MortonCurveBuilder(FunctionsUtil.getFactoryOrDefault(g));
    if (g != null) {
      builder.setExtent(FunctionsUtil.getEnvelopeOrDefault(g));
    }
    // builder.setNumPoints(n);
    builder.setLevel(level);
    return builder.getGeometry();
  }
}
