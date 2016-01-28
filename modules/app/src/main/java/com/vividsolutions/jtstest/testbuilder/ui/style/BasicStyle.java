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

package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.*;

import org.locationtech.jts.geom.*;

import com.vividsolutions.jtstest.*;
import com.vividsolutions.jtstest.testbuilder.ui.Viewport;
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
