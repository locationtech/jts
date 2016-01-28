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

package com.vividsolutions.jtstest.testbuilder.ui;

import java.awt.Color;

import org.locationtech.jts.math.MathUtil;


public class ColorUtil {

  public static Color gray(int grayVal)
  {
    return new Color(grayVal, grayVal, grayVal);
  }
  
  public static Color opaque(Color clr)
  {
    return new Color(clr.getRed(), clr.getGreen(), clr.getBlue());
  }
  
  public static Color lighter(Color clr)
  {
    return lighter(clr, 0.4);
  }
  
  public static Color lighter(Color clr, double saturationFraction)
  {
    float[] hsb = new float[3];
    Color.RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue(), hsb);
    hsb[1] *= saturationFraction;
    return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
  }
  
  public static Color saturate(Color clr, double saturation)
  {
    float[] hsb = new float[3];
    Color.RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue(), hsb);
    hsb[1] = (float) MathUtil.clamp(saturation, 0, 1);;
    return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
  }

}
