package com.vividsolutions.jtstest.testbuilder;

import javax.swing.JPanel;

import com.vividsolutions.jtstest.testbuilder.model.TestBuilderModel;

public abstract class TestBuilderPanel extends JPanel 
{
  protected TestBuilderModel tbModel;
  //protected JTSTestBuilderFrame tbFrame;
  
  TestBuilderPanel() {
    /*
    try {
        jbInit();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    */
  }
  
  protected abstract void uiInit();

  public void setModel(TestBuilderModel tbModel) {
    this.tbModel = tbModel;
  }
}
