/*
 * Copyright (c) 2016 Martin Davis.
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


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.locationtech.jtstest.testbuilder.controller.CommandController;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;


/**
 * @version 1.7
 */
public class CommandPanel 
extends JPanel 
{
	private JTextArea txtCmd;
  private JTextArea txtErr;

  public CommandPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
  void jbInit() throws Exception {
    
    this.setLayout(new BorderLayout());

    JPanel textPanel = new JPanel();
    textPanel.setLayout(new BorderLayout());
    
    txtCmd = new JTextArea();
    txtCmd.setWrapStyleWord(true);
    txtCmd.setLineWrap(true);
    //txtResult.setBackground(AppColors.BACKGROUND);

    JScrollPane jScrollPane = new JScrollPane();
    jScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    jScrollPane.getViewport().add(txtCmd, null);
    
    txtErr = new JTextArea();
    txtErr.setWrapStyleWord(true);
    txtErr.setLineWrap(true);
    txtErr.setBackground(AppColors.BACKGROUND);
    txtErr.setEditable(false);
    txtErr.setPreferredSize(new Dimension(100,60));

    JScrollPane jScrollPaneErr = new JScrollPane();
    jScrollPaneErr.setBorder(BorderFactory.createLoweredBevelBorder());
    jScrollPaneErr.getViewport().add(txtErr, null);
    
    textPanel.add(jScrollPane, BorderLayout.CENTER);
    textPanel.add(jScrollPaneErr, BorderLayout.SOUTH);
    
    
    JButton btnRun = SwingUtil.createButton(AppIcons.EXECUTE, "Run Command", new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        txtErr.setText("");
        txtErr.repaint();
        CommandController.execCommand( txtCmd.getText() );
      }
    });
    
    /*
    Box btnPanel = Box.createVerticalBox();
    btnPanel.setPreferredSize(new java.awt.Dimension(30, 30));
    //btnPanel.add(btnRun);
    this.add(btnPanel, BorderLayout.EAST);
    */
    
    JLabel lblCommand = new JLabel();
    lblCommand.setText("Command");
    lblCommand.setBorder(new EmptyBorder(2,2,2,20));//top,left,bottom,right
    
    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
    labelPanel.setBorder(BorderFactory.createEmptyBorder(0,4,2,2));
    labelPanel.add(lblCommand);
    labelPanel.add(btnRun);


    this.add(labelPanel, BorderLayout.NORTH);
    this.add(textPanel, BorderLayout.CENTER);
  }
  
  public void setError(String msg) {
    txtErr.setText(msg);
    // scroll to top
    txtErr.setCaretPosition(0);
  }


  
}
