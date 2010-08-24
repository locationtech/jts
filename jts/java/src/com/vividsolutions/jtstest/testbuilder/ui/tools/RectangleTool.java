package com.vividsolutions.jtstest.testbuilder.ui.tools;

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
  }

  protected void gestureFinished() 
  {      
    panel().getGeomModel().setGeometryType(GeometryType.POLYGON);
    geomModel().addComponent(getCoordinatesFromStart());
    panel().updateGeom();
  }

  
}
