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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.locationtech.jtstest.testbuilder.model.Layer;
import org.locationtech.jtstest.testbuilder.model.LayerList;

/**
 * @version 1.7
 */
public class LayerListPanel extends JPanel {
  
  JPanel list = new JPanel();
  private LayerStylePanel lyrStylePanel;

  public LayerListPanel() {
    try {
      uiInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void uiInit() throws Exception {
    setSize(200, 250);
    setBackground(SystemColor.control);
    
    list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
    list.setBackground(SystemColor.control);
    list.setBorder(BorderFactory.createEmptyBorder(2,2,2,10));

    JScrollPane jScrollPane1 = new JScrollPane();
    jScrollPane1.setBackground(SystemColor.control);
    jScrollPane1.getViewport().add(list, null);

    setLayout(new BorderLayout());
    add(jScrollPane1, BorderLayout.WEST);
    lyrStylePanel = new LayerStylePanel();
    add(lyrStylePanel, BorderLayout.CENTER);
  }

  JPanel createStylePanel() {
    JPanel panelStyle = new JPanel();
    panelStyle.setLayout(new BoxLayout(panelStyle, BoxLayout.Y_AXIS));
    panelStyle.add(new JLabel("Styling"));
    panelStyle.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    return panelStyle;
  }
  
  public void populateList() {
    LayerList lyrList = JTSTestBuilderFrame.instance().getModel().getLayers();

    for (int i = 0; i < lyrList.size(); i++) {
      Layer lyr = lyrList.getLayer(i);
      LayerItemPanel item = new LayerItemPanel(lyr, lyrStylePanel);
      list.add(item);
    }
    
    lyrStylePanel.setLayer(lyrList.getLayer(0));
  }
}

class LayerItemPanel extends JPanel {
  private Layer layer;
  private JCheckBox checkbox;
  private LayerStylePanel lyrStylePanel;

  LayerItemPanel(Layer lyr, LayerStylePanel lyrStylePanel) {
    this.layer = lyr;
    this.lyrStylePanel = lyrStylePanel;
    try {
      uiInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void uiInit() throws Exception {
    setSize(200, 250);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setBackground(SystemColor.control);
    setAlignmentX(Component.LEFT_ALIGNMENT);

    checkbox = new JCheckBox();
    add(checkbox);
    checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
    checkbox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        layerVisAction();
      }
    });
    checkbox.setSelected(layer.isEnabled());

    JLabel lblName = new JLabel(layer.getName());
    lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
    add(lblName);
    lblName.addMouseListener(new MouseAdapter()  
    {  
      public void mouseClicked(MouseEvent e)  
      {  
        lyrStylePanel.setLayer(layer);
      }  
  }); 
  }

  private void layerVisAction() {
    boolean isVisible = checkbox.isSelected();
    layer.setEnabled(isVisible);
    repaint();
    JTSTestBuilder.controller().geometryViewChanged();
  }
}
