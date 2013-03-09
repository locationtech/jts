/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtstest.testbuilder.ui.tools;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import com.vividsolutions.jtstest.testbuilder.*;

/**
 * @version 1.7
 */
public class PanTool extends BasicTool {
  private static PanTool singleton = null;

  public static PanTool getInstance() {
    if (singleton == null)
      singleton = new PanTool();
    return singleton;
  }

  private Point2D source;

  private PanTool() {
  }

  public Cursor getCursor() {
    return AppCursors.HAND;
  }

  public void activate() {
    source = null;
  }

  public void mousePressed(MouseEvent e) {
    source = toModel(e.getPoint());
  }
  
  public void mouseReleased(MouseEvent e) {
    if (source == null)
      return;
    Point2D destination = toModel(e.getPoint());
    double xDisplacement = destination.getX() - source.getX();
    double yDisplacement = destination.getY() - source.getY();
    panel().zoomPan(xDisplacement, yDisplacement);
    /*
    getViewport().setViewOrigin(getViewport().getViewOriginX() - xDisplacement,
        getViewport().getViewOriginY() - yDisplacement);
        */
  }

}
