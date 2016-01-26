/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtstest.testbuilder.ui.tools;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import com.vividsolutions.jtstest.*;
import com.vividsolutions.jtstest.testbuilder.AppCursors;
import com.vividsolutions.jtstest.testbuilder.GeometryEditPanel;

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
    pan(panel(), source, destination);
  }

  public static void pan(GeometryEditPanel panel, Point2D source, Point2D destination ) {
    double xDisplacement = destination.getX() - source.getX();
    double yDisplacement = destination.getY() - source.getY();
    panel.zoomPan(xDisplacement, yDisplacement);
  }

}
