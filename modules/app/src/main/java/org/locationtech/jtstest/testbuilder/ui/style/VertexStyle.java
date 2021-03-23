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
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.testbuilder.AppConstants;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


public class VertexStyle  implements Style
{
  public static final int SYM_SQUARE_SOLID = 0;
  public static final int SYM_SQUARE_HOLLOW = 1;
  public static final int SYM_CIRCLE_SOLID = 2;
  public static final int SYM_CIRCLE_HOLLOW = 3;
  
  private int size = AppConstants.VERTEX_SIZE;
  private double sizeOver2 =  size / 2d;
  
  protected Rectangle shape;
  private Color color;
  private int symbol = SYM_SQUARE_SOLID;
  private Stroke stroke;
  
  
  // reuse point objects to avoid creation overhead
  private Point2D pM = new Point2D.Double();
  private Point2D pV = new Point2D.Double();

  public VertexStyle(Color color) {
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
  public int getSymbol() {
    return symbol;
  }
  public void setSymbol(int sym) {
    this.symbol = sym;
    init();
  }
  private void init() {
    sizeOver2 = size / 2d;
    shape = new Rectangle(0, 0, size, size);
    float strokeSize = size / 4;
    if (strokeSize < 1) strokeSize = 1;
    stroke = new BasicStroke(strokeSize);
  }

  public void paint(Geometry geom, Viewport viewport, Graphics2D g)
  {
    g.setPaint(color);
    g.setStroke(stroke);
    
    Coordinate[] coordinates = geom.getCoordinates();
    
    for (int i = 0; i < coordinates.length; i++) {
        if (! viewport.containsInModel(coordinates[i])) {
            //Otherwise get "sun.dc.pr.PRException: endPath: bad path" exception 
            continue;
        }       
        pM.setLocation(coordinates[i].x, coordinates[i].y);
        viewport.toView(pM, pV);
      	//shape.setLocation((int) (pV.getX() - sizeOver2), (int) (pV.getY() - sizeOver2));
        //g.fill(shape);
        int x = (int) (pV.getX() - sizeOver2);
        int y = (int) (pV.getY() - sizeOver2);
        switch (symbol) {
        case SYM_SQUARE_SOLID: 
          g.fillRect(x, y, size, size);
          break;
        case SYM_SQUARE_HOLLOW: 
          g.drawRect(x, y, size, size);
          break;
        case SYM_CIRCLE_SOLID:
          g.fillOval(x, y, size, size);
          break;
        case SYM_CIRCLE_HOLLOW:
          g.drawOval(x, y, size, size);
          break;
        }
    }
  }
  
}
