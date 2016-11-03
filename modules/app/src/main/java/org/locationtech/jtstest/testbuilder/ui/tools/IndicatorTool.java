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

package org.locationtech.jtstest.testbuilder.ui.tools;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import org.locationtech.jts.awt.FontGlyphReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jtstest.*;
import org.locationtech.jtstest.testbuilder.AppConstants;
import org.locationtech.jtstest.testbuilder.model.*;


public abstract class IndicatorTool extends BasicTool
{
  private Color bandColor = AppConstants.BAND_CLR;
  
  private Point mousePoint;
  private Shape lastShapeDrawn;
  private String lastLabelDrawn = null;
  private Point lastLabelLoc = null;

  private boolean isIndicatorVisible = false;
  private Color originalColor;
  private Stroke originalStroke;
  private Font originalFont;

  public IndicatorTool() {
    super();
  }

  public IndicatorTool(Cursor cursor) {
    super(cursor);
  }

  /**
   * Gets the shape for displaying the current state of the action.
   * Subclasses should override.
   * 
   * @return null if nothing should be drawn
   */
  protected Shape getShape()
  {
    return null;
  }

  /**
   * Important for XOR drawing. Even if #getShape returns null, this method
   * will return true between calls of #redrawShape and #clearShape.
   */
  public boolean isIndicatorVisible() {
    return isIndicatorVisible;
  }

  private void setIndicatorVisible(boolean isIndicatorVisible) {
    this.isIndicatorVisible = isIndicatorVisible;
  }

  protected void clearIndicator() {
    clearShape(getGraphics2D());
  }

  protected void redrawIndicator() 
  {
    try {
      redrawShape(getGraphics2D());
    }
    catch (Exception ex) {
      // no other way to handle exception
      ex.printStackTrace();
    }
  }

  private void clearShape(Graphics2D graphics) {
    if (!isIndicatorVisible) {
      return;
    }
    drawShapeXOR(graphics, lastShapeDrawn, lastLabelDrawn, lastLabelLoc);
    setIndicatorVisible(false);
  }

  private void redrawShape(Graphics2D graphics) throws Exception {
    clearShape(graphics);
    drawShapeXOR(graphics);
    setIndicatorVisible(true);
  }

  private void drawShapeXOR(Graphics2D g) throws Exception {
    Shape newShape = getShape();
    String label = getLabel();
    drawShapeXOR(g, newShape, label, mousePoint);
    lastShapeDrawn = newShape;
    lastLabelDrawn = label;
    lastLabelLoc = mousePoint;
  }

  private void drawShapeXOR(Graphics2D graphics, Shape shape, String label, Point labelLoc) {
    setup(graphics);
    try {
      if (shape != null) {
        graphics.draw(shape);
      }
      /*
       // TODO: make this work
      if (label != null)
        graphics.drawString(label, labelLoc.x, labelLoc.y);
*/
    } 
    finally {
      teardown(graphics);
    }
  }

  private void setup(Graphics2D graphics) {
    originalColor = graphics.getColor();
    originalStroke = graphics.getStroke();
    originalFont = graphics.getFont();
    graphics.setFont(new Font(FontGlyphReader.FONT_SANSSERIF, Font.PLAIN, 14));
    graphics.setColor(bandColor);
    graphics.setXORMode(Color.white);
  }

  private void teardown(Graphics2D graphics) {
    graphics.setPaintMode();
    graphics.setColor(originalColor);
    graphics.setStroke(originalStroke);
    graphics.setFont(originalFont);
  }

  /*
  protected void setStroke(Stroke stroke) {
    this.stroke = stroke;
  }
*/
  
  private void recordLabel(Point p)
  {
    mousePoint = new Point(p.x + 5, p.y);
  }
  
  private String getLabel()
  {
    if (mousePoint == null) return null;
    return mousePoint.x + "," + mousePoint.y;
  }
  
//  protected void gestureFinished() throws Exception;

  public void mouseDragged(MouseEvent e) 
  {
    recordLabel(e.getPoint());    
  }

  public void mouseMoved(MouseEvent e) {
    recordLabel(e.getPoint());
  }


}
