/*
 * Copyright (c) 2019 Martin Davis, and others.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.locationtech.jtstest.testbuilder.ui.render.ViewStyle;

public class GeometryViewStylePanel extends LabelComponentsPanel {
  
  JTSTestBuilderFrame tbFrame;
  private JCheckBox cbGrid;
  private JCheckBox cbLegend;
  private JCheckBox cbTitle;
  private JTextField txtTitle;
  private JCheckBox cbLegendBorder;
  private JCheckBox cbTitleBorder;
  private JPanel ctlBackgroundClr;
  private JPanel ctlTitleFillClr;
  private JCheckBox cbViewBorder;
  private JPanel ctlBorderClr;
  private JPanel ctlLegendFillClr;
  private JCheckBox cbLegendStats;

  public GeometryViewStylePanel() {
    try {
      uiInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }

  private void uiInit() {
    ViewStyle viewStyle = new ViewStyle();
    
    txtTitle = new JTextField();
    txtTitle.setMaximumSize(new Dimension(100,20));
    txtTitle.setPreferredSize(new Dimension(100,20));
    txtTitle.setMinimumSize(new Dimension(100,20));
    
    cbTitle = new JCheckBox();
    cbTitle.setSelected(viewStyle.isTitleEnabled());
    cbTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbTitle.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateView();      }
    });
    cbTitleBorder = new JCheckBox();
    cbTitleBorder.setSelected(viewStyle.isTitleBorderEnabled());
    cbTitleBorder.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbTitleBorder.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateView();      }
    });
    ctlTitleFillClr = ColorControl.create(this, 
        "Title fill color",
        viewStyle.getTitleFill(),
        new ColorControl.ColorListener() {
          public void colorChanged(Color clr) {
            updateView();
          }
        }
       );

    addRow("Title", cbTitle, txtTitle, "Border", cbTitleBorder, ctlTitleFillClr );
    
    cbLegend = new JCheckBox();
    cbLegend.setSelected(viewStyle.isLegendEnabled());
    cbLegend.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbLegend.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateView();      }
    });
    cbLegendBorder = new JCheckBox();
    cbLegendBorder.setSelected(viewStyle.isLegendBorderEnabled());
    cbLegendBorder.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbLegendBorder.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateView();      }
    });
    ctlLegendFillClr = ColorControl.create(this, 
        "Legend fill color",
        viewStyle.getLegendFill(),
        new ColorControl.ColorListener() {
          public void colorChanged(Color clr) {
            updateView();
          }
        }
       );
    cbLegendStats = new JCheckBox();
    cbLegendStats.setSelected(viewStyle.isLegendStatsEnabled());
    cbLegendStats.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbLegendStats.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateView();      }
    });
    addRow("Legend", cbLegend, ctlLegendFillClr, "Border", cbLegendBorder, "Stats", cbLegendStats );
    
    cbViewBorder = new JCheckBox();
    cbViewBorder.setSelected(viewStyle.isBorderEnabled());
    cbViewBorder.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbViewBorder.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateView();      }
    });
    ctlBorderClr = ColorControl.create(this, 
        "Border Color",
        viewStyle.getBorderColor(),
        new ColorControl.ColorListener() {
          public void colorChanged(Color clr) {
            updateView();
          }
        }
       );
    addRow("Border", cbViewBorder, ctlBorderClr);
    
    //--------------------------------------------------
    cbGrid = new JCheckBox();
    cbGrid.setSelected(viewStyle.isGridEnabled());
    cbGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbGrid.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateView();      }
    });
    
    ctlBackgroundClr = ColorControl.create(this, 
        "Background Color",
        viewStyle.getBackground(),
        new ColorControl.ColorListener() {
          public void colorChanged(Color clr) {
            updateView();
          }
        }
       );
    addRow("Grid", cbGrid, "Background", ctlBackgroundClr);
  }
  
  private void updateView() {
    ViewStyle viewStyle = new ViewStyle();
    viewStyle.setGridEnabled(cbGrid.isSelected());
    viewStyle.setBorderEnabled(cbViewBorder.isSelected());
    viewStyle.setBorderColor(ctlBorderClr.getBackground());
    viewStyle.setBackground(ctlBackgroundClr.getBackground());
    viewStyle.setTitleEnabled(cbTitle.isSelected());
    viewStyle.setTitleBorderEnabled(cbTitleBorder.isSelected());
    viewStyle.setTitleFill(ctlTitleFillClr.getBackground());
    viewStyle.setTitle(txtTitle.getText());
    viewStyle.setLegendEnabled(cbLegend.isSelected());
    viewStyle.setLegendBorderEnabled(cbLegendBorder.isSelected());
    viewStyle.setLegendStatsEnabled(cbLegendStats.isSelected());
    viewStyle.setLegendFill(ctlLegendFillClr.getBackground());
    
    JTSTestBuilder.controller().setViewStyle(viewStyle);

  }
}
