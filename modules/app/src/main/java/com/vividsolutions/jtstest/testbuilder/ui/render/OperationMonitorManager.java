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

package com.vividsolutions.jtstest.testbuilder.ui.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.locationtech.jts.geom.*;

import com.vividsolutions.jtstest.testbuilder.ui.Viewport;

public class OperationMonitorManager 
{
  public static Geometry indicator = null; 
  
  // testing only
  static {
    GeometryFactory geomFact = new GeometryFactory();
    indicator = geomFact.createLineString(new Coordinate[]{
      new Coordinate(0,0), new Coordinate(100, 10)
    });
  }
  
  private JPanel panel;
  private Viewport viewport;
  
  private Timer repaintTimer = new Timer(50, new ActionListener() 
      {
        public void actionPerformed(ActionEvent e) {
          if (indicator != null) {
            paint();
            return;
          }
        }
      });

  public OperationMonitorManager(JPanel panel, Viewport viewport)
  {
    this.panel = panel;
    this.viewport = viewport;
    // start with a short time cycle to give better appearance
    repaintTimer.setInitialDelay(1000);
    repaintTimer.start();
  }

  private void paint()
  {
    Graphics2D g = (Graphics2D) panel.getGraphics();
    if (g == null) return;
    GeometryPainter.paint(indicator, viewport, g, Color.RED, null);
  }

}
