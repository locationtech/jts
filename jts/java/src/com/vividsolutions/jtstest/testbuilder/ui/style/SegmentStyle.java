package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jtstest.testbuilder.Viewport;

public abstract class SegmentStyle 
extends LineStringStyle
{

  public SegmentStyle() {
    super();
    // TODO Auto-generated constructor stub
  }

  protected void paintLineString(LineString lineString, int lineType, Viewport viewport, Graphics2D graphics) throws Exception {
    for (int i = 0; i < lineString.getNumPoints() - 1; i++) {
      paint(lineString.getCoordinateN(i),
            lineString.getCoordinateN(i + 1),
            viewport, graphics);
    }
  }

  protected void paint(Coordinate p0, Coordinate p1, Viewport viewport, Graphics2D g
      ) throws Exception {
      paint(viewport.toView(new Point2D.Double(p0.x, p0.y)),
          viewport.toView(new Point2D.Double(p1.x, p1.y)), viewport, g);
  }

  /**
   * 
   * @param p0 the origin of the line segment, in view space
   * @param p1 the termination of the line segment, in view space
   * @param viewport
   * @param graphics
   * @throws Exception
   */
  protected abstract void paint(Point2D p0, Point2D p1,
      Viewport viewport, Graphics2D graphics) throws Exception;

}
