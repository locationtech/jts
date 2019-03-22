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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import org.locationtech.jtstest.testbuilder.model.Layer;
import org.locationtech.jtstest.testbuilder.model.LayerList;
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;

/**
 * @version 1.7
 */
public class LayerListPanel extends JPanel {
  
  private static final int TAB_INDEX_LAYER = 0;

  private static final String LBL_LAYER_STYLE = "Layer Style";
  private static final String LBL_VIEW_STYLE = "View Style";
  
  JPanel list = new JPanel();
  JTabbedPane tabPane = new JTabbedPane();
  private LayerStylePanel lyrStylePanel;
  List<LayerItemPanel> layerItems = new ArrayList<LayerItemPanel>();

  public LayerListPanel() {
    try {
      uiInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void uiInit() throws Exception {
    setSize(200, 250);
    setBackground(AppColors.BACKGROUND);
    
    list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
    list.setBackground(AppColors.BACKGROUND);
    list.setBorder(BorderFactory.createEmptyBorder(2,2,2,0));

    JScrollPane jScrollPane1 = new JScrollPane();
    jScrollPane1.setBackground(AppColors.BACKGROUND);
    jScrollPane1.setOpaque(true);
    jScrollPane1.getViewport().add(list, null);

    setLayout(new BorderLayout());
    add(jScrollPane1, BorderLayout.WEST);
    
    lyrStylePanel = new LayerStylePanel();
    GeometryViewStylePanel viewStylePanel = new GeometryViewStylePanel();
    //add(lyrStylePanel, BorderLayout.CENTER);    

    //tabFunctions.setBackground(jTabbedPane1.getBackground());
    tabPane.add(lyrStylePanel,  LBL_LAYER_STYLE);
    tabPane.add(viewStylePanel,   LBL_VIEW_STYLE);
    add(tabPane, BorderLayout.CENTER);
  }
  
  public void showTabLayerStyle(String title) {
    tabPane.setSelectedIndex(TAB_INDEX_LAYER);
    tabPane.setTitleAt(0, LBL_LAYER_STYLE + " - " + title);
    //SwingUtil.showTab(tabPane, LBL_LAYER_STYLE);
  }
  
  public void populateList() {
    LayerList lyrList = JTSTestBuilderFrame.instance().getModel().getLayers();

    for (int i = 0; i < lyrList.size(); i++) {
      Layer lyr = lyrList.getLayer(i);
      LayerItemPanel item = new LayerItemPanel(lyr, this);
      list.add(item);
      layerItems.add(item);
    }
    setLayerFocus(layerItems.get(0));
  }
  
  public void setLayerFocus(LayerItemPanel layerItem) {
    for (LayerItemPanel item : layerItems) {
      item.setFocusLayer(false);
    }
    layerItem.setFocusLayer(true);
    showTabLayerStyle(layerItem.getLayer().getName());
    lyrStylePanel.setLayer(layerItem.getLayer());
  }
}

class LayerItemPanel extends JPanel {
  private static Font FONT_FOCUS = new java.awt.Font("Dialog", Font.BOLD, 12);
  private static Font FONT_NORMAL = new java.awt.Font("Dialog", Font.PLAIN, 12);
  
  private Border BORDER_CONTROL = BorderFactory.createLineBorder(CLR_CONTROL);
  private Border BORDER_HIGHLIGHT = BorderFactory.createLineBorder(Color.DARK_GRAY);
  
  private static final Color CLR_CONTROL = AppColors.BACKGROUND;
  private static final Color CLR_HIGHLIGHT = ColorUtil.darker(CLR_CONTROL, .95);
  
  private Layer layer;
  private JCheckBox checkbox;
  private LayerListPanel lyrListPanel;
  private LayerItemPanel self;
  private JPanel namePanel;
  private boolean hasFocus;
  private JLabel lblName;

  LayerItemPanel(Layer lyr, LayerListPanel lyrListPanel) {
    this.layer = lyr;
    this.lyrListPanel = lyrListPanel;
    try {
      uiInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    self = this;
  }

  public Layer getLayer() {
    return layer;
  }

  public void setFocusLayer(boolean hasFocus) {
    setBackground(hasFocus ? AppColors.TAB_FOCUS : AppColors.BACKGROUND);
    lblName.setFont(hasFocus ? FONT_FOCUS : FONT_NORMAL);
    revalidate();
    this.hasFocus = hasFocus;
  }
  
  public boolean isFocusLayer() {
    return hasFocus;
  }
  
  private void uiInit() throws Exception {
    setSize(200, 250);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setBackground(AppColors.BACKGROUND);
    //setOpaque(true);
    setAlignmentX(Component.LEFT_ALIGNMENT);
    setBorder(BORDER_CONTROL);
    
    checkbox = new JCheckBox();
    add(checkbox);
    checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
    checkbox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        layerVisAction();
      }
    });
    checkbox.setSelected(layer.isEnabled());

    namePanel = new JPanel();
    namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
    //namePanel.setBackground(CLR_CONTROL);
    namePanel.setOpaque(false);
    namePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    namePanel.setMinimumSize(new Dimension(50,12));
    namePanel.setPreferredSize(new Dimension(50,12));
    namePanel.setMaximumSize(new Dimension(50,12));
    //namePanel.setBorder(BORDER_GRAY);;
    add(namePanel);
    
    
    lblName = new JLabel(layer.getName());
    lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
    lblName.setMinimumSize(new Dimension(50,12));
    lblName.setPreferredSize(new Dimension(50,12));
    lblName.setMaximumSize(new Dimension(50,12));
    lblName.setFont(FONT_NORMAL);

    namePanel.add(lblName);
    namePanel.addMouseListener(new HighlightMouseListener(this));
    lblName.addMouseListener(new HighlightMouseListener(this));
    
    lblName.addMouseListener(new MouseAdapter()  
    {  
      public void mouseClicked(MouseEvent e)  
      {  
        lyrListPanel.setLayerFocus(self);
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
    private LayerItemPanel comp;

    HighlightMouseListener(LayerItemPanel comp) {
      this.comp = comp;
    }
    
    public void mouseEntered(MouseEvent e) {
      if (comp.isFocusLayer()) return;
      comp.setBackground(CLR_HIGHLIGHT);
      //comp.setBorder(BORDER_HIGHLIGHT);
      comp.revalidate();
    }

    public void mouseExited(MouseEvent e) {
      if (comp.isFocusLayer()) return;
      comp.setBackground(AppColors.BACKGROUND);
      //comp.setBorder(BORDER_CONTROL);
      comp.revalidate();
   }
  }

}
