
/*
 * Copyright (c) 2016 Martin Davis.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.locationtech.jtstest.testbuilder.model.Layer;
import org.locationtech.jtstest.testbuilder.ui.style.BasicStyle;

public class LayerStylePanel extends JPanel {
  private Layer layer;
  private JCheckBox cbVertex;
  private JLabel title;
  private JPanel stylePanel;
  private int rowIndex;
  private JCheckBox cbDashed;
  private JSpinner widthSpinner;
  private SpinnerNumberModel widthModel;
  private JCheckBox cbFilled;
  private JSlider sliderFillAlpha;
  private JPanel btnFillColor;
  private JPanel btnLineColor;
  private JSlider sliderLineAlpha;
  
  public LayerStylePanel() {
    
    try {
      uiInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  private BasicStyle geomStyle() {
    return layer.getLayerStyle().getGeomStyle();
  }  
  public void setLayer(Layer layer) {
    this.layer = layer;
    this.title.setText("Styling - Layer " + layer.getName());
    cbVertex.setSelected(layer.getLayerStyle().isVertices());
    cbDashed.setSelected(geomStyle().isDashed());
    cbFilled.setSelected(geomStyle().isFilled());
    widthModel.setValue(geomStyle().getStrokeWidth());
    sliderLineAlpha.setValue(geomStyle().getLineAlpha());
    sliderFillAlpha.setValue(geomStyle().getFillAlpha());
    btnLineColor.setBackground(geomStyle().getLineColor());
    btnFillColor.setBackground(geomStyle().getFillColor());
  }
  
  private void uiInit() throws Exception {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
     
     
    title = new JLabel("Styling");
    title.setAlignmentX(Component.LEFT_ALIGNMENT);
    add(title);
    
    
    stylePanel = new JPanel();
    stylePanel.setLayout(new GridBagLayout());
    stylePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    add(stylePanel);
    add(Box.createVerticalGlue());
    
    Dimension minSize = new Dimension(5, 100);
    Dimension prefSize = new Dimension(5, 100);
    Dimension maxSize = new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    add(new Box.Filler(minSize, prefSize, maxSize));
    
    cbVertex = new JCheckBox();
    cbVertex.setToolTipText(AppStrings.STYLE_VERTEX_ENABLE);
    cbVertex.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbVertex.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().setVertices(cbVertex.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    addRow("Vertices", cbVertex);
   
    cbDashed = new JCheckBox();
    //cbDashed.setToolTipText(AppStrings.STYLE_VERTEX_ENABLE);
    cbDashed.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbDashed.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        geomStyle().setDashed(cbDashed.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    addRow("Dashed", cbDashed);
    //=============================================

    btnLineColor = ColorControl.create(this, 
        "Fill",
        AppColors.GEOM_VIEW_BACKGROUND,
        new ColorControl.ColorListener() {
          public void colorChanged(Color clr) {
            geomStyle().setLineColor(clr);
            JTSTestBuilder.controller().geometryViewChanged();
          }
        }
       );

    widthModel = new SpinnerNumberModel(1.0, 0, 100.0, 0.2);
    widthSpinner = new JSpinner(widthModel);
    //widthSpinner.setMinimumSize(new Dimension(50,12));
    //widthSpinner.setPreferredSize(new Dimension(50,12));
    widthSpinner.setMaximumSize(new Dimension(40,16));
    widthSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    widthSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        float width = widthModel.getNumber().floatValue();
        geomStyle().setStrokeWidth(width);
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    sliderLineAlpha = createOpacitySlider(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (! source.getValueIsAdjusting()) {
          int alpha = (int)source.getValue();
          geomStyle().setLineAlpha(alpha);
          JTSTestBuilder.controller().geometryViewChanged();
        }
      }
    });

    addRow("Line", btnLineColor, widthSpinner, sliderLineAlpha);
    //=============================================
    cbFilled = new JCheckBox();
    cbFilled.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbFilled.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        geomStyle().setFilled(cbFilled.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
   
    sliderFillAlpha = createOpacitySlider(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (! source.getValueIsAdjusting()) {
          int alpha = (int)source.getValue();
          geomStyle().setFillAlpha(alpha);
          JTSTestBuilder.controller().geometryViewChanged();
        }
      }
    });
    btnFillColor = ColorControl.create(this, 
        "Fill",
        AppColors.GEOM_VIEW_BACKGROUND,
        new ColorControl.ColorListener() {
          public void colorChanged(Color clr) {
            geomStyle().setFillColor(clr);
            geomStyle().setLineColor(lineColorFromFill(clr));
            updateStyle();
            JTSTestBuilder.controller().geometryViewChanged();
          }
        }
       );
    addRow("Fill", cbFilled, btnFillColor, sliderFillAlpha);
  }
 
  void updateStyle() {
    ColorControl.update(btnLineColor, geomStyle().getLineColor() );
    ColorControl.update(btnFillColor, geomStyle().getFillColor() );
  }

  protected static Color lineColorFromFill(Color clr) {
    return clr.darker();
  }

  private JSlider createOpacitySlider(ChangeListener changeListener) {
    JSlider slide = new JSlider(JSlider.HORIZONTAL, 0, 255, 150);
    slide.addChangeListener(changeListener);
    slide.setMajorTickSpacing(32);
    slide.setPaintTicks(true);
    return slide;
  }

  private void addRow(String title, JComponent comp) {
    JLabel lbl = new JLabel(title);
    stylePanel.add(lbl, gbc(0, rowIndex, GridBagConstraints.EAST, 0.1));
    stylePanel.add(comp, gbc(1, rowIndex, GridBagConstraints.WEST, 1));
    rowIndex++;
  }

  private void addRow(String title, JComponent c1, JComponent c2) {
    addRow(title, c1, c2, null);
  }
  
  private void addRow(String title, JComponent c1, JComponent c2, JComponent c3) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(c1);
    if (c2 != null) {
      panel.add(Box.createRigidArea(new Dimension(2,0)));
      panel.add(c2);
    }
    if (c3 != null) {
      panel.add(Box.createRigidArea(new Dimension(2,0)));
      panel.add(c3);
    }
    addRow(title, panel);
  }
  
  private GridBagConstraints gbc(int x, int y, int align, double weightX) {
    // TODO Auto-generated method stub
    return new GridBagConstraints(x, y, 
        1, 1, 
        weightX, 1, //weights
        align,
        GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2),
        2,
        0);
  }
}
