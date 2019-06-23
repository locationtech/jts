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
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


public class CircleLineEndStyle 
  extends LineEndStyle 
{

  private static final int FILL_ALPHA = 150;
  private final static double DIAMETER = 10;
  private boolean filled = true;

  // default in case colour is not set
  private Color color = Color.RED;
  private double diameter = DIAMETER;
  private static final double OFFSET_SIZE = 8;

  public CircleLineEndStyle(Color color, boolean start, boolean filled) {
    super(start);
    setColor(color);
    this.filled = filled;
  }

  public CircleLineEndStyle(Color color, double diameter, boolean start, boolean filled) {
    this(color, start, filled);
    this.diameter  = diameter;
  }
  
  public void setColor(Color color) {
    this.color = ColorUtil.setAlpha(color, FILL_ALPHA);;
  }

  protected void paint(Point2D terminal, Point2D next, Viewport viewport,
      Graphics2D g) 
  {
    Point2D offset = AWTUtil.vector(next, terminal, OFFSET_SIZE);
      Shape circle = toCircle(
          terminal.getX() - offset.getX(), 
          terminal.getY() - offset.getY(),
          diameter
          );
      
     g.setPaint(color);
      
 
      if (filled) {
          g.fill(circle);
      }
      else {
        g.draw(circle);
      }
  }

  private static Shape toCircle(double x, double y, double diameter) {
    return new Ellipse2D.Double(x - (diameter / 2d), y - (diameter / 2d), diameter, diameter);
  }

}
