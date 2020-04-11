package org.locationtech.jtstest.testbuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class GeometryViewStylePanel extends LabelComponentsPanel {
  
  JTSTestBuilderFrame tbFrame;
  
  public GeometryViewStylePanel() {
    try {
      uiInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }

  private void uiInit() {
    
    JCheckBox cbGrid = new JCheckBox();
    cbGrid.setSelected(true);
    cbGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbGrid.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JTSTestBuilder.controller().showGrid( cbGrid.isSelected() );
      }
    });
    addRow("Grid", cbGrid);
    
    JCheckBox cbLegend = new JCheckBox();
    cbLegend.setSelected(false);
    cbLegend.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbLegend.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JTSTestBuilder.controller().showLegend( cbLegend.isSelected() );
      }
    });
    addRow("Legend", cbLegend);
    
    JPanel ctlBackgroundClr = ColorControl.create(this, 
        "Background Color",
        AppColors.GEOM_VIEW_BACKGROUND,
        new ColorControl.ColorListener() {
          public void colorChanged(Color clr) {
            JTSTestBuilder.controller().getGeometryEditPanel().setViewBackground(clr);
            JTSTestBuilder.controller().geometryViewChanged();
          }
        }
       );
    addRow("Background", ctlBackgroundClr);
  }
}
