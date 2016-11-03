/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


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
