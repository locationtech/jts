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

import org.locationtech.jts.awt.FontGlyphReader;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jtstest.*;
import org.locationtech.jtstest.testbuilder.AppConstants;
import org.locationtech.jtstest.testbuilder.ui.GraphicsUtil;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


public class VertexLabelStyle  implements Style
{
  private static final int DEFAULT_FONT_SIZE = 9;

  private static final int LABEL_OFFSET_Y = 4;

  private Font font = new Font(FontGlyphReader.FONT_SANSSERIF, Font.PLAIN, DEFAULT_FONT_SIZE);
  
  private Color color;
  private int size;
  
  // reuse point objects to avoid creation overhead
  private Point2D pM = new Point2D.Double();
  private Point2D pV = new Point2D.Double();


  public VertexLabelStyle(Color color) {
    this.color = color;
    // create basic rectangle shape
    init();
  }

  public Color getColor() {
    return color;
  }
  public void setColor(Color color) {
    this.color = color;
  }
  public int getSize() {
    return size;
  }
  
  public void setSize(int size) {
    this.size = size;
    init();
  }
  
  private void init() {
  }

  public void paint(Geometry geom, Viewport viewport, Graphics2D g)
  {
    g.setPaint(color);
    g.setFont(font);
    Coordinate[] coordinates = geom.getCoordinates();
    
    for (int i = 0; i < coordinates.length; i++) {
      Coordinate pt = coordinates[i];
      if (! viewport.containsInModel(pt)) {
          continue;
      }       
      pM.setLocation(pt.x, pt.y);
      viewport.toView(pM, pV);
      String label = WKTWriter.format(pt);
      GraphicsUtil.drawStringAlignCenter(g, label, (int) pV.getX(), (int) pV.getY() - LABEL_OFFSET_Y); 
    }
  }
  
}
