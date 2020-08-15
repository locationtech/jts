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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.locationtech.jts.awt.FontGlyphReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jtstest.testbuilder.geom.ConstrainedInteriorPoint;
import org.locationtech.jtstest.testbuilder.ui.GraphicsUtil;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


public class DataLabelStyle implements Style
{
  private Color color;
  private int size = 12;
  private Font font = new Font(FontGlyphReader.FONT_SANSSERIF, Font.BOLD, 12);

  public DataLabelStyle(Color color) {
    this.color = color;
  }

  public DataLabelStyle() {
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
    font = new Font(FontGlyphReader.FONT_SANSSERIF, Font.BOLD, size);
  }

  public void paint(Geometry geom, Viewport viewport, Graphics2D g2d)
  {
    if (geom.getUserData() == null) return;
    g2d.setColor(color);
    g2d.setFont(font);
    
    String label = geom.getUserData().toString();
    
    if (geom instanceof Polygon) {
      paintLabelPolygon(label, geom, viewport, g2d);
    }
    else if (geom instanceof LineString) {
      paintLabelLine(label, geom, viewport, g2d);
    }
    else {
      paintLabel(label, geom, viewport, g2d);
    }
  }
  
  private void paintLabelPolygon(String label, Geometry geom, Viewport viewport, Graphics2D g2d) {
    Coordinate origin = ConstrainedInteriorPoint.getCoordinate((Polygon) geom, viewport.getModelEnv());
    Point2D vp = viewport.toView(new Point2D.Double(origin.x, origin.y));
    GraphicsUtil.drawStringAlignCenter(g2d, label, (int) vp.getX(), (int) vp.getY()); 
  }

  private void paintLabel(String label, Geometry geom, Viewport viewport, Graphics2D g2d) {
    Coordinate origin = geom.getInteriorPoint().getCoordinate();
    Point2D vp = viewport.toView(new Point2D.Double(origin.x, origin.y));
    GraphicsUtil.drawStringAlignCenter(g2d, label, (int) vp.getX(), (int) vp.getY()); 
  }

  private void paintLabelLine(String label, Geometry line, Viewport viewport, Graphics2D g2d) {
    LineSegment baseline = LineLabelBaseline.getBaseline((LineString) line, viewport.getModelEnv());
    if (baseline == null) return;
    
    Coordinate origin = baseline.p0;
    Point2D vpOrigin = viewport.toView(new Point2D.Double(origin.x, origin.y));
    
    Coordinate dirPt = baseline.p1;
    Point2D vpDir = viewport.toView(new Point2D.Double(dirPt.x, dirPt.y));
    
    double dx = vpDir.getX() - vpOrigin.getX();
    double dy = vpDir.getY() - vpOrigin.getY();

    double offsetLen = 15;
    double nudgeX = 5;
    
    double dirVecLen = Math.sqrt(dx*dx + dy*dy);
    
    double offsetX = offsetLen * dx / dirVecLen;
    double offsetY = offsetLen * dy / dirVecLen;
    offsetX += dx > 0 ? nudgeX : -nudgeX;

    float alignX = offsetX < 0 ? 1 : 0;
    float alignY = offsetY < 0 ? 0 : 1;
    
    Point2D vp = new Point2D.Double(vpOrigin.getX() + offsetX, vpOrigin.getY() + offsetY);
    
    GraphicsUtil.drawStringAlign(g2d, label, (int) vp.getX(), (int) vp.getY(), alignX, alignY); 

  }

}
