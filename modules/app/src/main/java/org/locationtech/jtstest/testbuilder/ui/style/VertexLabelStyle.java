/*
 * Copyright (c) 2019 Martin Davis.
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
import org.locationtech.jts.io.OrdinateFormat;
import org.locationtech.jtstest.testbuilder.ui.GraphicsUtil;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


public class VertexLabelStyle  implements Style
{
  private static final int DEFAULT_FONT_SIZE = 11;

  private static final int LABEL_OFFSET = 4;

  private static final double MIN_VEW_DIST_SQ = 900;

  private Font font = new Font(FontGlyphReader.FONT_SERIF, Font.PLAIN, DEFAULT_FONT_SIZE);
  
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
    
    int len = coordinates.length;
    // don't label duplicate closing point
    if (len > 1 && coordinates[0].equals2D(coordinates[len - 1])) len--;
    
    Point2D lastDrawnPV = new Point2D.Double();;
    for (int i = 0; i < len; i++) {
      Coordinate pt = coordinates[i];
      if (! viewport.containsInModel(pt)) {
          continue;
      }       
      pM.setLocation(pt.x, pt.y);
      viewport.toView(pM, pV);
      if (i > 0 && isTooClose(lastDrawnPV, pV)) continue;
      lastDrawnPV.setLocation(pV);
      
      String label = format(pt);
      
      int dir = 2;  // Use N for points
      if (len > 1) {
        Coordinate p1 = coordinates[ i <= 0 ? 0 : i-1 ];
        Coordinate p2 = coordinates[ (i >= len - 1) ? len - 2 : i+1 ];
        dir = labelDirection(pt, p1, p2);
      }
      /*
      System.out.println( pt + "   dir= " + dir + "  " 
          + DIR_ALIGN[dir][0] + "  " + DIR_ALIGN[dir][1] );
      */
      GraphicsUtil.drawStringAlign(g, label, (int) pV.getX(), (int) pV.getY(),
          DIR_ALIGN[dir][0], DIR_ALIGN[dir][1], LABEL_OFFSET );
    }
  }
  
  private static String format(Coordinate p) {
    return OrdinateFormat.DEFAULT.format(p.x) + "," + OrdinateFormat.DEFAULT.format(p.y);
  }
  
  private boolean isTooClose(Point2D p0, Point2D p1) {
    double dx = p1.getX() - p0.getX();
    double dy = p1.getY() - p0.getY();
    
    double len = dx * dx + dy * dy;
    
    return len < MIN_VEW_DIST_SQ;
  }

  private static float[][] DIR_ALIGN = {
      { 0, 0.5f },  // 0 - E
      { 0, 0 },     // 1 - NE
      { 0.5f, 0 },  // 2 - N
      { 1, 0 },     // 3 - NW
      { 1, 0.5f },  // 4 - W
      { 1, 1 },     // 5 - SW
      { 0.5f, 1 },  // 6 - S
      { 0, 1 }      // 7 - SE
  };
  
  private int labelDirection(Coordinate pt, Coordinate p1, Coordinate p2) {
    double dx1 = p1.getX() - pt.getX();
    double dy1 = p1.getY() - pt.getY();
    double dx2 = p2.getX() - pt.getX();
    double dy2 = p2.getY() - pt.getY();
    
    if (dx1 <= 0 && dx2 <= 0) return 0;
    if (dx1 >= 0 && dx2 >= 0) return 4;
    if (dy1 <= 0 && dy2 <= 0) return 2;
    if (dy1 >= 0 && dy2 >= 0) return 6;
    
    // diagonal
    // TODO: better positioning for diagonals
    return 0;
  }
}
