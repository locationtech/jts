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
import com.vividsolutions.jtstest.util.ExceptionFormatter;

import java.awt.*;
import java.awt.event.*;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.locationtech.jts.geom.*;


/**
 * @version 1.7
 */
public class ResultValuePanel 
extends JPanel 
{
	TestBuilderModel tbModel = null;
  Object currResult = null;
 
  
  JPanel labelPanel = new JPanel();
  JLabel resultLabel = new JLabel();
  BorderLayout labelPanelLayout = new BorderLayout();
  
	JScrollPane jScrollPane1 = new JScrollPane();
	JTextArea txtResult = new JTextArea();
  BorderLayout tabPanelLayout = new BorderLayout();
	
	public ResultValuePanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
  void jbInit() throws Exception {
    
    this.setLayout(tabPanelLayout);

    txtResult.setWrapStyleWord(true);
    txtResult.setLineWrap(true);
    txtResult.setBackground(SystemColor.control);

    labelPanel.setLayout(labelPanelLayout);
    labelPanel.setBorder(BorderFactory.createEmptyBorder(0,4,2,2));
    labelPanel.add(resultLabel);
    resultLabel.setText("Value");

    this.add(jScrollPane1, BorderLayout.CENTER);
    this.add(labelPanel, BorderLayout.NORTH);
    
    jScrollPane1.setBorder(BorderFactory.createLoweredBevelBorder());
    jScrollPane1.getViewport().add(txtResult, null);
  }
  
	public void setModel(TestBuilderModel tbModel)
	{
		this.tbModel = tbModel;
	}
	
  public void setResult(String opName, String execTime, Object o)
  {
    currResult = o;
    resultLabel.setText("Value of: " + opName
        + "    ( " + execTime + " )");
    
    if (o == null) {
      setString("");
    }
    else if (o instanceof Throwable) {
      setError((Throwable) o);
    }
    else {
      setString(o.toString());
    }
  }

  public void setString(String s)
  {
    txtResult.setText(s);
    txtResult.setBackground(SystemColor.control);
  }
  
  public void setError(Throwable ex)
  {
    String exStr = ExceptionFormatter.getFullString(ex);
    txtResult.setText(exStr);
    txtResult.setBackground(Color.pink);
  }

  
}
