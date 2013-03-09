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
public class InfoPanel 
extends JPanel 
{
	TestBuilderModel tbModel = null;
	
	JScrollPane jScrollPane1 = new JScrollPane();
	JTextArea txtInfo = new JTextArea();
  BorderLayout tabPanelLayout = new BorderLayout();
  
  StringBuffer text = new StringBuffer();
	
	public InfoPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
  void jbInit() throws Exception {
    
    this.setLayout(tabPanelLayout);

    txtInfo.setWrapStyleWord(true);
    txtInfo.setLineWrap(true);
    txtInfo.setBackground(SystemColor.control);

    this.add(jScrollPane1, BorderLayout.CENTER);
    
    jScrollPane1.setBorder(BorderFactory.createLoweredBevelBorder());
    jScrollPane1.getViewport().add(txtInfo, null);
  }
  
	public void setModel(TestBuilderModel tbModel)
	{
		this.tbModel = tbModel;
	}
	
  public void OLDsetInfo(String s)
  {
    txtInfo.setText(s);
  }
  
  private static final String LOG_SEP = "-------------------------------------------------";
  
  public void setInfo(String s)
  {
    if (s == null || s.length() == 0) s = "";
    txtInfo.setText(s);
  }

  public void addInfo(String s)
  {
    if (s == null || s.length() == 0) return;
    
    if (text.length() != 0) {
      text.append("\n");
      text.append(LOG_SEP);
      text.append("\n");
    }
    text.append(s);
    txtInfo.setText(text.toString());
  }

  
}
