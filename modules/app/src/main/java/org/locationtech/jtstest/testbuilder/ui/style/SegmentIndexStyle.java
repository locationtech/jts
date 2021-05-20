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

import org.locationtech.jts.awt.FontGlyphReader;
import org.locationtech.jts.geom.Quadrant;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


public class SegmentIndexStyle 
  extends SegmentStyle
{
  private final static double MIN_LEN = 10;
  private final static int CHAR_WIDTH_APPROX = 6;
  private final static int CHAR_HEIGHT_APPROX = 6;
  private final static int VERTEX_OFFSET = 15;
  private final static int BOX_PAD = 1;
  private final static Font FONT = new Font(FontGlyphReader.FONT_SANSSERIF, Font.PLAIN, 10);
  private static final int MIN_LABEL_DIST = 300;
  
  private Color color = Color.RED;

  private double lastX = 0;
  private double lastY = 0;
  
  public SegmentIndexStyle(Color color) {
    this.color = color;
  }

  protected void paint(int index, Point2D p0, Point2D p1, int lineType, Viewport vp, Graphics2D gr)
  throws Exception
  {
    double len = p0.distance(p1);
    // don't try and label very short segments
    // can't compute label location for zero-length segments
    if (len < MIN_LEN) {
      return;
    }
    
    double vertexOffset = len / 4;
    if (vertexOffset > VERTEX_OFFSET)
      vertexOffset = VERTEX_OFFSET;
    
    double dx = p1.getX() - p0.getX();
    double dy = p1.getY() - p0.getY();
    int quadrant = Quadrant.quadrant(dx, -dy);
    
    String indexStr = Integer.toString(index);
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
    
    // cache last point drawn for simple label deconfliction
    boolean doDraw = index == 0 || distToLast(x, y) > MIN_LABEL_DIST; 
    
    if (doDraw) {
      gr.setColor(color);
      gr.setFont(FONT);
      gr.drawString(indexStr, (int) x, (int) y);
      
      lastX = x;
      lastY = y;
    }
  }

  private double distToLast(double x, double y) {
    return (x - lastX)*(x - lastX) + (y - lastY)*(y - lastY);
  }
  


}
