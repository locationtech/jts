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
package org.locationtech.jtstest.testbuilder;


import java.awt.*;
import java.awt.event.*;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jtstest.testbuilder.geom.*;
import org.locationtech.jtstest.testbuilder.model.*;


/**
 * @version 1.7
 */
public class StatsPanel 
extends JPanel 
{
	TestBuilderModel tbModel = null;
	
	JScrollPane jScrollPane1 = new JScrollPane();
	JTextArea txtStats = new JTextArea();
  BorderLayout tabPanelLayout = new BorderLayout();
	
	public StatsPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
  void jbInit() throws Exception {
    
    this.setLayout(tabPanelLayout);

    txtStats.setWrapStyleWord(true);
    txtStats.setLineWrap(true);
    txtStats.setBackground(SystemColor.control);

    this.add(jScrollPane1, BorderLayout.CENTER);
    
    jScrollPane1.setBorder(BorderFactory.createLoweredBevelBorder());
    jScrollPane1.getViewport().add(txtStats, null);
  }
  
	public void setModel(TestBuilderModel tbModel)
	{
		this.tbModel = tbModel;
	}
	
  public void refresh()
  {
    StringBuffer buf = new StringBuffer();

    writeGeomStats("A", tbModel.getCurrentTestCaseEdit().getGeometry(0), buf);
    writeGeomStats("B", tbModel.getCurrentTestCaseEdit().getGeometry(1), buf);
    writeGeomStats("Result", tbModel.getCurrentTestCaseEdit().getResult(), buf);
    
    setString(buf.toString());
  }
  
  private void writeGeomStats(String label,
      Geometry g, StringBuffer buf)
  {
    if (g == null) return;
    buf.append(label + " : ");
    buf.append(GeometryUtil.structureSummary(g));
    buf.append("\n");
    buf.append("    Length = " + g.getLength() + "    Area = " + g.getArea() + "\n");
    buf.append("\n");
  }
  
  private void setString(String s)
  {
    txtStats.setText(s);
  }

  
}
