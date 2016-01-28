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

package org.locationtech.jtstest.testbuilder;

import javax.swing.*;
import javax.swing.border.*;

import org.locationtech.jtstest.testbuilder.controller.JTSTestBuilderController;
import org.locationtech.jtstest.testbuilder.model.Layer;


import java.awt.*;
import java.awt.event.*;

public class LayerCheckBoxList extends JList {
  protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

  public LayerCheckBoxList(ListModel listModel) {
    this();
    setModel(listModel);
  }
  
  public LayerCheckBoxList() {
    setCellRenderer(new CellRenderer());

    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        int index = locationToIndex(e.getPoint());

        if (index != -1) {
          Layer lyr = (Layer) getModel().getElementAt(index);
          lyr.setEnabled(! lyr.isEnabled());
          repaint();
          JTSTestBuilderController.geometryViewChanged();
        }
      }
    });

    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

  protected class CellRenderer implements ListCellRenderer 
  {
    JCheckBox checkbox = new JCheckBox();

    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      Layer lyr = (Layer) value;
      checkbox.setBackground(isSelected ? getSelectionBackground()
          : getBackground());
      checkbox.setForeground(isSelected ? getSelectionForeground()
          : getForeground());
      checkbox.setSelected(lyr.isEnabled());
      checkbox.setEnabled(isEnabled());
      checkbox.setFont(getFont());
      checkbox.setFocusPainted(false);
      checkbox.setBorderPainted(true);
      checkbox.setBorder(isSelected ? UIManager
          .getBorder("List.focusCellHighlightBorder") : noFocusBorder);
      checkbox.setText(lyr.getNameInfo());
      return checkbox;
    }
  }
}
