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

package org.locationtech.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;

import org.locationtech.jtstest.testbuilder.ui.Viewport;


public class MidArrowLineStyle 
  extends SegmentStyle
{
  private final static double HEAD_ANGLE = 30;
  private final static double HEAD_LENGTH = 10;

  private Color color = Color.RED;

  public MidArrowLineStyle(Color color) {
    this.color = color;
  }

  protected void paint(int index, Point2D p0, Point2D p1, int lineType, Viewport vp, Graphics2D gr)
  throws Exception
  {
  	paintMidpointArrow(p0, p1, vp, gr);
  }

  protected void paintMidpointArrow(Point2D p0, Point2D p1, Viewport viewport,
      Graphics2D graphics) throws NoninvertibleTransformException 
  {
    // can't compute valid arrow for zero-length segments
    if (p0.equals(p1)) {
      return;
    }
    graphics.setColor(color);
    //      graphics.setStroke(1.0);
    Point2D mid = new Point2D.Float((float) ((p0.getX() + p1.getX()) / 2),
        (float) ((p0.getY() + p1.getY()) / 2));
    GeneralPath arrowhead = ArrowEndpointStyle.arrowheadPath(p0, p1, mid,
    		HEAD_LENGTH, HEAD_ANGLE);
    graphics.draw(arrowhead);
  }


}
