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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.Viewport;
import org.locationtech.jtstest.testbuilder.ui.render.GeometryPainter;


public class BasicStyle implements Style
{
  private Color lineColor;
  private int lineAlpha = 255;
  private Color fillColor;
  private int fillAlpha = 150;
  
  private boolean isStroked = true;
  private boolean isFilled = true;
  private float strokeWidth = 1;
  private boolean isDashed = false;
  private float[] dashes = { 5 };

  public BasicStyle(Color lineColor, Color fillColor) {
    this.lineColor = lineColor;
    this.fillColor = fillColor;
  }

  public BasicStyle() {
  }

  public BasicStyle(BasicStyle style) {
    this.lineColor = style.lineColor;
    this.lineAlpha = style.lineAlpha;
    this.fillColor = style.fillColor;
    this.fillAlpha = style.fillAlpha;
    this.isStroked = style.isStroked;
    this.isFilled = style.isFilled;
    this.strokeWidth = style.strokeWidth;
    this.isDashed = style.isDashed;
    this.dashes = style.dashes.clone();
  }

  public BasicStyle copy() {
    return new BasicStyle(this);
  }
  
  public void paint(Geometry geom, Viewport viewport, Graphics2D g)
  {
    Stroke stroke = createStroke();
    Color lineClr = (isStroked && stroke != null) ? getLineColor() : null;
    Color fillClr = isFilled ? getFillColor() : null;
    
    GeometryPainter.paint(geom, viewport, g, lineClr, fillClr, stroke);
  }
  
  private Stroke createStroke() {
    if (strokeWidth <= 0) return null;
    
    if (! isDashed)
      return new BasicStroke(strokeWidth);
    
    dashes = createDashes(strokeWidth);
    return new BasicStroke(strokeWidth, 
        BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, 
        dashes, 0
    );
  }

  private float[] createDashes(float width) {
    float dashSize = 5;
    float len = 2 * dashSize * width;
    float dashFrac = 0.5f;
    return new float[] { (1f - dashFrac) * len, dashFrac * len };
  }

  public Color getLineColor() {
    return ColorUtil.setAlpha(lineColor, lineAlpha);
  }

  public void setLineColor(Color color) {
    lineColor = color;
  }
  
  public void setLineAlpha(int alpha) {
    lineAlpha = alpha;
  }

  public int getLineAlpha() {
    return lineAlpha;
  }
  
  public Color getFillColor() {
    return ColorUtil.setAlpha(fillColor, fillAlpha);
  }

  public void setFillColor(Color color) {
    fillColor = color;
  }

  public void setFillAlpha(int alpha) {
    fillAlpha = alpha;
  }

  public int getFillAlpha() {
    return fillAlpha;
  }

  public boolean isStroked() {
    return isStroked;
  }

  public void setStroked(boolean isStroked) {
    this.isStroked = isStroked;
  }
  
  public boolean isFilled() {
    return isFilled;
  }

  public void setFilled(boolean isFilled) {
    this.isFilled = isFilled;
  }
  
  public float getStrokeWidth() {
    return strokeWidth;
  }

  public void setStrokeWidth(float width) {
    strokeWidth = width;
  }
  
  public boolean isDashed() {
    return isDashed;
  }

  public void setDashed(boolean isDashed) {
    this.isDashed = isDashed;
  }

  public float[] getDashes() {
    return dashes;
  }

  public void setDashes(float[] dashArray) {
    this.dashes = dashArray;
  }


}
