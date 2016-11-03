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
