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

import org.locationtech.jts.geom.*;
import org.locationtech.jtstest.*;
import org.locationtech.jtstest.testbuilder.ui.Viewport;
import org.locationtech.jtstest.testbuilder.ui.render.GeometryPainter;


public class BasicStyle implements Style
{
  private static final float NO_STROKE = -1.0f;
  
  private Color lineColor;
  private Color fillColor;
  private float strokeWidth = 1;
  private boolean isDashed = false;
  private float[] dashes = { 5 };

  public BasicStyle(Color lineColor, Color fillColor) {
    this.lineColor = lineColor;
    this.fillColor = fillColor;
  }

  public BasicStyle() {
  }

  public void paint(Geometry geom, Viewport viewport, Graphics2D g)
  {
    if (strokeWidth == NO_STROKE) {
      GeometryPainter.paint(geom, viewport, g, lineColor, fillColor);
    }
    else {
      GeometryPainter.paint(geom, viewport, g, lineColor, fillColor, createStroke());
    }
  }
  
  private Stroke createStroke() {
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
    return lineColor;
  }

  public void setLineColor(Color color) {
    lineColor = color;
  }

  public Color getFillColor() {
    return fillColor;
  }

  public void setFillColor(Color color) {
    fillColor = color;
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
