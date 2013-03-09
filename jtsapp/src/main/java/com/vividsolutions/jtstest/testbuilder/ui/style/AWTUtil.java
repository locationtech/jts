package com.vividsolutions.jtstest.testbuilder.ui.style;

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

  public static void setStroke(Graphics2D g, double width) {
    Stroke newStroke = new BasicStroke((float) width);
    g.setStroke(newStroke);
  }
}
