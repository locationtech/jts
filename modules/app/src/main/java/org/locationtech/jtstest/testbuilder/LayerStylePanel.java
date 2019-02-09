package org.locationtech.jtstest.testbuilder;

import java.awt.BorderLayout;
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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.locationtech.jtstest.testbuilder.model.Layer;
import org.locationtech.jtstest.testbuilder.ui.style.BasicStyle;
import org.locationtech.jtstest.testbuilder.ui.style.Style;

public class LayerStylePanel extends JPanel {
  private Layer layer;
  private JCheckBox cbVertex;
  private JLabel title;
  private JPanel stylePanel;
  private int rowIndex;
  private JCheckBox cbDashed;
  private JSpinner widthSpinner;
  private SpinnerNumberModel widthModel;
  
  public LayerStylePanel() {
    
    try {
      uiInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  public void setLayer(Layer layer) {
    this.layer = layer;
    this.title.setText("Styling - Layer " + layer.getName());
    cbVertex.setSelected(layer.getLayerStyle().isVertices());
    cbDashed.setSelected(layer.getLayerStyle().getGeomStyle().isDashed());
    widthModel.setValue(layer.getLayerStyle().getGeomStyle().getStrokeWidth());
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
    cbVertex.setSelected(true);
    addRow("Vertices", cbVertex);
   
    cbDashed = new JCheckBox();
    //cbDashed.setToolTipText(AppStrings.STYLE_VERTEX_ENABLE);
    cbDashed.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbDashed.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().getGeomStyle().setDashed(cbDashed.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    cbVertex.setSelected(true);
    addRow("Dashed", cbDashed);
   
    widthModel = new SpinnerNumberModel(1.0, 0, 100.0, 0.5);
    widthSpinner = new JSpinner(widthModel);
    //widthSpinner.setMinimumSize(new Dimension(50,12));
    //widthSpinner.setPreferredSize(new Dimension(50,12));
    widthSpinner.setMaximumSize(new Dimension(40,16));
    widthSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    widthSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        float width = widthModel.getNumber().floatValue();
        layer.getLayerStyle().getGeomStyle().setStrokeWidth(width);
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    addRow("Line Width", widthSpinner);
  }

  private void addRow(String title, JComponent comp) {
    JLabel lbl = new JLabel(title);
    stylePanel.add(lbl, gbc(0, rowIndex, GridBagConstraints.EAST, 0.1));
    stylePanel.add(comp, gbc(1, rowIndex, GridBagConstraints.WEST, 1));
    rowIndex++;
  }

  private GridBagConstraints gbc(int x, int y, int align, double weightX) {
    // TODO Auto-generated method stub
    return new GridBagConstraints(x, y, 
        1, 1, 
        weightX, 1, //weights
        align,
        GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2),
        0,
        0);
  }
}
