package org.locationtech.jtstest.testbuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class GeometryViewStylePanel extends LabelComponentsPanel {
  
  JTSTestBuilderFrame tbFrame;
  
  private JCheckBox cbGrid;

  private JPanel ctlBackgroundClr;

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
    
    ctlBackgroundClr = ColorControl.create(this, 
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
