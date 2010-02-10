package com.vividsolutions.jtstest.testbuilder.ui.tools;

import java.awt.Color;

public class GeometryStyle {

  public GeometryStyle() {
    super();
    // TODO Auto-generated constructor stub
  }

  public static Color getBandColor(int i)
  {
    if (i == 0) return Color.cyan;
    return Color.pink;
  }
}
