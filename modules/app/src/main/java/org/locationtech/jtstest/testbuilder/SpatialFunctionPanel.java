/*
 * Copyright (c) 2016 Vivid Solutions.
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

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.util.Stopwatch;
import org.locationtech.jtstest.function.*;
import org.locationtech.jtstest.testbuilder.controller.JTSTestBuilderController;
import org.locationtech.jtstest.testbuilder.event.GeometryFunctionEvent;
import org.locationtech.jtstest.testbuilder.event.GeometryFunctionListener;
import org.locationtech.jtstest.testbuilder.event.SpatialFunctionPanelEvent;
import org.locationtech.jtstest.testbuilder.event.SpatialFunctionPanelListener;
import org.locationtech.jtstest.testbuilder.model.FunctionParameters;
import org.locationtech.jtstest.testbuilder.ui.*;


/**
 * @version 1.7
 */
public class SpatialFunctionPanel 
extends JPanel 
{
  private static final String[] PARAM_DEFAULT = { "10", "0", "0", "0", "0" };
  
  private static String[] capStyleItems = new String[] { "Round", "Flat", "Square" };
  private static Object[] capStyleValues = new Object[] { 
  		new Integer(BufferParameters.CAP_ROUND),
  		new Integer(BufferParameters.CAP_FLAT),
  		new Integer(BufferParameters.CAP_SQUARE)
  		};
  private static String[] joinStyleItems = new String[] { "Round", "Mitre", "Bevel" };
  private static Object[] joinStyleValues = new Object[] { 
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

  private JComponent[] paramComp = { txtDistance, txtQuadrantSegs, cbCapStyle, cbJoinStyle, txtMitreLimit };
  private JLabel[] paramLabel = { lblDistance, lblQuadSegs, lblCapStyle, lblJoinStyle, lblMitreLimit };
  
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
    txtDistance.setText(PARAM_DEFAULT[0]);
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
    
    execToNewButton.setText("Compute New");
    execToNewButton.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        execToNewButton_actionPerformed(e);
      }
    });
    
    panelExec.add(execButton);
    // disabled until behaviour is worked out
    panelExec.add(execToNewButton);
    
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
        execFunction(e.getFunction(), false);
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
    execFunction(geomFuncPanel.getFunction(), false);
  }

  void execToNewButton_actionPerformed(ActionEvent e) {
    execFunction(geomFuncPanel.getFunction(), true);
  }

  void displayAAndBCheckBox_actionPerformed(ActionEvent e) {
    JTSTestBuilderController.getGeometryEditPanel().setShowingInput(displayAAndBCheckBox.isSelected());
  }

  private void setCurrentFunction(GeometryFunction func) {
    currentFunc = func;
    // fire execution event even if null, to set UI appropriately
    fireFunctionExecuted(new SpatialFunctionPanelEvent(this));
  }

  public void execFunction(GeometryFunction func, boolean createNew) {
    currentFunc = func;
    if (currentFunc == null)
      return;
    fireFunctionExecuted(new SpatialFunctionPanelEvent(this, createNew));
  }

  private void functionChanged(GeometryFunction func)
  {
    currentFunc = func;
    updateParameters(func);
    execButton.setToolTipText(functionDescription(func));
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
  
  private void updateParameters(GeometryFunction func)
  {
    int numNonGeomParams = numNonGeomParams(func);
    for (int i = 0; i < paramComp.length; i++) {
      boolean isUsed = numNonGeomParams > i;
      //SwingUtil.setEnabledWithBackground(paramComp[i], isUsed);
      paramComp[i].setVisible(isUsed);
      paramLabel[i].setVisible(isUsed);
      setToolTipText(paramComp[i], func, i + 1);      
    }
  }
  
  private static void setToolTipText(JComponent control, GeometryFunction func, int i) {
    String txt = null;
    if (func.getParameterTypes().length > i) {
      txt = "Enter " + func.getParameterTypes()[i].getSimpleName();
    }
    control.setToolTipText(txt);
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
  
  private int attributeParamOffset(GeometryFunction func) {
    return func.isBinary() ? 1 : 0;
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
    if (currentFunc == null) return null;
    Class[] paramTypes = currentFunc.getParameterTypes();
    Object[] paramVal = new Object[paramTypes.length];
    
    for (int i = 0; i < paramVal.length; i++) {
      Object valRaw = getParamValue(i);
      paramVal[i] = SwingUtil.coerce(valRaw, paramTypes[i]);
    }
    return paramVal;
  }

  private Object getParamValue(int index) {
    if (currentFunc.isBinary() && index == 0)
      return JTSTestBuilderController.getGeometryB();
    
    int attrIndex = index - attributeParamOffset(currentFunc);
    
    switch (attrIndex) {
    case 0: return valOrDefault(SwingUtil.value(txtDistance), PARAM_DEFAULT[0]);
    case 1: return valOrDefault(SwingUtil.value(txtQuadrantSegs), PARAM_DEFAULT[1]);
    case 2: return SwingUtil.value(cbCapStyle, capStyleValues);
    case 3: return SwingUtil.value(cbJoinStyle, joinStyleValues);
    case 4: return valOrDefault(SwingUtil.value(txtMitreLimit), PARAM_DEFAULT[4]);
    }
    return null;
  }

  private static String valOrDefault(String s, String defaultVal) {
    if (s.length() > 0) return s;
    return defaultVal;
  }
    
  public boolean isFunctionSelected()
  {
  	return currentFunc != null;
  }

  public GeometryFunction getFunction() {
    return currentFunc;
  }

  //=================================================
  // Events
  //=================================================
  
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

