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

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.locationtech.jtstest.*;
import org.locationtech.jtstest.testbuilder.AppCursors;
import org.locationtech.jtstest.testbuilder.GeometryEditPanel;


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
