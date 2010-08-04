package com.vividsolutions.jtstest.testbuilder;

import java.awt.Color;

import com.vividsolutions.jtstest.testbuilder.model.GeometryDepiction;

public class AppConstants 
{
  public static double HIGHLIGHT_SIZE = 50.0;
  
	public static double TOPO_STRETCH_VIEW_DIST = 4;
	
	public static final Color HIGHLIGHT_COLOR = new Color(255, 192, 0, 150);
	public static final Color BAND_COLOR = new Color(255, 0, 0, 255);
	public static final Color INDICATOR_FILL_COLOR = GeometryDepiction.GEOM_RESULT_FILL_CLR;
	//public static final Color INDICATOR_LINE_COLOR = new Color(255, 0, 0, 255);
	//public static final Color INDICATOR_FILL_COLOR = new Color(255, 200, 200, 200);
	  public static final Color INDICATOR_LINE_COLOR = GeometryDepiction.GEOM_RESULT_LINE_CLR;

}
