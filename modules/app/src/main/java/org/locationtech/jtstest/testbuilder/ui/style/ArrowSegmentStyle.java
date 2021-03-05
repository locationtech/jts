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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.locationtech.jtstest.testbuilder.ui.Viewport;


public class ArrowSegmentStyle 
  extends SegmentStyle
{
  private final static double HEAD_ANGLE = 15;
  private final static double HEAD_LENGTH = 10;

  private Color color = Color.RED;

  private static Stroke DASH_STROKE = new BasicStroke(1,                  // Width of stroke
      BasicStroke.CAP_SQUARE,  // End cap style
      BasicStroke.JOIN_MITER, // Join style
      10,                  // Miter limit
      new float[] {2, 2}, // Dash pattern
      0);                   // Dash phase 
  private static Stroke MID_ARROW_STROKE = new BasicStroke(1);
  
  public ArrowSegmentStyle(Color color) {
    this.color = color;
  }
  public void setColor(Color color) {
    this.color = color;
  }

  protected void paint(int index, Point2D p0, Point2D p1, int lineType, Viewport vp, Graphics2D gr)
  throws Exception
  {
    if (lineType == LINE)
      paintMidpointArrow(p0, p1, vp, gr);
    else {
      paintMidArrowHalf(p0, p1, vp, gr);
      //paintOffsetLineArrow(p0, p1, vp, gr);
    }
  }

  protected void paintMidpointArrow(Point2D p0, Point2D p1, Viewport viewport,
      Graphics2D graphics) throws NoninvertibleTransformException 
  {
    if (isTooSmallToRender(p0, p1)) return;
    
    graphics.setColor(color);
    graphics.setStroke(MID_ARROW_STROKE);
    
    double arrowLen = 10;
    double arrowAngle = 15;
    
    Point2D mid = new Point2D.Float((float) ((p0.getX() + p1.getX()) / 2),
        (float) ((p0.getY() + p1.getY()) / 2));
    GeneralPath arrowhead = ArrowLineEndStyle.arrowheadPath(p0, p1, mid,
        arrowLen, arrowAngle);
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
    graphics.setStroke(DASH_STROKE);
    
    GeneralPath arrowhead = arrowHalfOffset(p0, p1);
    graphics.draw(arrowhead);
  }
  
  private static GeneralPath arrowHalfOffset(Point2D p0, Point2D p1) {
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
    return arrowhead;
  }
  
  private static double HALF_ARROW_LEN = 12;
  
  protected void paintMidArrowHalf(Point2D p0, Point2D p1, Viewport viewport,
      Graphics2D graphics) throws NoninvertibleTransformException 
  {
    
    double segDist = p0.distance(p1);
    double arrrowLen = HALF_ARROW_LEN;
    if (segDist < 3 * HALF_ARROW_LEN) arrrowLen = HALF_ARROW_LEN / 2;
      
    if (isTooSmallToRender(p0, p1, 3 * arrrowLen)) return;

    
    graphics.setColor(color);
    //      graphics.setStroke(1.0);
    
    Point2D mid = new Point2D.Float(
        (float) ((p0.getX() + p1.getX()) / 2),
        (float) ((p0.getY() + p1.getY()) / 2) );

    /*
    Point2D mid23 = new Point2D.Float(
        (float) ((p0.getX() + 2 * p1.getX()) / 3),
        (float) ((p0.getY() + 2 * p1.getY()) / 3) );
    */
    Point2D origin = mid;
    
    GeneralPath arrowhead = arrowHeadHalf(origin, p1, 2, arrrowLen, HEAD_ANGLE_RAD, 1.2);
    arrowhead.closePath();
    
    graphics.fill(arrowhead);
    graphics.draw(arrowhead);
  }
  
  private static GeneralPath arrowHeadHalf(Point2D origin, Point2D p1, 
      double offset, double len, double angle, double rakeFactor
      ) {
    double dx = p1.getX() - origin.getX();
    double dy = p1.getY() - origin.getY();
    
    double vlen = Math.sqrt(dx * dx + dy * dy);
    
    if (vlen <= 0) return null;
    
    double ux = dx / vlen;
    double uy = dy / vlen;
    
    // normal unit vector (direction of offset) - to right of segment
    // use negative offset to offset left
    double nx = -uy;
    double ny = ux;
    
    double off0x = origin.getX() + offset * nx;
    double off0y = origin.getY() + offset * ny;
    
    double off1x = origin.getX() + len * ux + offset * nx;
    double off1y = origin.getY() + len * uy + offset * ny;
    
    // TODO: make head direction match offset direction
    double barbBase = rakeFactor * len;
    double barbOff = barbBase * -Math.sin(angle);
    int directionSign = offset < 0 ? -1 : 1;
    double barbx = off1x - barbBase * ux + barbOff * nx * directionSign;
    double barby = off1y - barbBase * uy + barbOff * ny * directionSign;
    
    GeneralPath arrowhead = new GeneralPath();
    arrowhead.moveTo((float) off0x, (float) off0y);
    arrowhead.lineTo((float) off1x, (float) off1y);
    arrowhead.lineTo((float) barbx, (float) barby);
    return arrowhead;
  }

  private static boolean isTooSmallToRender(Point2D p0, Point2D p1) {
    return isTooSmallToRender(p0, p1, MIN_VISIBLE_LEN);
  }
  
  private static boolean isTooSmallToRender(Point2D p0, Point2D p1, double minLen)
  {
    if (p0.equals(p1)) {
      return true;
    }
    double dx = p1.getX() - p0.getX();
    double dy = p1.getY() - p0.getY();
    
    double len = Math.sqrt(dx * dx + dy * dy);
    
    return len < minLen;
  }
}
