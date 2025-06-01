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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.Memory;
import org.locationtech.jtstest.testbuilder.model.TestBuilderModel;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.util.ExceptionFormatter;


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

  JPanel panelLHBtns = new JPanel();
  JButton copyButton = new JButton();
  JButton copyToTestButton = new JButton();
	JPanel rButtonPanel = new JPanel();
//  FlowLayout rButtonPanelLayout = new FlowLayout();
  GridLayout rButtonPanelLayout = new GridLayout();
  BorderLayout rPanelLayout = new BorderLayout();
  BorderLayout tabPanelLayout = new BorderLayout();
		
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
    
    JButton copyButton = SwingUtil.createButton(AppIcons.COPY, "Copy Result (Ctl-click for formatted)", 
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rCopyButton_actionPerformed(e);
      }
    });
    JButton copyToTestButton = SwingUtil.createButton(AppIcons.COPY_TO_TEST, "Copy Result to new Test",
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JTSTestBuilderFrame.instance().copyResultToTest();
      }
    });
    JButton btnClearResult = SwingUtil.createButton(AppIcons.CUT, "Clear Result",         
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JTSTestBuilder.controller().resultClear();
      }
    });

    
    rButtonPanelLayout = new GridLayout(3,1);
    rButtonPanelLayout.setVgap(1);
    rButtonPanelLayout.setHgap(1);
    rButtonPanel.setLayout(rButtonPanelLayout);
    rButtonPanel.add(copyButton);
    rButtonPanel.add(copyToTestButton);
    rButtonPanel.add(btnClearResult);
    
    panelLHBtns.setLayout(rPanelLayout);
    panelLHBtns.add(rButtonPanel, BorderLayout.NORTH);
    
    txtResult.setWrapStyleWord(true);
    txtResult.setLineWrap(true);
    txtResult.setBackground(AppColors.BACKGROUND);
    
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

    //-------------------------------------
    JButton btnInspect = SwingUtil.createButton(AppIcons.GEOM_INSPECT, "Inspect", new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JTSTestBuilder.controller().inspectResult();
      }        
    });

    Box panelRHBtns = Box.createVerticalBox();
    panelRHBtns.setPreferredSize(new java.awt.Dimension(30, 30));
    panelRHBtns.add(btnInspect);

    this.add(jScrollPane1, BorderLayout.CENTER);
    this.add(labelPanel, BorderLayout.NORTH);
    this.add(panelLHBtns, BorderLayout.WEST);
    this.add(panelRHBtns, BorderLayout.EAST);
    
    
    jScrollPane1.getViewport().add(txtResult, null);

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

  public void setResult(Object o)
  {
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
    txtResult.setBackground(AppColors.BACKGROUND);
  }
  
  private void setString(String s)
  {
    txtResult.setText(s);
    txtResult.setBackground(AppColors.BACKGROUND);
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
