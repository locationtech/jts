

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
import javax.swing.*;

import org.locationtech.jts.geom.PrecisionModel;

import java.awt.event.*;


/**
 * @version 1.7
 */
public class PrecisionModelPanel extends JPanel {
private PrecisionModel precisionModel;
//============================================
  JLabel jLabel1 = new JLabel();
  JTextField txtScale = new JTextField();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel jLabel4 = new JLabel();
  JRadioButton rbFixed = new JRadioButton();
  JRadioButton rbFloating = new JRadioButton();
  ButtonGroup btnGrpmodelType = new ButtonGroup();
  private JRadioButton rbFloatingSingle = new JRadioButton();

  public PrecisionModelPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    jLabel1.setToolTipText("");
    jLabel1.setText("Scale");
    this.setLayout(gridBagLayout1);
    txtScale.setBackground(Color.white);
    txtScale.setToolTipText("");
    txtScale.setText("1.0");
    txtScale.setHorizontalAlignment(SwingConstants.RIGHT);
    this.setMinimumSize(new Dimension(300, 200));
    this.setPreferredSize(new Dimension(300, 200));
    this.setToolTipText("");
    jLabel4.setForeground(SystemColor.desktop);
    jLabel4.setToolTipText("");
    jLabel4.setText("Set the Precision Model for all Test Cases");
    rbFixed.setToolTipText(" * <li>FLOATING - represents full double precision floating point.\n" +
    " * This is the default precision model used in JTS\n");
    rbFixed.setText("Fixed");
    rbFixed.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rbFixed_actionPerformed(e);
      }
    });
    rbFloating.setText("Floating (Double)");
    rbFloating.setToolTipText("");
    rbFloating.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rbFloating_actionPerformed(e);
      }
    });
    rbFloatingSingle.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rbFloatingSingle_actionPerformed(e);
      }
    });
    rbFloatingSingle.setToolTipText("");
    rbFloatingSingle.setText("Floating (Single)");
    this.add(jLabel1,        new GridBagConstraints(0, 4, 1, 1, 0.2, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
    this.add(txtScale,        new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
    this.add(jLabel4,       new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
    this.add(rbFixed,        new GridBagConstraints(0, 3, 2, 1, 0.2, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));
    this.add(rbFloating,        new GridBagConstraints(0, 1, 2, 1, 0.2, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));
    this.add(rbFloatingSingle,       new GridBagConstraints(0, 2, 1, 1, 0.2, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));
    btnGrpmodelType.add(rbFloating);
    btnGrpmodelType.add(rbFixed);
    btnGrpmodelType.add(rbFloatingSingle);
  }

  public void setPrecisionModel(PrecisionModel precisionModel)
  {
    this.precisionModel = precisionModel;
    Object modelType = precisionModel.getType();
    rbFixed.setSelected(modelType == PrecisionModel.FIXED);
    rbFloating.setSelected(modelType == PrecisionModel.FLOATING);
    rbFloatingSingle.setSelected(modelType == PrecisionModel.FLOATING_SINGLE);
    if (modelType == PrecisionModel.FIXED) {
      txtScale.setText(Double.toString(precisionModel.getScale()));
    }
    updateDisplay();
  }

  public PrecisionModel getPrecisionModel()
  {
    if (rbFloating.isSelected()) {
      return new PrecisionModel();
    }
    if (rbFloatingSingle.isSelected()) {
      return new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
    }
    double scale = Double.parseDouble(txtScale.getText());
    return new PrecisionModel(scale);
  }

  void updateDisplay()
  {
    if (isFloatingSelected()) {
      txtScale.setEnabled(false);
      txtScale.setForeground(Color.lightGray);
      txtScale.setBackground(Color.lightGray);
    }
    else {
      txtScale.setEnabled(true);
      txtScale.setForeground(Color.black);
      txtScale.setBackground(Color.white);
    }
  }

  boolean isFloatingSelected()
  {
    return rbFloating.isSelected() || rbFloatingSingle.isSelected();
  }
  void rbFloating_actionPerformed(ActionEvent e) {
    updateDisplay();
  }

  void rbFixed_actionPerformed(ActionEvent e) {
    updateDisplay();
  }
  void rbFloatingSingle_actionPerformed(ActionEvent e) {
    updateDisplay();
  }

}
