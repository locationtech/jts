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

import org.locationtech.jts.geom.*;
import org.locationtech.jtstest.*;
import org.locationtech.jtstest.testbuilder.AppConstants;
import org.locationtech.jtstest.testbuilder.ui.Viewport;


public class VertexStyle  implements Style
{
  private int size = AppConstants.VERTEX_SIZE;
  private double sizeOver2 =  size / 2d;
  
  protected Rectangle shape;
  private Color color;
  
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
  private void init() {
    sizeOver2 = size / 2d;
    shape = new Rectangle(0, 0, size, size);
  }

  public void paint(Geometry geom, Viewport viewport, Graphics2D g)
  {
    g.setPaint(color);
    Coordinate[] coordinates = geom.getCoordinates();
    
    for (int i = 0; i < coordinates.length; i++) {
        if (! viewport.containsInModel(coordinates[i])) {
            //Otherwise get "sun.dc.pr.PRException: endPath: bad path" exception 
            continue;
        }       
        pM.setLocation(coordinates[i].x, coordinates[i].y);
        viewport.toView(pM, pV);
      	shape.setLocation((int) (pV.getX() - sizeOver2), (int) (pV.getY() - sizeOver2));
        g.fill(shape);
    }
  }
  
}
