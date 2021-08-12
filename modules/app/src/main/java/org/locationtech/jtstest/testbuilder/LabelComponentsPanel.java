/*
 * Copyright (c) 2019 Martin Davis
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LabelComponentsPanel extends JPanel {
  
  private double lblWeight = 0.1;
  private Insets cellInsets = new Insets(2, 2, 2, 2);
  private int rowIndex = 0;
  
  LabelComponentsPanel() {
    setLayout(new GridBagLayout());
    setAlignmentX(Component.LEFT_ALIGNMENT);
  }
  public void setCellInsets(Insets insets) {
    cellInsets = insets;
  }
  
  public JLabel label(String name) {
    JLabel lbl = new JLabel(name);
    return lbl;
  }
  
  public void addRowInternal(String title, JComponent comp) {
    JLabel lbl = new JLabel(title);
    add(lbl, gbc(0, rowIndex, GridBagConstraints.EAST, lblWeight));
    add(comp, gbc(1, rowIndex, GridBagConstraints.WEST, 1));
    rowIndex ++;
  }

  public void addRow(String title, Object... comp) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    for (Object o : comp) {
      panel.add(Box.createRigidArea(new Dimension(2,0)));
      JComponent c;
      if (o instanceof String) {
        c = label((String) o);
      }
      else {
        c = (JComponent) o;
      }
      panel.add(c);
    }
    addRowInternal(title, panel);
  }
  /*
  public void addRow(String title, JComponent c1, JComponent c2, JComponent c3) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(c1);
    panel.add(c2);
    panel.add(c3);
    addRow(title, panel);
  }
  */
  
  private GridBagConstraints gbc(int x, int y, int align, double weightX) {
    // TODO Auto-generated method stub
    return new GridBagConstraints(x, y, 
        1, 1, 
        weightX, 1, //weights
        align,
        GridBagConstraints.NONE,
        cellInsets,
        0,
        0);
  }
}
