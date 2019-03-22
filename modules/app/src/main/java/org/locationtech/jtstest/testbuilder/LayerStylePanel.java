
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.testbuilder.ui.style.BasicStyle;

public class LayerStylePanel extends JPanel {
  private Layer layer;
  private JCheckBox cbVertex;
  private JLabel title;
  private JPanel stylePanel;
  private int rowIndex;
  private JCheckBox cbDashed;
  private JSpinner spinnerWidth;
  private SpinnerNumberModel widthModel;
  private JCheckBox cbFilled;
  private JSlider sliderFillAlpha;
  private JPanel btnFillColor;
  private JPanel btnLineColor;
  private JSlider sliderLineAlpha;
  private JSpinner spinnerVertexSize;
  private SpinnerNumberModel vertexSizeModel;
  private JCheckBox cbStroked;
  private JPanel btnVertexColor;

  
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
    cbStroked.setSelected(geomStyle().isStroked());
    cbFilled.setSelected(geomStyle().isFilled());
    widthModel.setValue(geomStyle().getStrokeWidth());
    vertexSizeModel.setValue(layer.getLayerStyle().getVertexSize());
    updateStyleControls();
  }
  
  void updateStyleControls() {
    ColorControl.update(btnVertexColor, layer.getLayerStyle().getVertexColor() );
    ColorControl.update(btnLineColor, geomStyle().getLineColor() );
    ColorControl.update(btnFillColor, geomStyle().getFillColor() );
    sliderLineAlpha.setValue(geomStyle().getLineAlpha());
    sliderFillAlpha.setValue(geomStyle().getFillAlpha());
  }
  
  private void uiInit() throws Exception {
    setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    setLayout(new BorderLayout());
     
     
    title = new JLabel("Styling");
    title.setAlignmentX(Component.LEFT_ALIGNMENT);
    add(title, BorderLayout.NORTH);
    

    add( stylePanel(), BorderLayout.CENTER );
    
    JButton btnReset = SwingUtil.createButton(AppIcons.CLEAR, "Reset style to default", new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (layer == null) return;
        layer.resetStyle();
        updateStyleControls();
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
    btnPanel.add(btnReset);
    add( btnPanel, BorderLayout.EAST);
  }
  
  private JPanel stylePanel() {
    JPanel containerPanel = new JPanel();
    containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
    
    stylePanel = new JPanel();
    stylePanel.setLayout(new GridBagLayout());
    stylePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    containerPanel.add(Box.createVerticalGlue());
    containerPanel.add(stylePanel);
    
    Dimension minSize = new Dimension(5, 100);
    Dimension prefSize = new Dimension(5, 100);
    Dimension maxSize = new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    containerPanel.add(new Box.Filler(minSize, prefSize, maxSize));

    cbVertex = new JCheckBox();
    cbVertex.setToolTipText(AppStrings.TIP_STYLE_VERTEX_ENABLE);
    cbVertex.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbVertex.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().setVertices(cbVertex.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    btnVertexColor = ColorControl.create(this, 
        "Vertex",
        AppColors.GEOM_VIEW_BACKGROUND,
        new ColorControl.ColorListener() {
          public void colorChanged(Color clr) {
            if (layer == null) return;
            layer.getLayerStyle().setVertexColor(clr);
            JTSTestBuilder.controller().geometryViewChanged();
          }
        }
       );
    
    vertexSizeModel = new SpinnerNumberModel(4.0, 0, 100.0, 1);
    spinnerVertexSize = new JSpinner(vertexSizeModel);
    spinnerVertexSize.setMaximumSize(new Dimension(40,16));
    spinnerVertexSize.setAlignmentX(Component.LEFT_ALIGNMENT);
    spinnerVertexSize.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int size = vertexSizeModel.getNumber().intValue();
        layer.getLayerStyle().setVertexSize(size);
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    
    addRow("Vertices", cbVertex, btnVertexColor, spinnerVertexSize);
    //=============================================

    cbStroked = new JCheckBox();
    cbStroked.setToolTipText(AppStrings.TIP_STYLE_LINE_ENABLE);
    cbStroked.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbStroked.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        geomStyle().setStroked(cbStroked.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    btnLineColor = ColorControl.create(this, 
        "Line",
        AppColors.GEOM_VIEW_BACKGROUND,
        new ColorControl.ColorListener() {
          public void colorChanged(Color clr) {
            geomStyle().setLineColor(clr);
            JTSTestBuilder.controller().geometryViewChanged();
          }
        }
       );
    JButton btnVertexSynch = SwingUtil.createButton("^", "Synch Vertex Color", new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (layer == null) return;
        Color clr = ColorControl.getColor(btnLineColor);
        layer.getLayerStyle().setVertexColor(clr);
        updateStyleControls();
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    widthModel = new SpinnerNumberModel(1.0, 0, 100.0, 0.2);
    spinnerWidth = new JSpinner(widthModel);
    //widthSpinner.setMinimumSize(new Dimension(50,12));
    //widthSpinner.setPreferredSize(new Dimension(50,12));
    spinnerWidth.setMaximumSize(new Dimension(40,16));
    spinnerWidth.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    spinnerWidth.addChangeListener(new ChangeListener() {
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

    addRow("Line", cbStroked, btnLineColor, btnVertexSynch, spinnerWidth, sliderLineAlpha);
    //=============================================
    
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

    cbFilled = new JCheckBox();
    cbFilled.setToolTipText(AppStrings.TIP_STYLE_FILL_ENABLE);
    cbFilled.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbFilled.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
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
            updateStyleControls();
            JTSTestBuilder.controller().geometryViewChanged();
          }
        }
       );
    JButton btnLineSynch = SwingUtil.createButton("^", "Synch Line Color", new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        geomStyle().setLineColor(lineColorFromFill( ColorControl.getColor(btnFillColor)) );
        updateStyleControls();
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    addRow("Fill", cbFilled, btnFillColor, btnLineSynch, sliderFillAlpha);
    
    return containerPanel;
  }

  protected static Color lineColorFromFill(Color clr) {
    return ColorUtil.saturate(clr,  1);
    //return clr.darker();
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
    addRow(title, c1, c2, null, null);
  }
  private void addRow(String title, JComponent c1, JComponent c2, JComponent c3) {
    addRow(title, c1, c2, c3, null, null);
  }
  private void addRow(String title, JComponent c1, JComponent c2, JComponent c3, JComponent c4) {
    addRow(title, c1, c2, c3, c4, null);
  }

  private void addRow(String title, JComponent c1, JComponent c2, JComponent c3, JComponent c4, JComponent c5) {
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
    if (c4 != null) {
      panel.add(Box.createRigidArea(new Dimension(2,0)));
      panel.add(c4);
    }
    if (c5 != null) {
      panel.add(Box.createRigidArea(new Dimension(2,0)));
      panel.add(c5);
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
