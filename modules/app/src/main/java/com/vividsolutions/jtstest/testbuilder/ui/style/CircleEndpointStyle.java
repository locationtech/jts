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
import java.awt.geom.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.ui.Viewport;

public class CircleEndpointStyle 
  extends LineStringEndpointStyle 
{

  private final static double DIAMETER = 10;
  private boolean filled = true;

  // default in case colour is not set
  private Color color = Color.RED;
  private double diameter = DIAMETER;

  public CircleEndpointStyle(Color color, boolean start, boolean filled) {
    super(start);
    this.color = color;
    this.filled = filled;
}

  public CircleEndpointStyle(Color color, double diameter, boolean start, boolean filled) {
    super(start);
    this.color = color;
    this.diameter  = diameter;
    this.filled = filled;
}

  protected void paint(Point2D terminal, Point2D next, Viewport viewport,
      Graphics2D g) 
  {

      g.setPaint(color);

      Shape circle = toCircle(terminal);

      if (filled) {
          g.fill(circle);
      }
      else {
        g.draw(circle);
      }
  }

  private Shape toCircle(Point2D viewPoint) {
    return new Ellipse2D.Double(viewPoint.getX() - (diameter / 2d),
        viewPoint.getY() - (diameter / 2d), diameter, diameter);
}

}
