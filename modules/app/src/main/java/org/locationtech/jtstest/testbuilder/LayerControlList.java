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

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.locationtech.jtstest.testbuilder.controller.JTSTestBuilderController;
import org.locationtech.jtstest.testbuilder.model.Layer;
import org.locationtech.jtstest.testbuilder.model.LayerList;


import java.awt.*;
import java.awt.event.*;

/**
 * Experimental control panel for layers.
 * Not currently used.
 * 
 * @author Martin Davis
 *
 */
public class LayerControlList extends JPanel
{
  protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

  private JTable table;

  class RadioButtonRenderer implements TableCellRenderer
  {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
    {
      if (value == null)
        return null;
      return (Component) value;
    }
  }

  class CheckBoxRenderer implements TableCellRenderer
  {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
    {
      if (value == null)
        return null;
      return (Component) value;
    }
  }

  class RadioButtonEditor extends DefaultCellEditor implements ItemListener
  {
    private JRadioButton button;

    public RadioButtonEditor(JCheckBox checkBox)
    {
      super(checkBox);
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column)
    {
      if (value == null)
        return null;
      button = (JRadioButton) value;
      button.addItemListener(this);
      return (Component) value;
    }

    public Object getCellEditorValue()
    {
      button.removeItemListener(this);
      return button;
    }

    public void itemStateChanged(ItemEvent e)
    {
      super.fireEditingStopped();
    }
  }

  class CheckBoxEditor extends DefaultCellEditor implements ItemListener
  {
    private JCheckBox button;

    public CheckBoxEditor(JCheckBox checkBox)
    {
      super(checkBox);
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column)
    {
      if (value == null)
        return null;
      button = (JCheckBox) value;
      button.addItemListener(this);
      return (Component) value;
    }

    public Object getCellEditorValue()
    {
      button.removeItemListener(this);
      return button;
    }

    public void itemStateChanged(ItemEvent e)
    {
      super.fireEditingStopped();
    }
  }

  public void addChangeListener(final JToggleButton cb, final int index)
  {
    cb.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent arg0)
      {
        enableUpdate(index, cb.isSelected());
      }
    });
  }
  
  public LayerControlList()
  {
    table = new JTable() {
      public void tableChanged(TableModelEvent e)
      {
        super.tableChanged(e);
        repaint();
      }
    };
    Object[][] items = new Object[][] {
        { new JRadioButton(), new JCheckBox(), "A" },
        { new JRadioButton(), new JCheckBox(), "B" },
        { new JRadioButton(), new JCheckBox(), "Result" } };
    DefaultTableModel dm = new DefaultTableModel();
    dm.setDataVector(items, new Object[] { "", "", "Layer" });

    ButtonGroup group1 = new ButtonGroup();
    group1.add((JRadioButton) dm.getValueAt(0, 0));
    group1.add((JRadioButton) dm.getValueAt(1, 0));
    group1.add((JRadioButton) dm.getValueAt(2, 0));

    addChangeListener((JToggleButton) dm.getValueAt(0, 1), 0);
    addChangeListener((JToggleButton) dm.getValueAt(1, 1), 1);
    addChangeListener((JToggleButton) dm.getValueAt(2, 1), 2);

    table.setModel(dm);
    // table.setShowHorizontalLines(false);
    // table.setShowVerticalLines(false);
    table.setGridColor(SystemColor.control);
    table.setBackground(SystemColor.control);

    table.getColumnModel().getColumn(0)
        .setCellRenderer(new RadioButtonRenderer());
    table.getColumnModel().getColumn(0)
        .setCellEditor(new RadioButtonEditor(new JCheckBox()));
    table.getColumnModel().getColumn(1).setCellRenderer(new CheckBoxRenderer());
    table.getColumnModel().getColumn(1)
        .setCellEditor(new CheckBoxEditor(new JCheckBox()));

    table.getColumnModel().getColumn(0).setMaxWidth(20);
    table.getColumnModel().getColumn(1).setMaxWidth(20);
    table.getColumnModel().getColumn(2).setMaxWidth(120);

    // table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    // JScrollPane scroll = new JScrollPane(table);

    setLayout(new BorderLayout());
    add(table, BorderLayout.CENTER);
  }

  public void populateList()
  {
  }
  
  private void enableUpdate(int i, boolean isEnabled)
  {
    LayerList lyrList = JTSTestBuilderFrame.instance().getModel().getLayers();
    Layer lyr = lyrList.getLayer(i);
    lyr.setEnabled(isEnabled);
    repaint();
    JTSTestBuilderController.geometryViewChanged();
  }
}
