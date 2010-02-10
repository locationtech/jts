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
import com.vividsolutions.jtstest.util.ExceptionFormatter;

import java.awt.*;
import java.awt.event.*;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.vividsolutions.jts.geom.*;

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
