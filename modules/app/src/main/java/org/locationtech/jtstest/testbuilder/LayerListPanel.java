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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

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
    setBackground(AppConstants.CONTROL_CLR);
    
    list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
    list.setBackground(AppConstants.CONTROL_CLR);
    list.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

    JScrollPane jScrollPane1 = new JScrollPane();
    jScrollPane1.setBackground(AppConstants.CONTROL_CLR);
    jScrollPane1.setOpaque(true);
    jScrollPane1.getViewport().add(list, null);

    setLayout(new BorderLayout());
    add(jScrollPane1, BorderLayout.WEST);
    lyrStylePanel = new LayerStylePanel();
    add(lyrStylePanel, BorderLayout.CENTER);
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
  private Border BORDER_CONTROL = BorderFactory.createLineBorder(CLR_CONTROL);
  private Border BORDER_HIGHLIGHT = BorderFactory.createLineBorder(Color.DARK_GRAY);
  
  private static final Color CLR_CONTROL = AppConstants.CONTROL_CLR; //SystemColor.control;
  private static final Color CLR_HIGHLIGHT = CLR_CONTROL.brighter();
  
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
    setBackground(AppConstants.CONTROL_CLR);
    setOpaque(true);
    setAlignmentX(Component.LEFT_ALIGNMENT);
    setBorder(BORDER_CONTROL);

    /*
    addMouseListener(new MouseListener() {
      public void mouseEntered(MouseEvent e) {
        //JPanel panel = (JPanel)e.getSource();
        setBorder(BORDER_GRAY);
        revalidate();
      }

      public void mouseExited(MouseEvent e) {
        JPanel parent = (JPanel)e.getSource();
        setBorder(null);
        revalidate();
     }

      public void mouseClicked(MouseEvent e) {}
      public void mousePressed(MouseEvent e) { }
      public void mouseReleased(MouseEvent e) {}
    });
    
    */
    
    checkbox = new JCheckBox();
    add(checkbox);
    checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
    checkbox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        layerVisAction();
      }
    });
    checkbox.setSelected(layer.isEnabled());

    JPanel namePanel = new JPanel();
    namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
    namePanel.setBackground(CLR_CONTROL);
    namePanel.setOpaque(true);
    namePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    namePanel.setMinimumSize(new Dimension(50,12));
    namePanel.setPreferredSize(new Dimension(50,12));
    namePanel.setMaximumSize(new Dimension(50,12));
    //namePanel.setBorder(BORDER_GRAY);;
    add(namePanel);
    
    
    JLabel lblName = new JLabel(layer.getName());
    lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
    lblName.setMinimumSize(new Dimension(50,12));
    lblName.setPreferredSize(new Dimension(50,12));
    lblName.setMaximumSize(new Dimension(50,12));

    namePanel.add(lblName);
    namePanel.addMouseListener(new HighlightMouseListener(this));
    lblName.addMouseListener(new HighlightMouseListener(this));
    
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
  
  class HighlightMouseListener extends MouseAdapter {
    private JComponent comp;

    HighlightMouseListener(JComponent comp) {
      this.comp = comp;
    }
    
    public void mouseEntered(MouseEvent e) {
      //comp.setBackground(CLR_HIGHLIGHT);
      comp.setBorder(BORDER_HIGHLIGHT);
      comp.revalidate();
    }

    public void mouseExited(MouseEvent e) {
      //comp.setBackground(CLR_CONTROL);
      comp.setBorder(BORDER_CONTROL);
      comp.revalidate();
   }
  }
}
