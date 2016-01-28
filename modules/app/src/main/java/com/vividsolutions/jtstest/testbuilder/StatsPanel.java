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
package com.vividsolutions.jtstest.testbuilder;

import com.vividsolutions.jtstest.testbuilder.model.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import com.vividsolutions.jtstest.testbuilder.geom.*;

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
