package com.vividsolutions.jtstest.testbuilder.ui.style;

import java.awt.*;
import java.awt.geom.*;

import com.vividsolutions.jts.awt.FontGlyphReader;
import com.vividsolutions.jts.geomgraph.Quadrant;
import com.vividsolutions.jts.noding.Octant;
import com.vividsolutions.jtstest.testbuilder.Viewport;

public class SegmentIndexStyle 
  extends SegmentStyle
{
  private final static double MIN_LEN = 10;
  private final static int CHAR_WIDTH_APPROX = 6;
  private final static int CHAR_HEIGHT_APPROX = 6;
  private final static int VERTEX_OFFSET = 15;
  private final static int BOX_PAD = 1;
  private final static Font FONT = new Font(FontGlyphReader.FONT_SANSERIF, Font.PLAIN, 10);
  
  private Color color = Color.RED;

  public SegmentIndexStyle(Color color) {
    this.color = color;
  }

  protected void paint(int index, Point2D p0, Point2D p1, int lineType, Viewport vp, Graphics2D gr)
  throws Exception
  {
    double len = p0.distance(p1);
    // can't compute label location for zero-length segments
    if (len < MIN_LEN) {
      return;
    }
    
    double vertexOffset = len / 4;
    if (vertexOffset > VERTEX_OFFSET)
      vertexOffset = VERTEX_OFFSET;
    
    String indexStr = Integer.toString(index);

    double dx = p1.getX() - p0.getX();
    double dy = p1.getY() - p0.getY();
    
    int quadrant = Quadrant.quadrant(dx, -dy);
    
    double boxMaxX = /*2 * BOX_PAD +*/ indexStr.length() * CHAR_WIDTH_APPROX;
    double boxMaxY = 2 * BOX_PAD + CHAR_HEIGHT_APPROX;
    
    double strOffsetX = BOX_PAD;
    double strOffsetY = -BOX_PAD;
    switch (quadrant) {
    case 0:
      strOffsetX = -boxMaxX;
      break;
    case 1:
      break;
    case 2:
      strOffsetY = boxMaxY;
      break;
    case 3:
      strOffsetX = -boxMaxX;
      strOffsetY = boxMaxY;
      break;
    }
    
    double x = p0.getX() + dx/len*vertexOffset + strOffsetX;
    double y = p0.getY() + dy/len*vertexOffset + strOffsetY;
    
    gr.setColor(color);
    gr.setFont(FONT);
    gr.drawString(indexStr, (int) x, (int) y);  
  }
  


}
