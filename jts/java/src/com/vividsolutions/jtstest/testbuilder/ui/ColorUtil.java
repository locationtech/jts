package com.vividsolutions.jtstest.testbuilder.ui;

import java.awt.Color;

public class ColorUtil {

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

}
