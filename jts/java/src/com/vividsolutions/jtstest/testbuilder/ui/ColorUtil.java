package com.vividsolutions.jtstest.testbuilder.ui;

import java.awt.Color;

public class ColorUtil {

  public static Color opaque(Color clr)
  {
    return new Color(clr.getRed(), clr.getGreen(), clr.getBlue());
  }
}
