package org.locationtech.jtstest.testbuilder;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.locationtech.jtstest.testbuilder.model.Layer;

public class LayerStylePanel extends JPanel {
  private Layer layer;
  private JCheckBox cbVertex;
  private JLabel title;
  
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
  }
  
  private void uiInit() throws Exception {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    title = new JLabel("Styling");
    add(title);
    setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    
    cbVertex = new JCheckBox("Vertices");
    cbVertex.setToolTipText(AppStrings.STYLE_VERTEX_ENABLE);
    add(cbVertex);
    cbVertex.setAlignmentX(Component.LEFT_ALIGNMENT);
    cbVertex.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (layer == null) return;
        layer.getLayerStyle().setVertices(cbVertex.isSelected());
        JTSTestBuilder.controller().geometryViewChanged();
      }
    });
    cbVertex.setSelected(true);
  }
}
