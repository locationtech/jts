package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.*;
import com.vividsolutions.jtstest.testbuilder.ui.render.GeometryPainter;

public class BasicStyle implements Style
{
  private Color lineColor;
  private Color fillColor;

  public BasicStyle(Color lineColor, Color fillColor) {
    this.lineColor = lineColor;
    this.fillColor = fillColor;
  }

  public BasicStyle() {
  }

  public void paint(Geometry geom, Viewport viewport, Graphics2D g)
  {
  	GeometryPainter.paint(geom, viewport, g, lineColor, fillColor);
  }
  
  public Color getLineColor() {
    return lineColor;
  }

  public Color getFillColor() {
    return fillColor;
  }


}
