/*
 * Copyright (c) 2016 Martin Davis.
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.locationtech.jtstest.testbuilder.controller.CommandController;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;


/**
 * @version 1.7
 */
public class CommandPanel 
extends JPanel 
{
	private JTextArea txtCmd;
  private JTextArea txtOutput;

  private List<String> commandLog = new ArrayList<String>();
  
  private int historyIndex = 1;
  protected String commandSave;
  private boolean isCommandSavedOnUpdate;
  private JLabel lblCommand;
  private JCheckBox cbSupplyStdin;
  private JRadioButton rbWKT;
  private JRadioButton rbWKB;
  
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
    // save command whenever it is changed
    txtCmd.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        save();
      }
      public void removeUpdate(DocumentEvent e) {
        //save();
      }
      public void insertUpdate(DocumentEvent e) {
        save();
      }

      public void save() {
        /**
         * If the change occurred via internal setting, 
         * don't copy to the save buffer.
         */
        if (! isCommandSavedOnUpdate) {
          isCommandSavedOnUpdate = true;
          return;
        }
        
        commandSave = txtCmd.getText();
        historyIndex = -1;
        update();
      }
    });

    JScrollPane jScrollPane = new JScrollPane();
    jScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    jScrollPane.getViewport().add(txtCmd, null);
    
    txtOutput = new JTextArea();
    txtOutput.setWrapStyleWord(true);
    txtOutput.setLineWrap(true);
    txtOutput.setBackground(AppColors.BACKGROUND);
    txtOutput.setEditable(false);
    txtOutput.setPreferredSize(new Dimension(100,60));

    JScrollPane jScrollPaneErr = new JScrollPane();
    jScrollPaneErr.setBorder(BorderFactory.createLoweredBevelBorder());
    jScrollPaneErr.getViewport().add(txtOutput, null);
    
    textPanel.add(jScrollPane, BorderLayout.CENTER);
    textPanel.add(jScrollPaneErr, BorderLayout.SOUTH);
    
    
    JButton btnRun = SwingUtil.createButton(AppIcons.EXECUTE, "Run Command", new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doRun();
      }
    });
    
    JButton btnPaste = SwingUtil.createButton(AppIcons.PASTE, "Paste Command", new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setCommandText(getPaste());
        update();
      }
    });
    
    JButton btnClear = SwingUtil.createButton(AppIcons.CUT, "Clear Command", new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setCommandText("");
        update();
      }
    });
    
    JButton btnPrev = SwingUtil.createButton(AppIcons.LEFT, "Previous Command", new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (historyIndex == -1) {
          historyIndex = commandLog.size()-1;
        }
        else if (historyIndex > 0) {
          historyIndex--;
        }
        setCommandTextNoSave(commandLog.get(historyIndex));
        update();
      }
    });
    
    JButton btnNext = SwingUtil.createButton(AppIcons.RIGHT, "Next Command", new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (historyIndex == -1) return;
        if (historyIndex == commandLog.size()-1) {
          historyIndex = -1;
          setCommandTextNoSave(commandSave);
        }
        else {
          historyIndex += 1;
          setCommandTextNoSave(commandLog.get(historyIndex));
        }
        update();
      }
    });
    
    /*
    Box btnPanel = Box.createVerticalBox();
    btnPanel.setPreferredSize(new java.awt.Dimension(30, 30));
    //btnPanel.add(btnRun);
    this.add(btnPanel, BorderLayout.EAST);
    */
    
    lblCommand = new JLabel();
    lblCommand.setText("Command 1");
    lblCommand.setBorder(new EmptyBorder(2,2,2,20));//top,left,bottom,right
    
    JLabel lblVars = new JLabel();
    lblVars.setText("Vars ");
    lblVars.setBorder(new EmptyBorder(2,30,2,2));//top,left,bottom,right
    
    JButton btnAwkt = SwingUtil.createButton("A-WKT", 
        "Insert variable " + CommandController.VAR_A + " for A as WKT", 
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        insertCmdText(CommandController.VAR_A);
      }
    });
    JButton btnAwkb = SwingUtil.createButton("A-WKB", 
        "Insert variable " + CommandController.VAR_A_WKB + " for A as WKB", 
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        insertCmdText(CommandController.VAR_A_WKB);
      }
    });
    JButton btnBwkt = SwingUtil.createButton("B-WKT", 
        "Insert variable " + CommandController.VAR_B + " for B as WKT", 
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        insertCmdText(CommandController.VAR_B);
      }
    });
    JButton btnBwkb = SwingUtil.createButton("B-WKB", 
        "Insert variable " + CommandController.VAR_B_WKB + " for B as WKB", 
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        insertCmdText(CommandController.VAR_B_WKB);
      }
    });
    JLabel lblStdin = new JLabel();
    lblStdin.setText("Stdin ");
    lblStdin.setBorder(new EmptyBorder(2,10,2,0));//top,left,bottom,right
    
    cbSupplyStdin = new JCheckBox();
    cbSupplyStdin.setToolTipText("Send A to command standard input");
    rbWKT = new JRadioButton();
    rbWKT.setToolTipText("Send A to stdin in WKT");
    rbWKT.setSelected(true);
    rbWKT.setText("WKT");
    rbWKB = new JRadioButton();
    rbWKB.setToolTipText("Send A to stdin in WKB");
    rbWKB.setText("WKB");
    ButtonGroup btnGrpStdInFormat = new ButtonGroup();
    btnGrpStdInFormat.add(rbWKT);
    btnGrpStdInFormat.add(rbWKB);
    
    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
    labelPanel.setBorder(BorderFactory.createEmptyBorder(0,4,2,2));
    labelPanel.add(lblCommand);
    labelPanel.add(btnRun);
    labelPanel.add(lblVars);
    labelPanel.add(btnAwkt);
    labelPanel.add(btnAwkb);
    labelPanel.add(btnBwkt);
    labelPanel.add(btnBwkb);
    labelPanel.add(lblStdin);
    labelPanel.add(cbSupplyStdin);
    labelPanel.add(rbWKT);
    labelPanel.add(rbWKB);
    

    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
    //btnPanel.setBorder(BorderFactory.createEmptyBorder(0,4,2,2));
    btnPanel.add(btnPaste);
    btnPanel.add(btnClear);
    btnPanel.add(btnPrev);
    btnPanel.add(btnNext);


    this.add(btnPanel, BorderLayout.WEST);
    this.add(labelPanel, BorderLayout.NORTH);
    this.add(textPanel, BorderLayout.CENTER);
  }
  
  private void insertCmdText(String text)
  {
    int pos = txtCmd.getCaretPosition();
    Document doc = txtCmd.getDocument();
    try {
        doc.insertString(pos, text, null);
    }
    catch (BadLocationException e) {
      // TODO - ok to ignore this exception?
      e.printStackTrace();
    }
    // help user by switching focus to inserted text
    boolean gotFocus = txtCmd.requestFocusInWindow();
  }

  
  private void update() {
    lblCommand.setText(getCommandName());
  }
  
  public String getCommandName() {
    int commandIndex = historyIndex + 1;
    if (commandIndex <= 0) commandIndex = commandLog.size() + 1;
    return "Command " + commandIndex;
  }
  
  private void doRun() {
    txtOutput.setText("");
    txtOutput.setBackground(AppColors.BACKGROUND);
    txtOutput.repaint();
    String cmd = txtCmd.getText();
    boolean isSupplyStdin =  cbSupplyStdin.isSelected();
    boolean isStdInWKT = rbWKT.isSelected();
    // do not save on every run, only on success
    //log(cmd);
    CommandController.execCommand( getCommandName(), cmd, isSupplyStdin, isStdInWKT );
  }

  private String getPaste() {
    Object obj = SwingUtil.getFromClipboard();
    if ( obj instanceof String ) {
      return (String) obj;
    }
    return "";
  }
  
  public void setError(String msg) {
    txtOutput.setText(msg);
    // scroll to top
    txtOutput.setCaretPosition(0);
    txtOutput.setBackground(AppColors.BACKGROUND_ERROR);
  }
  public void setOutput(String msg) {
    txtOutput.setText(msg);
    // scroll to top
    txtOutput.setCaretPosition(0);
    txtOutput.setBackground(AppColors.BACKGROUND);
  }
  
  private String getCommandText() {
    return txtCmd.getText();
  }
  private void setCommandText(String cmd) {
    isCommandSavedOnUpdate = true;
    commandSave = cmd;
    txtCmd.setText(cmd);
  }
  private void setCommandTextNoSave(String cmd) {
    isCommandSavedOnUpdate = false;
    txtCmd.setText(cmd);
  }
  
  /**
   * Record the command in history, but only if it is different to ones already there
   * @param cmd
   */
  public void saveCommand(String cmd) {
    if (commandLog.contains(cmd)) return;
    commandLog.add(cmd);
  }
  
}
