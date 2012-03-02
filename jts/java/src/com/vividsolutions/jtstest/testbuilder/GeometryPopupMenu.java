package com.vividsolutions.jtstest.testbuilder;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jtstest.testbuilder.controller.JTSTestBuilderController;

public class GeometryPopupMenu extends JPopupMenu 
{
  Coordinate clickCoord;
  
  public GeometryPopupMenu(){
    initUI();
  }
  
  private void initUI()
  {
    JMenuItem extractComponentItem = new JMenuItem("Extract Component");
    extractComponentItem.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              JTSTestBuilderController.extractComponentsToTestCase(clickCoord);
            }
          });
    add(extractComponentItem);
    
    JMenuItem copyComponentItem = new JMenuItem("Copy Component");
    copyComponentItem.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              JTSTestBuilderController.copyComponentToClipboard(clickCoord);
            }
          });
    add(copyComponentItem);
    
    JMenuItem infoItem = new JMenuItem("Info");
    infoItem.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              JTSTestBuilderFrame.instance().displayInfo(clickCoord);
            }
          });
    add(infoItem);
    
  }
  
  /**
   * Record model coordinate of click point for use in menu operations
   */
  public void show(Component invoker, int x, int y)
  {
    GeometryEditPanel editPanel = (GeometryEditPanel) invoker;
    clickCoord = editPanel.getViewport().toModelCoordinate(new java.awt.Point(x, y));
    super.show(invoker, x, y);
  }
  
}

