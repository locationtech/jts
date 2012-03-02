package com.vividsolutions.jtstest.testbuilder;

import java.awt.Color;

import com.vividsolutions.jtstest.testbuilder.model.GeometryDepiction;
import com.vividsolutions.jtstest.testbuilder.ui.ColorUtil;

public class AppConstants 
{
  public static final int POINT_SIZE = 5;
  public static final int VERTEX_SIZE = 4;
  public static double HIGHLIGHT_SIZE = 50.0;
  public static double VERTEX_SHADOW_SIZE = 100;
  
	public static double TOPO_STRETCH_VIEW_DIST = 5;
	
	public static double  MASK_WIDTH_FRAC = 0.3333;
	// a very light gray
	public static final Color MASK_CLR = new Color(230, 230, 230);
	
  public static final Color VERTEX_SHADOW_CLR = new Color(180,180,180);
  public static final Color VERTEX_HIGHLIGHT_CLR = new Color(255, 255, 0);
  
  public static final Color HIGHLIGHT_CLR = new Color(255, 192, 0, 150);
  public static final Color HIGHLIGHT_FILL_CLR = new Color(255, 240, 192, 200);
  
	public static final Color BAND_CLR = new Color(255, 0, 0, 255);
	public static final Color INDICATOR_FILL_CLR = GeometryDepiction.GEOM_RESULT_FILL_CLR;
	//public static final Color INDICATOR_LINE_COLOR = new Color(255, 0, 0, 255);
	//public static final Color INDICATOR_FILL_COLOR = new Color(255, 200, 200, 200);
	public static final Color INDICATOR_LINE_CLR = GeometryDepiction.GEOM_RESULT_LINE_CLR;

	public static final int AXIS_WIDTH = 3;
	public static final Color AXIS_CLR = Color.lightGray;

	public static final Color GRID_CLR = Color.lightGray;

	public static final Color GRID_MAJOR_CLR = ColorUtil.gray(240);
	public static final Color GRID_MINOR_CLR = ColorUtil.gray(190);
  public static int TOLERANCE_PIXELS = 5;

}
