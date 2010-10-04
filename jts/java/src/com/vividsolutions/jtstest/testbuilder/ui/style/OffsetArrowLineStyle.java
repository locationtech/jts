package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;

import com.vividsolutions.jtstest.testbuilder.Viewport;
import com.vividsolutions.jtstest.testbuilder.ui.ColorUtil;

public class OffsetArrowLineStyle 
  extends SegmentStyle
{
  private final static double HEAD_ANGLE = 30;
  private final static double HEAD_LENGTH = 10;

  private Color color = Color.RED;

  private static Stroke dashStroke = new BasicStroke(1,                  // Width of stroke
      BasicStroke.CAP_SQUARE,  // End cap style
      BasicStroke.JOIN_MITER, // Join style
      10,                  // Miter limit
      new float[] {2, 2}, // Dash pattern
      0);                   // Dash phase 

  public OffsetArrowLineStyle(Color color) {
    this.color = color;
  }

  protected void paint(Point2D p0, Point2D p1, Viewport vp, Graphics2D gr)
  throws Exception
  {
  	paintOffsetArrow(p0, p1, vp, gr);
  }

  private static final double LINE_OFFSET = 4;
  private static final double ENDPOINT_OFFSET = 10;
  
  private static final double HEAD_ANGLE_RAD = (HEAD_ANGLE - 180 ) /180.0 * Math.PI;
  private static final double HEAD_COS = Math.cos(HEAD_ANGLE_RAD);
  private static final double HEAD_SIN = Math.sin(HEAD_ANGLE_RAD);
  private static final double HEAD_LEN = 6;
  
  private static final double MIN_VISIBLE_LEN = 2 * ENDPOINT_OFFSET + 4;
  
  protected void paintOffsetArrow(Point2D p0, Point2D p1, Viewport viewport,
      Graphics2D graphics) throws NoninvertibleTransformException 
  {
    // can't compute valid arrow for zero-length segments
    if (p0.equals(p1)) {
      return;
    }
    graphics.setColor(color);
    //      graphics.setStroke(1.0);
    graphics.setStroke(dashStroke);
    
    double dx = p1.getX() - p0.getX();
    double dy = p1.getY() - p0.getY();
    
    double len = Math.sqrt(dx * dx + dy * dy);
    
    if (len < MIN_VISIBLE_LEN) return;
    
    double vy = dy / len;
    double vx = dx / len;
    
    double off0x = p0.getX() + ENDPOINT_OFFSET * vx + LINE_OFFSET * vy;
    double off0y = p0.getY() + ENDPOINT_OFFSET * vy + LINE_OFFSET * -vx;
    
    double off1x = p1.getX() - ENDPOINT_OFFSET * vx + LINE_OFFSET * vy;
    double off1y = p1.getY() - ENDPOINT_OFFSET * vy + LINE_OFFSET * -vx;
    
    double headx = off1x + HEAD_LEN * (HEAD_COS * vx - HEAD_SIN * vy);
    double heady = off1y + HEAD_LEN * (HEAD_SIN * vx + HEAD_COS * vy);
    
    GeneralPath arrowhead = new GeneralPath();
    arrowhead.moveTo((float) off0x, (float) off0y);
    arrowhead.lineTo((float) off1x, (float) off1y);
    arrowhead.lineTo((float) headx, (float) heady);
    
    graphics.draw(arrowhead);
  }


}
