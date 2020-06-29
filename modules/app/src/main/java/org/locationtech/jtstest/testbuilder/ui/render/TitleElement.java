/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder.ui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;

import org.locationtech.jts.awt.FontGlyphReader;
import org.locationtech.jtstest.testbuilder.ui.Viewport;

public class TitleElement {
  private static final int BOX_MARGIN = 10;

  private static final int DEFAULT_FONT_SIZE = 14;

  private Viewport viewport;
  private String title = "";
  
  private Font font = new Font(FontGlyphReader.FONT_SANSERIF, Font.BOLD, DEFAULT_FONT_SIZE);
  private int borderSize = 1;

  private boolean isBorderEnabled = true;

  private Color fillClr = Color.WHITE;

  private Paint borderColor;;

  public TitleElement(Viewport viewport) {
    this.viewport = viewport;
  }
  public void setBorderEnabled(boolean isBorderEnabled) {
    this.isBorderEnabled  = isBorderEnabled;
  }
  public void setBorder(int borderSize) {
    this.borderSize = borderSize;
  }
  public void setBorderColor(Color clr) {
    borderColor = clr;
  }
  public void setFill(Color clr) {
    this.fillClr  = clr;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  
  public void paint(Graphics2D g) {
    
    g.setFont(font);
    
    int textWidth = (int) g.getFontMetrics().getStringBounds(title, g).getWidth();
    int width = textWidth + 2 * BOX_MARGIN;
    
    int lineHeight = DEFAULT_FONT_SIZE;
    int height = lineHeight + 2 * BOX_MARGIN;
    
    Rectangle box = new Rectangle(
        0, 
        0,
        width, height);

    drawBox(box, g);
    
    g.setPaint(Color.BLACK);
    g.drawString(title, BOX_MARGIN, lineHeight + BOX_MARGIN);
  }

  private void drawBox(Rectangle box, Graphics2D g) {    
    g.setPaint(fillClr);
    g.fill(box);
    
    if (isBorderEnabled && borderSize > 0) {
      Stroke strokeBox = new BasicStroke(borderSize, // Width of stroke
          BasicStroke.CAP_BUTT,  // End cap style
          BasicStroke.JOIN_MITER, // Join style
          10,                  // Miter limit
          null, // Dash pattern
          0);                   // Dash phase 
      g.setStroke(strokeBox);
      g.setPaint(borderColor);
      g.draw(box);
    }
  }


}
