package com.vividsolutions.jtstest.testbuilder.ui.tools;

import java.awt.Cursor;

import com.vividsolutions.jtstest.testbuilder.model.GeometryType;

public class RectangleTool
extends BoxBandTool
{
  private static RectangleTool singleton = null;

  public static RectangleTool getInstance() {
      if (singleton == null)
          singleton = new RectangleTool();
      return singleton;
  }

  public RectangleTool() {
    super();
    cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  }

  protected void gestureFinished() 
  {      
    geomModel().setGeometryType(GeometryType.POLYGON);
    geomModel().addComponent(getCoordinates());
    panel().updateGeom();
  }

  
}
