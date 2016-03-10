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

import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


public class ArrowLineStyle 
  extends SegmentStyle
{
  private final static double HEAD_ANGLE = 30;
  private final static double HEAD_LENGTH = 8;

  private Color color = Color.RED;

  private static Stroke dashStroke = new BasicStroke(1,                  // Width of stroke
      BasicStroke.CAP_SQUARE,  // End cap style
      BasicStroke.JOIN_MITER, // Join style
      10,                  // Miter limit
      new float[] {2, 2}, // Dash pattern
      0);                   // Dash phase 
  private static Stroke MID_ARROW_STROKE = new BasicStroke(1);
  
  public ArrowLineStyle(Color color) {
    this.color = color;
  }

  protected void paint(int index, Point2D p0, Point2D p1, int lineType, Viewport vp, Graphics2D gr)
  throws Exception
  {
  	if (lineType == LINE)
  		paintMidpointArrow(p0, p1, vp, gr);
  	else
  		paintOffsetArrow(p0, p1, vp, gr);
  }

  protected void paintMidpointArrow(Point2D p0, Point2D p1, Viewport viewport,
      Graphics2D graphics) throws NoninvertibleTransformException 
  {
    if (isTooSmallToRender(p0, p1)) return;
    graphics.setColor(color);
    graphics.setStroke(MID_ARROW_STROKE);
    Point2D mid = new Point2D.Float((float) ((p0.getX() + p1.getX()) / 2),
        (float) ((p0.getY() + p1.getY()) / 2));
    GeneralPath arrowhead = ArrowEndpointStyle.arrowheadPath(p0, p1, mid,
    		HEAD_LENGTH, HEAD_ANGLE);
    graphics.draw(arrowhead);
  }

  private static final double LINE_OFFSET = 4;
  private static final double ENDPOINT_OFFSET = 15;
  
  private static final double HEAD_ANGLE_RAD = (HEAD_ANGLE - 180 ) /180.0 * Math.PI;
  private static final double HEAD_COS = Math.cos(HEAD_ANGLE_RAD);
  private static final double HEAD_SIN = Math.sin(HEAD_ANGLE_RAD);
  
  public static final double MIN_VISIBLE_LEN = 2 * ENDPOINT_OFFSET + 4;

  protected void paintOffsetArrow(Point2D p0, Point2D p1, Viewport viewport,
      Graphics2D graphics) throws NoninvertibleTransformException 
  {
    if (isTooSmallToRender(p0, p1)) return;
    
    graphics.setColor(color);
    //      graphics.setStroke(1.0);
    graphics.setStroke(dashStroke);
    
    
    double dx = p1.getX() - p0.getX();
    double dy = p1.getY() - p0.getY();
    
    double len = Math.sqrt(dx * dx + dy * dy);
    
    double vy = dy / len;
    double vx = dx / len;
    
    double off0x = p0.getX() + ENDPOINT_OFFSET * vx + LINE_OFFSET * vy;
    double off0y = p0.getY() + ENDPOINT_OFFSET * vy + LINE_OFFSET * -vx;
    
    double off1x = p1.getX() - ENDPOINT_OFFSET * vx + LINE_OFFSET * vy;
    double off1y = p1.getY() - ENDPOINT_OFFSET * vy + LINE_OFFSET * -vx;
    
    double headx = off1x + HEAD_LENGTH * (HEAD_COS * vx - HEAD_SIN * vy);
    double heady = off1y + HEAD_LENGTH * (HEAD_SIN * vx + HEAD_COS * vy);
    
    GeneralPath arrowhead = new GeneralPath();
    arrowhead.moveTo((float) off0x, (float) off0y);
    arrowhead.lineTo((float) off1x, (float) off1y);
    arrowhead.lineTo((float) headx, (float) heady);
    
    graphics.draw(arrowhead);
  }

  private boolean isTooSmallToRender(Point2D p0, Point2D p1)
  {
    if (p0.equals(p1)) {
      return true;
    }
    double dx = p1.getX() - p0.getX();
    double dy = p1.getY() - p0.getY();
    
    double len = Math.sqrt(dx * dx + dy * dy);
    
    return len < MIN_VISIBLE_LEN;

  }
}
