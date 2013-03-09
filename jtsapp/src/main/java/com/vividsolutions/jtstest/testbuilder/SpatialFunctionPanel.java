/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtstest.testbuilder;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Stopwatch;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import com.vividsolutions.jtstest.function.*;
import com.vividsolutions.jtstest.testbuilder.model.FunctionParameters;
import com.vividsolutions.jtstest.testbuilder.ui.*;

/**
 * @version 1.7
 */
public class SpatialFunctionPanel 
extends JPanel 
{
  private static String[] capStyleItems = new String[] { "", "Round", "Flat", "Square" };
  private static Object[] capStyleValues = new Object[] { 
  		null, 
  		new Integer(BufferParameters.CAP_ROUND),
  		new Integer(BufferParameters.CAP_FLAT),
  		new Integer(BufferParameters.CAP_SQUARE)
  		};
  private static String[] joinStyleItems = new String[] { "", "Round", "Mitre", "Bevel" };
  private static Object[] joinStyleValues = new Object[] { 
  		null, 
  		new Integer(BufferParameters.JOIN_ROUND),
  		new Integer(BufferParameters.JOIN_MITRE),
  		new Integer(BufferParameters.JOIN_BEVEL)
  };

	
	
  JPanel panelRB = new JPanel();
//  GeometryFunctionListPanel geomFuncPanel = new GeometryFunctionListPanel();
  GeometryFunctionTreePanel geomFuncPanel = new GeometryFunctionTreePanel();
  GridLayout gridLayout1 = new GridLayout();
  GridLayout gridLayout2 = new GridLayout();


  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();

  JPanel panelParam = new JPanel();
  JPanel panelExec = new JPanel();
  JPanel panelExecParam = new JPanel();
  FlowLayout flowLayout = new FlowLayout();
  FlowLayout flowLayout1 = new FlowLayout();
  
  JButton execButton = new JButton();
  JButton execToNewButton = new JButton();
  
  private final ImageIcon clearIcon = new ImageIcon(this.getClass().getResource("clear.gif"));

  private transient Vector spatialFunctionPanelListeners;
  private JPanel panelControl = new JPanel();
  private JCheckBox displayAAndBCheckBox = new JCheckBox();
  private JButton btnClearResult = new JButton();

  private JLabel lblDistance = new JLabel();
  private JTextField txtDistance = new JTextField();
  private JLabel lblQuadSegs = new JLabel();
  private JTextField txtQuadrantSegs = new JTextField();
  
  private JLabel lblCapStyle = new JLabel();
  private JComboBox cbCapStyle = new JComboBox();
  private JLabel lblJoinStyle = new JLabel();
  private JComboBox cbJoinStyle = new JComboBox();
  private JLabel lblMitreLimit = new JLabel();
  private JTextField txtMitreLimit = new JTextField();


  
  private GeometryFunction currentFunc = null;
  private Stopwatch timer;
  
  public SpatialFunctionPanel() {
    try {
      jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  void jbInit() throws Exception {
//    geomFuncPanel.populate(JTSTestBuilder.getFunctionRegistry().getGeometryFunctions());
    geomFuncPanel.populate(JTSTestBuilder.getFunctionRegistry().getCategorizedGeometryFunctions());

  	
    this.setLayout(borderLayout1);
    panelParam.setLayout(gridLayout2);
    panelExec.setLayout(flowLayout);
    panelExecParam.setLayout(borderLayout2);
    panelRB.setLayout(gridLayout1);
    gridLayout2.setRows(5);
    gridLayout2.setColumns(2);

    
    displayAAndBCheckBox.setSelected(true);
    displayAAndBCheckBox.setToolTipText("");
    displayAAndBCheckBox.setText("Display Input");
    displayAAndBCheckBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          displayAAndBCheckBox_actionPerformed(e);
        }
      });

    lblDistance.setText("Distance");
    
    txtDistance.setMaximumSize(new Dimension(25, 2147483647));
    txtDistance.setMinimumSize(new Dimension(25, 21));
    txtDistance.setPreferredSize(new Dimension(25, 17));
    txtDistance.setText("10");
    txtDistance.setHorizontalAlignment(SwingConstants.RIGHT);

    lblQuadSegs.setText("Quadrant Segs");
    txtQuadrantSegs.setHorizontalAlignment(SwingConstants.RIGHT);
    
    lblCapStyle.setText("Cap Style");
    ComboBoxModel modelCapStyle = new DefaultComboBoxModel(capStyleItems);
    cbCapStyle.setModel(modelCapStyle);
    
    lblJoinStyle.setText("Join Style");
    ComboBoxModel modelJoinStyle = new DefaultComboBoxModel(joinStyleItems);
    cbJoinStyle.setModel(modelJoinStyle);

    lblMitreLimit.setText("Mitre Limit");
    txtMitreLimit.setHorizontalAlignment(SwingConstants.RIGHT);

    panelControl.setLayout(flowLayout1);


    btnClearResult.setToolTipText("");
    btnClearResult.setMargin(new Insets(0, 10, 0, 10));
    btnClearResult.setSelected(true);
    btnClearResult.setText("Clear Result");
    btnClearResult.addActionListener(new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          clearResultButton_actionPerformed(e);
        }
      });

    panelParam.add(lblDistance);
    panelParam.add(txtDistance);
    panelParam.add(lblQuadSegs);
    panelParam.add(txtQuadrantSegs);
    panelParam.add(lblCapStyle);
    panelParam.add(cbCapStyle);
    panelParam.add(lblJoinStyle);
    panelParam.add(cbJoinStyle);
    panelParam.add(lblMitreLimit);
    panelParam.add(txtMitreLimit);

    panelControl.add(displayAAndBCheckBox, null);
    panelControl.add(btnClearResult, null);
    
    execButton.setText("Compute");
    execButton.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        execButton_actionPerformed(e);
      }
    });
    
    execToNewButton.setText("Compute To New");
    execToNewButton.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        execToNewButton_actionPerformed(e);
      }
    });
    
    panelExec.add(execButton);
    // disabled until behaviour is worked out
    //panelExec.add(execToNewButton);
    
    panelExecParam.add(panelExec, BorderLayout.NORTH);
    panelExecParam.add(panelParam, BorderLayout.CENTER);
    panelExecParam.add(panelControl, BorderLayout.SOUTH);
    
    this.add(geomFuncPanel, BorderLayout.CENTER);
    this.add(panelExecParam, BorderLayout.SOUTH);

    GeometryFunctionListener gfListener = new GeometryFunctionListener() {
      public void functionSelected(GeometryFunctionEvent e) {
      	functionChanged(e.getFunction());
      }
      public void functionInvoked(GeometryFunctionEvent e) {
        execFunction(e.getFunction());
      }
    };
    geomFuncPanel.addGeometryFunctionListener(gfListener);
  }

  public void enableExecuteControl(boolean isEnabled)
  {
  	execButton.setEnabled(isEnabled);
  }
  
  void clearResultButton_actionPerformed(ActionEvent e) {
    clearFunction();
  }

  void execButton_actionPerformed(ActionEvent e) {
    execFunction(geomFuncPanel.getFunction());
  }

  void execToNewButton_actionPerformed(ActionEvent e) {
    execFunction(geomFuncPanel.getFunction());
  }

  void displayAAndBCheckBox_actionPerformed(ActionEvent e) {
    getGeometryEditPanel().setShowingInput(displayAAndBCheckBox.isSelected());
  }

  private Geometry getGeometryA() {
    return JTSTestBuilder.model().getGeometryEditModel().getGeometry(0);
  }

  private Geometry getGeometryB() {
    return JTSTestBuilder.model().getGeometryEditModel().getGeometry(1);
  }

  private GeometryEditPanel getGeometryEditPanel() {
    return JTSTestBuilderFrame.instance().getTestCasePanel().getGeometryEditPanel();
  }

  private void setCurrentFunction(GeometryFunction func) {
    currentFunc = func;
    fireFunctionExecuted(new SpatialFunctionPanelEvent(this));
  }

  public void execFunction(GeometryFunction func) {
    currentFunc = func;
    if (currentFunc == null)
      return;
    fireFunctionExecuted(new SpatialFunctionPanelEvent(this));
  }

  private void functionChanged(GeometryFunction func)
  {
    currentFunc = func;
    updateParameterControls();
    execButton.setToolTipText("Compute " + functionDescription(func));
  }
  
  private String functionDescription(GeometryFunction func)
  {
  	String txt = "<b>" + func.getSignature() + "</b>";
  	String desc = func.getDescription();
  	if (desc != null) {
  		txt += "<br><br>" + desc;
  	}
  	return "<html>" + txt + "</html>";
  }
  
  private void updateParameterControls()
  {
    int numNonGeomParams = numNonGeomParams(currentFunc);
    // TODO: this is a bit of a hack, and should be made smarter
    SwingUtil.setEnabledWithBackground(txtDistance, numNonGeomParams >= 1);
    SwingUtil.setEnabledWithBackground(txtQuadrantSegs, numNonGeomParams >= 2);
    SwingUtil.setEnabledWithBackground(cbCapStyle, numNonGeomParams >= 3);
    SwingUtil.setEnabledWithBackground(cbJoinStyle, numNonGeomParams >= 4);
    SwingUtil.setEnabledWithBackground(txtMitreLimit, numNonGeomParams >= 5);
  }
  
  private static int numNonGeomParams(GeometryFunction func)
  {
    int count = 0;
    Class[] paramTypes = func.getParameterTypes();
    for (int i = 0; i < paramTypes.length; i++) {
      if (paramTypes[i] != Geometry.class)
        count++;
    }
    return count;
  }
  
  public boolean shouldShowGeometryA() {
    return displayAAndBCheckBox.isSelected();
  }

  public boolean shouldShowGeometryB() {
    return displayAAndBCheckBox.isSelected();
  }

  public void clearFunction() {
    setCurrentFunction(null);
  }

  public Object[] getFunctionParams()
  {
  	// TODO: improve this, it is cheesy
  	
    Class[] paramTypes = currentFunc.getParameterTypes();
    
    if (paramTypes.length == 1 
        && paramTypes[0] == Geometry.class)
      return new Object[] { getGeometryB() };
    
    if (paramTypes.length == 1 
        && (paramTypes[0] == Double.class || paramTypes[0] == double.class))
      return new Object[] { SwingUtil.getDouble(txtDistance, null) };
    
    if (paramTypes.length == 1 
        && (paramTypes[0] == Integer.class || paramTypes[0] == int.class))
      return new Object[] { SwingUtil.getInteger(txtDistance, null) };
    
    if (paramTypes.length == 1 
        && (paramTypes[0] == String.class))
      return new Object[] { txtDistance.getText() };
    
    if (paramTypes.length == 2 
        && paramTypes[0] == Geometry.class
      && (paramTypes[1] == Double.class || paramTypes[1] == double.class))
      return new Object[] { getGeometryB(), SwingUtil.getDouble(txtDistance, null) };
    
    if (paramTypes.length == 2 
        && paramTypes[0] == Geometry.class
      && (paramTypes[1] == Integer.class || paramTypes[1] == int.class))
      return new Object[] { getGeometryB(), SwingUtil.getDouble(txtDistance, null) };
    
    if (paramTypes.length == 2 
        && (paramTypes[0] == Integer.class || paramTypes[0] == int.class)
        && (paramTypes[1] == Double.class || paramTypes[1] == double.class))
      return new Object[] {  
        SwingUtil.getInteger(txtDistance, new Integer(100)), 
        SwingUtil.getDouble(txtQuadrantSegs, new Double(0.0)) };
    
    if (paramTypes.length == 2 
        && (paramTypes[0] == Double.class || paramTypes[0] == double.class)
        && (paramTypes[1] == Integer.class || paramTypes[1] == int.class)
    )
      return new Object[] {  
        SwingUtil.getDouble(txtDistance, new Double(10)), 
        SwingUtil.getInteger(txtQuadrantSegs, new Integer(0)) 
        };
    
    if (paramTypes.length == 2 
        && (paramTypes[0] == Double.class || paramTypes[0] == double.class)
        && (paramTypes[1] == Double.class || paramTypes[1] == double.class)
    )
      return new Object[] {  
        SwingUtil.getDouble(txtDistance, new Double(10)), 
        SwingUtil.getDouble(txtQuadrantSegs, new Double(0)) 
        };
    
    if (paramTypes.length >= 2)
      return new Object[] { 
        SwingUtil.getDouble(txtDistance, null),
        SwingUtil.getInteger(txtQuadrantSegs, null),
        SwingUtil.getSelectedValue(cbCapStyle, capStyleValues),
        SwingUtil.getSelectedValue(cbJoinStyle, joinStyleValues),
        SwingUtil.getDouble(txtMitreLimit, null),
        };
    
    return null; 
  }
  
  public boolean isFunctionSelected()
  {
  	return currentFunc != null;
  }
  
  public String getFunctionCall() {
    if (currentFunc == null)
      return null;
    return currentFunc.getCategory() + "." + currentFunc.getName()
    + "(" + FunctionParameters.toString(getFunctionParams()) + ")";
  }

  public GeometryFunction getFunction() {
    return currentFunc;
  }

  public synchronized void removeSpatialFunctionPanelListener(SpatialFunctionPanelListener l) {
    if (spatialFunctionPanelListeners != null && spatialFunctionPanelListeners.contains(l)) {
      Vector v = (Vector) spatialFunctionPanelListeners.clone();
      v.removeElement(l);
      spatialFunctionPanelListeners = v;
    }
  }

  public synchronized void addSpatialFunctionPanelListener(SpatialFunctionPanelListener l) {
    Vector v = spatialFunctionPanelListeners == null ? new Vector(2) : (Vector) spatialFunctionPanelListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      spatialFunctionPanelListeners = v;
    }
  }

  protected void fireFunctionExecuted(SpatialFunctionPanelEvent e) {
    if (spatialFunctionPanelListeners != null) {
      Vector listeners = spatialFunctionPanelListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((SpatialFunctionPanelListener) listeners.elementAt(i)).functionExecuted(e);
      }
    }
  }


}

