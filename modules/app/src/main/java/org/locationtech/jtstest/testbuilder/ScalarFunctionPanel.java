/*
 * Copyright (c) 2016 Vivid Solutions.
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.Stopwatch;
import org.locationtech.jtstest.geomfunction.GeometryFunction;
import org.locationtech.jtstest.geomfunction.GeometryFunctionRegistry;
import org.locationtech.jtstest.testbuilder.event.GeometryFunctionEvent;
import org.locationtech.jtstest.testbuilder.event.GeometryFunctionListener;
import org.locationtech.jtstest.testbuilder.event.SpatialFunctionPanelEvent;
import org.locationtech.jtstest.testbuilder.event.SpatialFunctionPanelListener;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;



/**
 * @version 1.7
 */
public class ScalarFunctionPanel 
extends JPanel implements FunctionPanel 
{
  private static final String[] PARAM_DEFAULT = { "10" };
  
  JPanel panelRB = new JPanel();
  GeometryFunctionTreePanel funcListPanel = new GeometryFunctionTreePanel();
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
  
  private transient Vector spatialFunctionPanelListeners;

  private JLabel lblDistance = new JLabel();
  private JTextField txtDistance = new JTextField();

  private JComponent[] paramComp = { txtDistance };
  private JLabel[] paramLabel = { lblDistance };

  private GeometryFunction currentFunc = null;
  private Stopwatch timer;
  
  public ScalarFunctionPanel() {
    try {
      jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  void jbInit() throws Exception {
    funcListPanel.populate(JTSTestBuilder.getFunctionRegistry().getCategorizedScalarFunctions());

    this.setLayout(borderLayout1);
    panelParam.setLayout(gridLayout2);
    panelExec.setLayout(flowLayout);
    panelExecParam.setLayout(borderLayout2);
    panelRB.setLayout(gridLayout1);
    gridLayout2.setRows(1);
    gridLayout2.setColumns(2);

    lblDistance.setText("Distance");
    
    txtDistance.setMaximumSize(new Dimension(25, 2147483647));
    txtDistance.setMinimumSize(new Dimension(25, 21));
    txtDistance.setPreferredSize(new Dimension(25, 17));
    txtDistance.setText("10");
    txtDistance.setHorizontalAlignment(SwingConstants.RIGHT);

    panelParam.add(lblDistance);
    panelParam.add(txtDistance);
    
    execButton = SwingUtil.createButton(AppIcons.EXECUTE, AppStrings.TIP_EXECUTE,
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execButton_actionPerformed(e);
      }
    });
    
    panelExec.add(execButton);
    
    panelExecParam.add(panelExec, BorderLayout.SOUTH);
    panelExecParam.add(panelParam, BorderLayout.CENTER);
        
    this.add(funcListPanel, BorderLayout.CENTER);
    this.add(panelExecParam, BorderLayout.SOUTH);

    GeometryFunctionListener gfListener = new GeometryFunctionListener() {
      public void functionSelected(GeometryFunctionEvent e) {
        functionChanged(e.getFunction());
      }
      public void functionInvoked(GeometryFunctionEvent e) {
        execFunction(e.getFunction(), false);
      }
    };
    funcListPanel.addGeometryFunctionListener(gfListener);
  }

  void execButton_actionPerformed(ActionEvent e) {
    execFunction(funcListPanel.getFunction(), false);  }

  public void execFunction(GeometryFunction func, boolean createNew) {
    currentFunc = func;
    if (currentFunc == null)
      return;
    fireFunctionExecuted(new SpatialFunctionPanelEvent(this));
  }
  
  private void functionChanged(GeometryFunction func)
  {
    currentFunc = func;
    SpatialFunctionPanel.updateParameters(func, paramComp, paramLabel);
    execButton.setToolTipText( GeometryFunctionRegistry.functionDescriptionHTML(func) );
  }
  
  public Object getResult() {
    Object result = null;
    if (currentFunc == null || JTSTestBuilder.controller().getGeometryA() == null)
      return null;
    
    try {
      timer = new Stopwatch();
      result = currentFunc.invoke(JTSTestBuilder.controller().getGeometryA(), getFunctionParams());
      timer.stop();
    }
    catch (Exception ex) {
      ex.printStackTrace(System.out);
      result = ex;
    }
    return result;
  }

  private Object[] OLDgetFunctionParams()
  {
  	// TODO: this is somewhat cheesy
    Class[] paramTypes = currentFunc.getParameterTypes();
    if (paramTypes.length == 1 
        && paramTypes[0] == Geometry.class)
      return new Object[] { JTSTestBuilder.controller().getGeometryB() };
    
    if (paramTypes.length == 1 
        && (paramTypes[0] == Double.class || paramTypes[0] == double.class))
      return new Object[] { SwingUtil.getDouble(txtDistance, null) };
    
    if (paramTypes.length == 2 
        && paramTypes[0] == Geometry.class
      && (paramTypes[1] == Double.class || paramTypes[1] == double.class))
      return new Object[] { JTSTestBuilder.controller().getGeometryB(), SwingUtil.getDouble(txtDistance, null) };
    
    if (paramTypes.length >= 2)
      return new Object[] { 
    		SwingUtil.getDouble(txtDistance, null)
        };
    
    return null;
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
      return JTSTestBuilder.controller().getGeometryB();
    
    int attrIndex = index - SpatialFunctionPanel.attributeParamOffset(currentFunc);
    
    switch (attrIndex) {
    case 0: return SpatialFunctionPanel.valOrDefault(SwingUtil.value(txtDistance), PARAM_DEFAULT[0]);
    }
    return null;
  }
  
  public String getOpName() {
    if (currentFunc == null)
      return "";
    return currentFunc.getName();
  }

  public GeometryFunction getFunction() {
    return currentFunc;
  }
  
  public Stopwatch getTimer()
  {
    return timer;
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

