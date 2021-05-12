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
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.List;

import org.locationtech.jts.awt.FontGlyphReader;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.geom.GeometryUtil;
import org.locationtech.jtstest.testbuilder.model.Layer;
import org.locationtech.jtstest.testbuilder.ui.Viewport;

public class LegendElement {
  
  private static final Color NAME_CLR = Color.BLACK;
  
  private static final int BOX_OFFSET = 1;
  private static final int BOX_MARGIN = 8;
  private static final int SWATCH_SIZE = 10;
  private static final int SWATCH_MARGIN = 6;
  
  private static final int DEFAULT_FONT_SIZE = 12;

  private Viewport viewport;
  private Font font = new Font(FontGlyphReader.FONT_SANSERIF, Font.PLAIN, DEFAULT_FONT_SIZE);
  private int borderSize = 1;

  private boolean isBorderEnabled;
  private boolean isStatsEnabled = false;

  private Color borderColor;

  private Color fillClr = Color.WHITE;

  public LegendElement(Viewport viewport) {
    this.viewport = viewport;
  }
  
  public void setBorderEnabled(boolean isBorderEnabled) {
    this.isBorderEnabled = isBorderEnabled;
  }
  
  public void setStatsEnabled(boolean isEnabled) {
    this.isStatsEnabled = isEnabled;
  }
  
  public void setBorder(int borderSize) {
    this.borderSize  = borderSize;
  }
  
  public void setBorderColor(Color clr) {
    borderColor = clr;
  }
  public void setFill(Color clr) {
    this.fillClr  = clr;
  }
  public void paint(List<Layer> layerList, Graphics2D g) {

    if (layerList.size() <= 0) return;
    
    g.setFont(font);
    Rectangle box = computeBox(layerList, g);
    drawBox(box, g);
    drawEntries(layerList, box, g);
  }

  private void drawEntries(List<Layer> layerList, Rectangle box, Graphics2D g) {
    g.setFont(font);

    int nameX = box.x + BOX_MARGIN + SWATCH_SIZE + SWATCH_MARGIN;
    // have to account for width of outline
    int baseY = box.y + BOX_MARGIN - 2 * borderSize;
    int lineHeight = DEFAULT_FONT_SIZE + 4;
    int n = layerList.size();
    for (int i = 0; i < n; i++) {
      // draw layer name
      int nameY = baseY + (i + 1) * lineHeight;
      drawEntry(layerList.get(i), nameX, nameY, g);
    }
  }

  private void drawEntry(Layer layer, int nameX, int nameY, Graphics2D g) {
    String name = getDescription(layer);
    g.setPaint(NAME_CLR);
    g.drawString(name, nameX, nameY);
    
    int swatchX = nameX - SWATCH_SIZE - SWATCH_MARGIN;
    int swatchY = nameY - DEFAULT_FONT_SIZE + 2;
    drawSwatch(layer, swatchX, swatchY, g);
  }

  private String getDescription(Layer layer) {
    String desc = layer.getName();
    if (isStatsEnabled) {
      desc += " -- " + GeometryUtil.structureSummary(layer.getGeometry());
    }
    return desc;
  }

  private void drawSwatch(Layer layer, int x, int y, Graphics2D g) {
    Geometry geom = layer.getGeometry();
    switch (geom.getDimension()) {
    case 2:
      drawSwatchBox(layer, x, y, g);
      break;
    case 1:
      drawSwatchLine(layer, x, y, g);
      break;
    case 0:
      drawSwatchPoint(layer, x, y, g);
      break;
    }
  }
  
  private void drawSwatchBox(Layer layer, int x, int y, Graphics2D g) {
    Rectangle box = new Rectangle(
        x, y,
        SWATCH_SIZE, SWATCH_SIZE);
    
    //--- paint Fill
    Color fillClr = Color.WHITE;
    if (layer.getGeometryStyle().isFilled())
      fillClr = layer.getGeometryStyle().getFillColor();
    
    g.setPaint(fillClr);
    g.fill(box);
    
    //--- paint Line
    float lineWidth = layer.getGeometryStyle().getStrokeWidth();
    if (layer.getGeometryStyle().getStrokeWidth() > 3)
      lineWidth = 3;
    
    Stroke strokeBox = new BasicStroke(lineWidth, // Width of stroke
        BasicStroke.CAP_BUTT,  // End cap style
        BasicStroke.JOIN_MITER, // Join style
        10,                  // Miter limit
        null, // Dash pattern
        0);                   // Dash phase 
    g.setStroke(strokeBox);
    
    Color lineClr = layer.getGeometryStyle().getLineColor();
    g.setPaint(lineClr);
    g.draw(box);
  }

  private void drawSwatchLine(Layer layer, int x, int y, Graphics2D g) {
    Line2D line = new Line2D.Float(
        x, y + SWATCH_SIZE,
        x + SWATCH_SIZE, y );
    
    //--- paint Line
    float lineWidth = layer.getGeometryStyle().getStrokeWidth();
    if (layer.getGeometryStyle().getStrokeWidth() > 3)
      lineWidth = 3;
    
    Stroke strokeBox = new BasicStroke(lineWidth, // Width of stroke
        BasicStroke.CAP_BUTT,  // End cap style
        BasicStroke.JOIN_MITER, // Join style
        10,                  // Miter limit
        null, // Dash pattern
        0);                   // Dash phase 
    g.setStroke(strokeBox);
    
    Color lineClr = layer.getGeometryStyle().getLineColor();
    g.setPaint(lineClr);
    g.draw(line);
  }
  
  private void drawSwatchPoint(Layer layer, int x, int y, Graphics2D g) {
    int size = layer.getLayerStyle().getVertexSize();
    if (size > SWATCH_SIZE) size = SWATCH_SIZE;
    
    int margin = (SWATCH_SIZE - size) / 2;
    
    Rectangle box = new Rectangle(
        x + margin, y + margin,
        size, size);

    Color clr = layer.getLayerStyle().getVertexColor();  
    g.setPaint(clr);
    g.fill(box);
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
  
  private Rectangle computeBox(List<Layer> layerList, Graphics2D g) {
    int width = entryWidth(layerList, g) + 2 * BOX_MARGIN + SWATCH_SIZE + SWATCH_MARGIN;
    
    int lineHeight = DEFAULT_FONT_SIZE + 4;
    int height = layerList.size() * lineHeight + 2 * BOX_MARGIN;
    
    int viewHeight = (int) viewport.getHeightInView();
    int viewWidth = (int) viewport.getWidthInView();
    Rectangle box = new Rectangle(
        viewWidth - BOX_OFFSET - width, 
        viewHeight - BOX_OFFSET - height,
        width, height);
    return box;
  }

  private int entryWidth(List<Layer> layerList, Graphics2D g2) {
    int width = 0;
    for (Layer layer : layerList) {
      String s = getDescription(layer);
      int nameWidth = (int) g2.getFontMetrics().getStringBounds(s, g2).getWidth();
      if (nameWidth > width) 
        width = nameWidth;
    }
    return width;
  }







}
