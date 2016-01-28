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
package org.locationtech.jtstest.testbuilder;

import java.awt.Frame;

import javax.swing.JDialog;

import org.locationtech.jts.geom.Geometry;


/**
 * @version 1.7
 */
public class GeometryInspectorDialog extends JDialog
{

  InspectorPanel inspectPanel;
  
  public GeometryInspectorDialog(Frame frame, String title, boolean modal)
  {
    super(frame, title, modal);
    try {
      initUI();
      pack();
      setSize(500, 500);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public GeometryInspectorDialog()
  {
    this(null, "", false);
  }

  public GeometryInspectorDialog(Frame frame)
  {
    this(null, "Geometry Inspector", false);
  }
  
  void initUI() throws Exception
  {
    inspectPanel = new InspectorPanel(false);
    getContentPane().add(inspectPanel);
  }

  public void setGeometry(String tag, Geometry geometry) {
    inspectPanel.setGeometry(tag, geometry, 0);
  }
}
