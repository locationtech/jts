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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.operation.valid.IsSimpleOp;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;
import org.locationtech.jtstest.testbuilder.event.ValidPanelEvent;
import org.locationtech.jtstest.testbuilder.event.ValidPanelListener;
import org.locationtech.jtstest.testbuilder.model.TestCaseEdit;



/**
 * @version 1.7
 */
public class ValidPanel extends JPanel {
  private static final int TEXT_BOX_WIDTH = 240;
  
  TestCaseEdit testCase;
  private Coordinate markPoint = null;
  
  //===========================================
  JTextField txtIsValid = new JTextField();
  JTextField txtIsSimple = new JTextField();
  JTextArea taInvalidMsg = new JTextArea();
  JLabel lblValidSimple = new JLabel();
  JPanel jPanel1 = new JPanel();
  private transient Vector validPanelListeners;
  GridLayout gridLayout1 = new GridLayout();
  JPanel markPanel = new JPanel();
  JPanel markBtnPanel = new JPanel();
  JTextField txtMarkLocation = new JTextField();
  JTextField txtMarkLabel = new JTextField();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel lblMark = new JLabel();
  JButton btnClearMark = new JButton();
  JButton btnSetMark = new JButton();
  private JCheckBox cbInvertedRingAllowed;

  public ValidPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    JButton btnValidate = new JButton();
    btnValidate.setText("Valid?");
    btnValidate.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnValidate_actionPerformed(e);
      }
    });
    JButton btnSimple = new JButton();
    btnSimple.setText("Simple?");
    btnSimple.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnSimple_actionPerformed(e);
      }
    });
    
    JButton btnClear = new JButton();
    btnClear.setText("Clear");
    btnClear.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearAll();
      }
    });
    
    
    cbInvertedRingAllowed = new JCheckBox();
    cbInvertedRingAllowed.setToolTipText(AppStrings.TIP_ALLOW_INVERTED_RINGS);
    cbInvertedRingAllowed.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbInvertedRingAllowed.setText("Allow Inverted Rings");
    
    txtIsValid.setBackground(AppColors.BACKGROUND);
    txtIsValid.setEditable(false);
    //txtIsValid.setText("Y");
    txtIsValid.setHorizontalAlignment(SwingConstants.CENTER);
    Dimension flagSize = new Dimension(40, 24);
    txtIsValid.setMinimumSize(flagSize);
    txtIsValid.setPreferredSize(flagSize);
    
    txtIsSimple.setBackground(AppColors.BACKGROUND);
    txtIsSimple.setEditable(false);
    txtIsSimple.setHorizontalAlignment(SwingConstants.CENTER);
    txtIsSimple.setMinimumSize(flagSize);
    txtIsSimple.setPreferredSize(flagSize);
    
    taInvalidMsg.setPreferredSize(new Dimension(TEXT_BOX_WIDTH, 80));
    taInvalidMsg.setMaximumSize(new Dimension(TEXT_BOX_WIDTH, 80));
    taInvalidMsg.setMinimumSize(new Dimension(TEXT_BOX_WIDTH, 80));
    
    taInvalidMsg.setLineWrap(true);
    taInvalidMsg.setBorder(BorderFactory.createLoweredBevelBorder());
    taInvalidMsg.setToolTipText("");
    taInvalidMsg.setBackground(AppColors.BACKGROUND);
    taInvalidMsg.setEditable(true);
    taInvalidMsg.setFont(new java.awt.Font("SansSerif", 0, 12));
    
    lblValidSimple.setToolTipText("");
    lblValidSimple.setText("Valid / Simple ");
    
    lblMark.setToolTipText("");
    lblMark.setText("Mark Point ( X Y ) ");
    
    btnClearMark.setToolTipText("");
    btnClearMark.setText("Clear Mark");
    btnClearMark.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearMark();
      }
    });
    
    btnSetMark.setToolTipText("");
    btnSetMark.setText("Set Mark");
    btnSetMark.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnSetMark_actionPerformed(e);
      }
    });
    
    JPanel panelValidSimple = new JPanel();
    panelValidSimple.add(btnValidate);
    panelValidSimple.add(txtIsValid);
    
    JPanel panelSimple = new JPanel();
    panelSimple.add(btnSimple);
    panelSimple.add(txtIsSimple);
   
    JPanel panelMsg = new JPanel();
    panelMsg.add(taInvalidMsg);
    
    JPanel panelClear = new JPanel();
    panelClear.add(btnClear);
    
    jPanel1.setLayout(new GridBagLayout());
    jPanel1.add(panelSimple, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
    jPanel1.add(panelValidSimple, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
    jPanel1.add(cbInvertedRingAllowed, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 0, 4, 0), 10, 0));
    
    /*jPanel1.add(lblValidSimple, new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 4, 0, 4), 0, 0));
*/
    /*
    jPanel1.add(txtIsValid, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
        ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 0, 4, 0), 10, 0));
    */
    jPanel1.add(panelMsg, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 4, 0, 4), 0, 0));
    jPanel1.add(panelClear, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 4, 0, 4), 0, 0));
   
    //----------------------------------------------
    txtMarkLocation.setBorder(BorderFactory.createLoweredBevelBorder());
    txtMarkLocation.setToolTipText("");
    txtMarkLocation.setEditable(true);
    txtMarkLocation.setFont(new java.awt.Font("SansSerif", 0, 12));
    txtMarkLocation.setHorizontalAlignment(SwingConstants.LEFT);
    Dimension markDim = new Dimension(TEXT_BOX_WIDTH, 20);
    txtMarkLocation.setPreferredSize(markDim);
    txtMarkLocation.setMaximumSize(markDim);
    txtMarkLocation.setMinimumSize(markDim);

    
    markPanel.setLayout(new BorderLayout());
    
    markBtnPanel.add(btnSetMark);
    markBtnPanel.add(btnClearMark);
    markPanel.add(lblMark, BorderLayout.NORTH);
    markPanel.add(txtMarkLocation, BorderLayout.CENTER);
    //markPanel.add(txtMarkLabel, BorderLayout.CENTER);
    markPanel.add(markBtnPanel, BorderLayout.SOUTH);
    
    //----------------------------------------------
    this.setLayout(new BorderLayout());
    this.add(jPanel1, BorderLayout.CENTER);
    this.add(markPanel, BorderLayout.SOUTH);
  }

  public void setTestCase(TestCaseEdit testCase) {
    this.testCase = testCase;
  }

  public Coordinate getMarkPoint()   { return markPoint; }

  void clearAll() {
    clearFlag(txtIsValid);
    clearFlag(txtIsSimple);
    taInvalidMsg.setText("");
    clearMark();
  }

  void btnValidate_actionPerformed(ActionEvent e) 
  {
    TopologyValidationError err = null;
    if (testCase.getGeometry(0) != null) {
      IsValidOp validOp = new IsValidOp(testCase.getGeometry(0));
      if (cbInvertedRingAllowed.isSelected()) {
        validOp.setSelfTouchingRingFormingHoleValid(true);
      }
      err = validOp.getValidationError();
    }
    String msg = "";
    boolean isValid = true;
    Coordinate invalidPoint = null;
    if (err != null) {
      isValid = false;
      msg = err.toString();
      invalidPoint = err.getCoordinate();
    }
    taInvalidMsg.setText(msg);
    setFlagText(txtIsValid, isValid);
    setMarkPoint(invalidPoint);
  }
  void btnSimple_actionPerformed(ActionEvent e) 
  {
  	boolean isSimple = true;
  	Coordinate nonSimpleLoc = null;
  	if (testCase.getGeometry(0) != null) {
      IsSimpleOp simpleOp = new IsSimpleOp(testCase.getGeometry(0));
      isSimple = simpleOp.isSimple();
      nonSimpleLoc = simpleOp.getNonSimpleLocation(); 
    }
    String msg = isSimple ?
    		""
    		: "Non-simple intersection at " + WKTWriter.toPoint(nonSimpleLoc);
    taInvalidMsg.setText(msg);
    setFlagText(txtIsSimple, isSimple);
    setMarkPoint(nonSimpleLoc);
  }
  
  private void setFlagText(JTextField txt, boolean val) {
    txt.setText(val ? "Y" : "N");
    txt.setBackground(val ? AppColors.BACKGROUND : AppColors.BACKGROUND_ERROR);
  }
  
  private void clearFlag(JTextField txt) {
    txt.setText("");
    txt.setBackground(AppColors.BACKGROUND);
  }
  
  private void setMarkPoint(Coordinate coord)
  {
    markPoint = coord;
    String markText = "";
    if (markPoint != null) {
      markText = " " + coord.x + "  " + coord.y + " ";
    }
    txtMarkLocation.setText(markText);
    fireSetHighlightPerformed(new ValidPanelEvent(this));
  }
  
  private void clearMark() {
    setMarkPoint(null);
  }
  
  public synchronized void removeValidPanelListener(ValidPanelListener l) {
    if (validPanelListeners != null && validPanelListeners.contains(l)) {
      Vector v = (Vector) validPanelListeners.clone();
      v.removeElement(l);
      validPanelListeners = v;
    }
  }
  public synchronized void addValidPanelListener(ValidPanelListener l) {
    Vector v = validPanelListeners == null ? new Vector(2) : (Vector) validPanelListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      validPanelListeners = v;
    }
  }
  protected void fireSetHighlightPerformed(ValidPanelEvent e) {
    if (validPanelListeners != null) {
      Vector listeners = validPanelListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ValidPanelListener) listeners.elementAt(i)).setHighlightPerformed(e);
      }
    }
  }

  void btnSetMark_actionPerformed(ActionEvent e) {
    String xyStr = txtMarkLocation.getText();
    setMarkPoint(parseXY(xyStr));
  }

  Coordinate parseXY(String xyStr)
  {
    // remove commas and underscores in case they are present
    String cleanStr = xyStr.replace("_", "");
    cleanStr = cleanStr.replace(",", " ");
    String[] xy = cleanStr.trim().split("\\s+");
    double x = parseNumber(xy, 0);
    double y = parseNumber(xy, 1);
    return new Coordinate(x, y);
  }

  double parseNumber(String[] xy, int index)
  {
    if (xy.length <= index) return 0.0;
    String s = xy[index];
    try {
      return Double.parseDouble(s);
    } 
    catch (NumberFormatException ex)
    {
      // just eat it - not much we can do
    }
    return 0.0;
  }
}
