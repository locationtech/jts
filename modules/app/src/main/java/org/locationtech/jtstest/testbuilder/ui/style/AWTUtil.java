/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;

public class AWTUtil 
{

  public static Point2D subtract(Point2D a, Point2D b) {
    return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
  }

  public static Point2D add(Point2D a, Point2D b) {
    return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
  }

  public static Point2D multiply(Point2D v, double x) {
    return new Point2D.Double(v.getX() * x, v.getY() * x);
  }

  public static Point2D vector(Point2D a, Point2D b, double size) {
    double dx = b.getX() - a.getX();
    double dy = b.getY() - a.getY();
    double len = Math.sqrt(dx*dx + dy*dy);
    return new Point2D.Double(size * dx/len, size * dy/len);
  }

  public static void setStroke(Graphics2D g, double width) {
    Stroke newStroke = new BasicStroke((float) width);
    g.setStroke(newStroke);
  }
}
