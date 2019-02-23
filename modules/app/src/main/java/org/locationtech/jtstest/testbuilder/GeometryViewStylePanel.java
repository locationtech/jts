package org.locationtech.jtstest.testbuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;

public class GeometryViewStylePanel extends LabelComponentsPanel {
  
  JTSTestBuilderFrame tbFrame;
  
  private JCheckBox cbGrid;

  private JButton btnBackground;

  public GeometryViewStylePanel() {
    try {
      uiInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }

  private void uiInit() {
    
    cbGrid = new JCheckBox();
    cbGrid.setSelected(true);
    cbGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbGrid.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JTSTestBuilderFrame.instance().setShowingGrid( cbGrid.isSelected() );
        JTSTestBuilder.controller().geometryViewChanged();
        
      }
    });
    addRow("Grid", cbGrid);
    
    btnBackground = createColorButton(new ColorListener() {
      public void colorChanged(Color clr) {
        btnBackground.setBackground(clr);
        JTSTestBuilder.controller().getGeometryEditPanel().setViewBackground(clr);
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    addRow("Background", btnBackground);
  }
  
  private Color showColorChooser(String title, Color initColor) {
    return JColorChooser.showDialog(this, title, initColor);
  }
  
  private interface ColorListener {
    void colorChanged(Color clr);
  }
  
  private JButton createColorButton(ColorListener colorListener) {
    JButton btn = new JButton();
    Dimension dim = new Dimension(16,16);
    btn.setMinimumSize(dim);
    btn.setMaximumSize(dim);
    btn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Color initClr = JTSTestBuilder.controller().getGeometryEditPanel().VIEW_backgroundColor;
        Color clr = showColorChooser("Background Color", initClr);
        if (clr != null) {
          colorListener.colorChanged(clr);
        }
      }
    });    
    return btn;
  }
}
