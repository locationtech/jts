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

import org.locationtech.jts.geom.*;

import com.vividsolutions.jtstest.testbuilder.ui.Viewport;

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
