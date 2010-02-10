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
package com.vividsolutions.jtstest.testbuilder;

import com.vividsolutions.jtstest.testbuilder.model.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
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
