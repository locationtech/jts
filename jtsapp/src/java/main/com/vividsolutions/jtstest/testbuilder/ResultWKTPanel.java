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
import com.vividsolutions.jtstest.testbuilder.ui.SwingUtil;
import com.vividsolutions.jtstest.util.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.*;

/**
 * @version 1.7
 */
public class ResultWKTPanel 
extends JPanel 
{
	TestBuilderModel tbModel = null;
	String opName;
	
	JScrollPane jScrollPane1 = new JScrollPane();
	JTextArea txtResult = new JTextArea();
	
  JPanel labelPanel = new JPanel();
  JLabel functionLabel = new JLabel();
  JLabel timeLabel = new JLabel();
  JLabel memoryLabel = new JLabel();
  GridLayout labelPanelLayout = new GridLayout(1,3);

  JPanel rPanel = new JPanel();
  JButton copyButton = new JButton();
  JButton copyToTestButton = new JButton();
	JPanel rButtonPanel = new JPanel();
//  FlowLayout rButtonPanelLayout = new FlowLayout();
  GridLayout rButtonPanelLayout = new GridLayout();
  BorderLayout rPanelLayout = new BorderLayout();
  BorderLayout tabPanelLayout = new BorderLayout();
	
  private final ImageIcon copyIcon = new ImageIcon(this.getClass().getResource("Copy.gif"));
  private final ImageIcon copyToTestIcon = new ImageIcon(this.getClass().getResource("CopyToTest.png"));
	
	public ResultWKTPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
  void jbInit() throws Exception {
    
    this.setLayout(tabPanelLayout);
   
    jScrollPane1.setBorder(BorderFactory.createLoweredBevelBorder());
    
    copyButton.setToolTipText("Copy Result (Ctl-click for formatted)");
    copyButton.setIcon(copyIcon);
    copyButton.setMargin(new Insets(0, 0, 0, 0));
    
    copyToTestButton.setToolTipText("Copy Result to new Test");
    copyToTestButton.setIcon(copyToTestIcon);
    copyToTestButton.setMargin(new Insets(0, 0, 0, 0));
    
    rButtonPanelLayout = new GridLayout(2,1);
    rButtonPanelLayout.setVgap(1);
    rButtonPanelLayout.setHgap(1);
    rButtonPanel.setLayout(rButtonPanelLayout);
    rButtonPanel.add(copyButton);
    rButtonPanel.add(copyToTestButton);
    
    rPanel.setLayout(rPanelLayout);
    rPanel.add(rButtonPanel, BorderLayout.NORTH);
    
    txtResult.setWrapStyleWord(true);
    txtResult.setLineWrap(true);
    txtResult.setBackground(SystemColor.control);
    
    labelPanel.setLayout(labelPanelLayout);
    //labelPanel.setBorder(BorderFactory.createEmptyBorder(0,4,2,2));
    labelPanel.add(functionLabel);
    labelPanel.add(timeLabel);
    labelPanel.add(memoryLabel);
    
    functionLabel.setText(" ");
    functionLabel.setHorizontalAlignment(SwingConstants.CENTER);
//    functionLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    functionLabel.setToolTipText("Result Info");

    timeLabel.setFont(new Font("SanSerif", Font.BOLD, 16));
    timeLabel.setText(" ");
    timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    timeLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    timeLabel.setToolTipText("Execution Time");

    memoryLabel.setText(" ");
    memoryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    memoryLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    memoryLabel.setToolTipText("JVM Memory Usage");

    this.add(jScrollPane1, BorderLayout.CENTER);
    this.add(labelPanel, BorderLayout.NORTH);
    this.add(rPanel, BorderLayout.EAST);
    
    
    jScrollPane1.getViewport().add(txtResult, null);
    
    copyButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            rCopyButton_actionPerformed(e);
          }
        });
    copyToTestButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            copyToTestButton_actionPerformed(e);
          }
        });
  }
  
	public void setModel(TestBuilderModel tbModel)
	{
		this.tbModel = tbModel;
	}
	
  public void setOpName(String opName)
  {
      this.opName = opName;
  }
  
  public void setRunningTime(String time)
  {
    setExecutedTime(time);
  }
  
  public void setExecutedTime(String time)
  {
    functionLabel.setText(opName);
    timeLabel.setText(time);
    memoryLabel.setText(Memory.usedTotalString());   
  }
  
  public void updateResult()
  {
  	Object o = tbModel.getResult();
    if (o == null) {
      setString("");
    }
    else if (o instanceof Geometry) {
      setGeometry((Geometry) o);
    }
    else if (o instanceof Throwable) {
      setError((Throwable) o);
    }
    else {
      setString(o.toString());
    }
  }
  
  public void clearResult()
  {
    functionLabel.setText("");
    setString("");
  }
  
  private void setGeometry(Geometry g)
  {
    String  str = tbModel.getResultDisplayString(g);
    txtResult.setText(str);
    txtResult.setBackground(SystemColor.control);
  }
  
  private void setString(String s)
  {
    txtResult.setText(s);
    txtResult.setBackground(SystemColor.control);
  }
  
  private void setError(Throwable ex)
  {
    String exStr = ExceptionFormatter.getFullString(ex);
    txtResult.setText(exStr);
    txtResult.setBackground(Color.pink);
  }
  
  void rCopyButton_actionPerformed(ActionEvent e) {
    boolean isFormatted = 0 != (e.getModifiers() & ActionEvent.CTRL_MASK);
    tbModel.copyResult(isFormatted);
  }
  
  void copyToTestButton_actionPerformed(ActionEvent e) {
    JTSTestBuilderFrame.instance().copyResultToTest();
  }
  
}
