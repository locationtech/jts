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
      paint(i, 
          lineString.getCoordinateN(i),
          lineString.getCoordinateN(i + 1),
          lineType, viewport, graphics);
    }
  }

  protected void paint(int index, Coordinate p0, Coordinate p1, int lineType, Viewport viewport, Graphics2D g
      ) throws Exception {
      paint(index, viewport.toView(new Point2D.Double(p0.x, p0.y)),
          viewport.toView(new Point2D.Double(p1.x, p1.y)), lineType, viewport, g);
  }

  /**
   * 
   * @param p0 the origin of the line segment, in view space
   * @param p1 the termination of the line segment, in view space
   * @param viewport
   * @param graphics
   * @throws Exception
   */
  protected abstract void paint(int index, Point2D p0, Point2D p1,
  		int lineType, Viewport viewport, Graphics2D graphics) throws Exception;

}
