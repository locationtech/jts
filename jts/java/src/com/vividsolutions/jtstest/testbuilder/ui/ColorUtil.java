package com.vividsolutions.jtstest.testbuilder.ui;

import java.awt.Color;

import com.vividsolutions.jts.math.MathUtil;

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
    float[] hsb = new float[3];
    Color.RGBtoHSB(clr.getRed(), clr.getGreen(), clr.getBlue(), hsb);
    hsb[1] *= 0.4;
    return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
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
