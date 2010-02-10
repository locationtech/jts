

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

import com.vividsolutions.jts.geom.PrecisionModel;

import java.awt.*;
import javax.swing.*;
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
