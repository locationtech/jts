/*
 * Copyright (c) 2016 Martin Davis.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.locationtech.jtstest.testbuilder.model.Layer;
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.testbuilder.ui.style.BasicStyle;
import org.locationtech.jtstest.testbuilder.ui.style.LayerStyle;
import org.locationtech.jtstest.testbuilder.ui.style.Palette;
import org.locationtech.jtstest.testbuilder.ui.style.VertexStyle;

public class LayerStylePanel extends JPanel {
  
  private static StyleSwatchList createPresets() {
    return StyleSwatchList.create(
      new BasicStyle(Color.RED, Color.PINK),
      new BasicStyle(AppColors.GEOM_SELECT_LINE_CLR, AppColors.GEOM_SELECT_FILL_CLR),
      new BasicStyle(Color.MAGENTA, Color.PINK),
      new BasicStyle(Color.YELLOW, ColorUtil.lighter(ColorUtil.lighter(Color.YELLOW))),
      new BasicStyle(Color.BLACK, Color.LIGHT_GRAY)
      );
  }
  
  private Layer layer;
  private JLabel title;
  private JCheckBox cbShift;
  private JPanel stylePanel;
  private int rowIndex;
  private JCheckBox cbDashed;
  private JSpinner spinnerLineWidth;
  private SpinnerNumberModel lineWidthModel;
  private JCheckBox cbFilled;
  private JSlider sliderFillAlpha;
  private JPanel btnFillColor;
  private JPanel btnLineColor;
  private JSlider sliderLineAlpha;
  
  private JCheckBox cbVertex;
  private JPanel btnVertexColor;
  private JSpinner spinVertexSize;
  private SpinnerNumberModel vertexSizeModel;
  
  private JCheckBox cbLabel;
  private JPanel btnLabelColor;
  private JSpinner spinLabelSize;
  private SpinnerNumberModel labelSizeModel;
  
  private JCheckBox cbStroked;
  private JTextField txtName;
  private JCheckBox cbOrient;
  private JCheckBox cbStructure;
  private JCheckBox cbVertexLabel;
  private JCheckBox cbOffset;
  private JSpinner spinOffsetSize;
  private SpinnerNumberModel offsetSizeModel;
  private JCheckBox cbEndpoint;
  private JComboBox comboPalette;
  private JCheckBox cbSegIndex;
  private JComboBox comboVertexSymbol;

  
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
  
  public void setLayer(Layer layer, boolean isModifiable) {
    this.layer = layer;
    //this.title.setText("Styling - Layer " + layer.getName());
    txtName.setText(layer.getName());
    txtName.setEditable(isModifiable);
    txtName.setFocusable(isModifiable);
    updateStyleControls();
  }
  
  void updateStyleControls() {
    ColorControl.update(btnVertexColor, layer.getLayerStyle().getVertexColor() );
    ColorControl.update(btnLabelColor, layer.getLayerStyle().getLabelColor() );
    ColorControl.update(btnLineColor, geomStyle().getLineColor() );
    ColorControl.update(btnFillColor, geomStyle().getFillColor() );
    sliderLineAlpha.setValue(geomStyle().getLineAlpha());
    sliderFillAlpha.setValue(geomStyle().getFillAlpha());
    cbShift.setSelected(layer.getLayerStyle().isShifted());
    cbVertex.setSelected(layer.getLayerStyle().isVertices());
    cbVertexLabel.setSelected(layer.getLayerStyle().isVertexLabels());
    setVertexSymbol(comboVertexSymbol, layer.getLayerStyle().getVertexSymbol());
    vertexSizeModel.setValue(layer.getLayerStyle().getVertexSize());
    cbLabel.setSelected(layer.getLayerStyle().isLabel());
    labelSizeModel.setValue(layer.getLayerStyle().getLabelSize());
    cbEndpoint.setSelected(layer.getLayerStyle().isEndpoints());
    cbDashed.setSelected(geomStyle().isDashed());
    cbOffset.setSelected(layer.getLayerStyle().isOffset());
    offsetSizeModel.setValue(layer.getLayerStyle().getOffsetSize() );
    cbStroked.setSelected(geomStyle().isStroked());
    cbFilled.setSelected(geomStyle().isFilled());
    cbOrient.setSelected(layer.getLayerStyle().isOrientations());
    cbStructure.setSelected(layer.getLayerStyle().isStructure());
    cbSegIndex.setSelected(layer.getLayerStyle().isSegIndex());
    lineWidthModel.setValue((double) geomStyle().getStrokeWidth());
    setPaletteType(comboPalette, layer.getLayerStyle().getFillType());

    JTSTestBuilder.controller().layerListUpdate();
  }
  
  private void uiInit() throws Exception {
    setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    setLayout(new BorderLayout());
     
    //title = new JLabel("Styling");
    //title.setAlignmentX(Component.LEFT_ALIGNMENT);
    //add(title, BorderLayout.NORTH);
    
    add( stylePanel(), BorderLayout.CENTER );
    
    JButton btnReset = SwingUtil.createButton(AppIcons.CLEAR, "Reset style to default", new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (layer == null) return;
        layer.resetStyle();
        updateStyleControls();
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    StyleSwatchList stylePresetList = createStylePresets();
    
    JPanel btnPanel = new JPanel();
    //btnPanel.setPreferredSize(new Dimension(30, 30));
    btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
    btnPanel.add(btnReset);
    btnPanel.add(Box.createVerticalStrut(6));
    btnPanel.add(stylePresetList);
    
    add( btnPanel, BorderLayout.EAST);
  }
  
  private StyleSwatchList createStylePresets() {
    StyleSwatchList stylePresetList = createPresets();
    //stylePresetList.setBackground(Color.WHITE);
    //stylePresetList.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    stylePresetList.setAlignmentX(LEFT_ALIGNMENT);

    stylePresetList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        stylePresetList.clearSelection();
        if (layer == null) return;
        
        BasicStyle style = stylePresetList.getStyle(e);
        
        layer.getGeometryStyle().setFillColor(style.getFillColor());
        layer.getLayerStyle().getGeomStyle().setLineColor(style.getLineColor());
        layer.getLayerStyle().setColor(style.getLineColor());
        layer.getLayerStyle().setVertexColor(style.getLineColor());
        
        ColorControl.update(btnVertexColor, layer.getLayerStyle().getVertexColor() );
        ColorControl.update(btnLineColor, geomStyle().getLineColor() );
        ColorControl.update(btnFillColor, geomStyle().getFillColor() );
        
        JTSTestBuilder.controller().geometryViewChanged();
        JTSTestBuilder.controller().layerListUpdate();
      }
    });
    return stylePresetList;
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

    //=============================================
    txtName = new JTextField();
    txtName.setMaximumSize(new Dimension(100,20));
    txtName.setPreferredSize(new Dimension(100,20));
    txtName.setMinimumSize(new Dimension(100,20));
    
    cbShift = new JCheckBox();
    cbShift.setToolTipText(AppStrings.TIP_STYLE_SHIFT);
    cbShift.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbShift.setText("Shift");
    cbShift.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().setShift(cbShift.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    addRow("Name", txtName, cbShift);
    
    txtName.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        update();
      }
      public void removeUpdate(DocumentEvent e) {
        update();
      }
      public void insertUpdate(DocumentEvent e) {
        update();
      }

      public void update() {
        String name = txtName.getText();
        layer.setName(name);
        JTSTestBuilder.controller().layerListUpdate();
      }
    });

    //=============================================

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
    
    vertexSizeModel = new SpinnerNumberModel(4, 0, 100, 1);
    spinVertexSize = new JSpinner(vertexSizeModel);
    spinVertexSize.setMaximumSize(new Dimension(40,16));
    spinVertexSize.setAlignmentX(Component.LEFT_ALIGNMENT);
    spinVertexSize.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int size = vertexSizeModel.getNumber().intValue();
        layer.getLayerStyle().setVertexSize(size);
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    cbVertexLabel = new JCheckBox();
    cbVertexLabel.setToolTipText(AppStrings.TIP_STYLE_VERTEX_LABEL_ENABLE);
    cbVertexLabel.setText("Label");
    cbVertexLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbVertexLabel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().setVertexLabels(cbVertexLabel.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    comboVertexSymbol = new JComboBox(vertexSymbolNames);
    comboVertexSymbol.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        int symType = getVertexSymbol(cb);
        layer.getLayerStyle().setVertexSymbol(symType);
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    
    comboVertexSymbol.setToolTipText(AppStrings.TIP_STYLE_SYMBOL);
   
    addRow("Vertices", cbVertex, btnVertexColor, spinVertexSize, comboVertexSymbol, cbVertexLabel);
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
            layer.getLayerStyle().setColor(clr);
            JTSTestBuilder.controller().geometryViewChanged();
            JTSTestBuilder.controller().layerListUpdate();
          }
        }
       );
    JButton btnVertexSynch = createSynchButton("^", "Synch Vertex Color", new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (layer == null) return;
        Color clr = ColorControl.getColor(btnLineColor);
        layer.getLayerStyle().setColor(clr);
        layer.getLayerStyle().setVertexColor(clr);
        updateStyleControls();
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    lineWidthModel = new SpinnerNumberModel(1.0, 0, 100, 0.2);
    spinnerLineWidth = new JSpinner(lineWidthModel);
    //widthSpinner.setMinimumSize(new Dimension(50,12));
    //widthSpinner.setPreferredSize(new Dimension(50,12));
    spinnerLineWidth.setMaximumSize(new Dimension(40,16));
    spinnerLineWidth.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    spinnerLineWidth.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        float width = lineWidthModel.getNumber().floatValue();
        geomStyle().setStrokeWidth(width);
        JTSTestBuilder.controller().geometryViewChanged();
        JTSTestBuilder.controller().layerListUpdate();
      }
    });

    sliderLineAlpha = createOpacitySlider(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (! source.getValueIsAdjusting()) {
          int alpha = (int)source.getValue();
          geomStyle().setLineAlpha(alpha);
          JTSTestBuilder.controller().geometryViewChanged();
          JTSTestBuilder.controller().layerListUpdate();
        }
      }
    });
    cbDashed = new JCheckBox();
    cbDashed.setText("Dashed");
    //cbDashed.setToolTipText(AppStrings.STYLE_VERTEX_ENABLE);
    cbDashed.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbDashed.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        geomStyle().setDashed(cbDashed.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    cbOffset = new JCheckBox();
    cbOffset.setText("Offset");
    //cbDashed.setToolTipText(AppStrings.STYLE_VERTEX_ENABLE);
    cbOffset.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbOffset.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().setOffset(cbOffset.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    offsetSizeModel = new SpinnerNumberModel(LayerStyle.INIT_OFFSET_SIZE, -100, 100, 1);
    spinOffsetSize = new JSpinner(offsetSizeModel);
    spinOffsetSize.setMaximumSize(new Dimension(40,16));
    spinOffsetSize.setAlignmentX(Component.LEFT_ALIGNMENT);
    spinOffsetSize.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int size = offsetSizeModel.getNumber().intValue();
        layer.getLayerStyle().setOffsetSize(size);
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    addRow("Line", cbStroked, btnLineColor, btnVertexSynch, sliderLineAlpha, spinnerLineWidth, 
        cbDashed, cbOffset, spinOffsetSize);

    //=============================================
    
    cbEndpoint = new JCheckBox();
    cbEndpoint.setText("Endpoints");
    cbEndpoint.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbEndpoint.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().setEndpoints(cbEndpoint.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    
    cbOrient = new JCheckBox();
    cbOrient.setText("Orientation");
    cbOrient.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbOrient.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().setOrientations(cbOrient.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    cbStructure = new JCheckBox();
    cbStructure.setText("Structure");
    cbStructure.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbStructure.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().setStructure(cbStructure.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    cbSegIndex = new JCheckBox();
    cbSegIndex.setText("Index");
    cbSegIndex.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbSegIndex.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().setSegIndex(cbSegIndex.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });

    
   // Leave on separate line to allow room for dash style
    addRow("", cbEndpoint, cbOrient, cbStructure, cbSegIndex);
    //=============================================

    cbFilled = new JCheckBox();
    cbFilled.setToolTipText(AppStrings.TIP_STYLE_FILL_ENABLE);
    cbFilled.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbFilled.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        geomStyle().setFilled(cbFilled.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
        JTSTestBuilder.controller().layerListUpdate();
      }
    });
   
    sliderFillAlpha = createOpacitySlider(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (! source.getValueIsAdjusting()) {
          int alpha = (int)source.getValue();
          geomStyle().setFillAlpha(alpha);
          JTSTestBuilder.controller().geometryViewChanged();
          JTSTestBuilder.controller().layerListUpdate();
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
            JTSTestBuilder.controller().layerListUpdate();
          }
        }
       );
    JButton btnLineSynch = createSynchButton("^", "Synch Line Color", new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        Color clr = lineColorFromFill( ColorControl.getColor(btnFillColor));
        geomStyle().setLineColor(clr );
        layer.getLayerStyle().setColor(clr);
        updateStyleControls();
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    addRow("Fill", cbFilled, btnFillColor, btnLineSynch, sliderFillAlpha);

    //=============================================

    comboPalette = new JComboBox(paletteNames);
    comboPalette.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        int fillType = getPaletteType(cb);
        layer.getLayerStyle().setFillType(fillType);
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    comboPalette.setToolTipText(AppStrings.TIP_STYLE_PALETTE);
    addRow("Palette", comboPalette);
    
    //=============================================

    
    cbLabel = new JCheckBox();
    //cbLabel.setToolTipText(AppStrings.TIP_STYLE_VERTEX_ENABLE);
    cbLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbLabel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().setLabel(cbLabel.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    btnLabelColor = ColorControl.create(this, 
        "Label",
        AppColors.GEOM_VIEW_BACKGROUND,
        new ColorControl.ColorListener() {
          public void colorChanged(Color clr) {
            if (layer == null) return;
            layer.getLayerStyle().setLabelColor(clr);
            JTSTestBuilder.controller().geometryViewChanged();
          }
        }
       );
    
    labelSizeModel = new SpinnerNumberModel(4, 0, 100, 1);
    spinLabelSize = new JSpinner(labelSizeModel);
    spinLabelSize.setMaximumSize(new Dimension(40,16));
    spinLabelSize.setAlignmentX(Component.LEFT_ALIGNMENT);
    spinLabelSize.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int size = labelSizeModel.getNumber().intValue();
        layer.getLayerStyle().setLabelSize(size);
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });


    
    addRow("Label", cbLabel, btnLabelColor, spinLabelSize);
    
    //=============================================
    
    return containerPanel;
  }

  //-----------------------------------------
  static String[] paletteNames = { "Basic", "Varying", "Spectrum", "Spectrum Random", "Spectrum Third" }; 

  private static int getPaletteType(JComboBox comboPal) {
    String palName = (String)comboPal.getSelectedItem();
    
    int paletteType = Palette.TYPE_BASIC;
    if (palName.equalsIgnoreCase(paletteNames[1])) paletteType = Palette.TYPE_VARY;
    if (palName.equalsIgnoreCase(paletteNames[2])) paletteType = Palette.TYPE_SPECTRUM;
    if (palName.equalsIgnoreCase(paletteNames[3])) paletteType = Palette.TYPE_SPECTRUM_RANDOM;
    if (palName.equalsIgnoreCase(paletteNames[4])) paletteType = Palette.TYPE_SPECTRUM_THIRD;
    return paletteType;
  }
  
  private static void setPaletteType(JComboBox comboPal, int paletteType) {
    int index = 0;
    if (paletteType == Palette.TYPE_VARY) index = 1;
    if (paletteType == Palette.TYPE_SPECTRUM) index = 2;
    if (paletteType == Palette.TYPE_SPECTRUM_RANDOM) index = 3;
    if (paletteType == Palette.TYPE_SPECTRUM_THIRD) index = 4;
    comboPal.setSelectedIndex(index);
  }
  
  //-----------------------------------------
  static String[] vertexSymbolNames = { "Square", "Square Hollow", "Circle", "Circle Hollow" }; 

  private static int getVertexSymbol(JComboBox combo) {
    String name = (String)combo.getSelectedItem();
    
    for (int i = 0; i < vertexSymbolNames.length; i++) {
      if (name.equalsIgnoreCase(vertexSymbolNames[i])) return i;
    }
    return VertexStyle.SYM_SQUARE_SOLID;
  }
  
  private static void setVertexSymbol(JComboBox combo, int symbolType) {
    combo.setSelectedIndex(symbolType);
  }
  
  protected static Color lineColorFromFill(Color clr) {
    return ColorUtil.saturate(clr,  1);
    //return clr.darker();
  }

  private JButton createSynchButton(String lbl, String tip, ActionListener actionListener) {
    JButton btn = SwingUtil.createButton(lbl, tip, actionListener);
    btn.setMargin(new Insets(0, 0, 0, 0));
    Dimension dim = new Dimension(16, 20);
    btn.setMinimumSize(dim);
    btn.setPreferredSize(dim);
    btn.setMaximumSize(dim);
    return btn;
  }
  
  private JSlider createOpacitySlider(ChangeListener changeListener) {
    JSlider slide = new JSlider(JSlider.HORIZONTAL, 0, 255, 150);
    Dimension dim = new Dimension(80, 20);
    slide.setMinimumSize(dim);
    slide.setPreferredSize(dim);
    slide.setMaximumSize(dim);
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

  /*
  private void xaddRow(String title, JComponent c1, JComponent c2) {
    addRow(title, c1, c2, null, null);
  }
  private void xaddRow(String title, JComponent c1, JComponent c2, JComponent c3) {
    addRow(title, c1, c2, c3, null, null);
  }
  private void xaddRow(String title, JComponent c1, JComponent c2, JComponent c3, JComponent c4) {
    addRow(title, c1, c2, c3, c4, null);
  }
*/
  
  private void addRow(String title, JComponent ... comp) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    for (JComponent c : comp) {
      panel.add(Box.createRigidArea(new Dimension(2,0)));
      panel.add(c);
    }
    addRow(title, panel);
  }
  
  private GridBagConstraints gbc(int x, int y, int align, double weightX) {
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

class StyleSwatchButton extends JButton {
  
  public static StyleSwatchButton create(BasicStyle style, ActionListener action ) {
    StyleSwatchButton btn = new StyleSwatchButton(style);
    if (action != null) btn.addActionListener(action);
    btn.setFocusable(false);
    btn.setFocusPainted(false);
    return btn;
  }

  private BasicStyle style;

  public StyleSwatchButton(BasicStyle style) {
    this.style = style;
    
    setMargin(new Insets(2, 2, 2, 2));
    Dimension dim = new Dimension(16,16);
    setMinimumSize(dim);
    setPreferredSize(dim);
    setMaximumSize(dim);
    setOpaque(true);
    update(style);
  }
  
  public void setStyle(Layer layer) {
    layer.getGeometryStyle().setFillColor(style.getFillColor());
    layer.getLayerStyle().getGeomStyle().setLineColor(style.getLineColor());
    layer.getLayerStyle().setVertexColor(style.getLineColor());
    JTSTestBuilder.controller().geometryViewChanged();
    JTSTestBuilder.controller().layerListUpdate();
  }
  
  private void update(BasicStyle style) {
    
    Color fillClr = style.getFillColor() == null ? Color.WHITE : style.getFillColor();
    setBackground( fillClr );  

    int lineWidth = 1;
    if (style.getStrokeWidth() > 1)
      lineWidth = 2;

    setBorder(BorderFactory.createLineBorder(style.getLineColor(), lineWidth));
  }
}
