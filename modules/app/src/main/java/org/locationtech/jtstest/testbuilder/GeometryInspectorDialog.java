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
    this(frame, "Geometry Inspector", false);
  }
  
  void initUI() throws Exception
  {
    inspectPanel = new InspectorPanel(false);
    getContentPane().add(inspectPanel);
  }

  public void setGeometry(int geomIndex, Geometry geometry) {
    String tag = geomIndex == 0 ? AppStrings.GEOM_LABEL_A : AppStrings.GEOM_LABEL_B;
    inspectPanel.setGeometry(tag, geometry, geomIndex, false);
  }

  public void setGeometry(String tag, Geometry geom, int index, boolean isEditable) {
    inspectPanel.setGeometry(tag, geom, index, false);
  }

}
