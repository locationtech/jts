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
import com.vividsolutions.jtstest.testbuilder.ui.SwingUtil;


/**
 * @version 1.7
 */
public class ScalarFunctionPanel 
extends JPanel 
{
  JPanel panelRB = new JPanel();
  GeometryFunctionListPanel funcListPanel = new GeometryFunctionListPanel();
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
    funcListPanel.populate(JTSTestBuilder.getFunctionRegistry().getScalarFunctions());

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
    
    execButton.setText("Compute");
    execButton.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        execButton_actionPerformed(e);
      }
    });
    
    panelExec.add(execButton);
    
    panelExecParam.add(panelExec, BorderLayout.NORTH);
    panelExecParam.add(panelParam, BorderLayout.CENTER);
        
    this.add(funcListPanel, BorderLayout.CENTER);
    this.add(panelExecParam, BorderLayout.SOUTH);

    MouseListener mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          execFunction();
        }
      }
    };
    funcListPanel.registerMouseListener(mouseListener);

    ListSelectionListener listListener = new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        functionListValueChanged();
      }
    };
    funcListPanel.registerListSelectionListener(listListener);
  }

  void execButton_actionPerformed(ActionEvent e) {
    execFunction();
  }

  public void execFunction() {
    currentFunc = funcListPanel.getFunction();
    if (currentFunc == null)
      return;
    /*
    Object result = getResult();
    if (result == null)
    	return;
      */
    fireFunctionExecuted(new SpatialFunctionPanelEvent(this));
  }

  private void setCurrentFunction(GeometryFunction func) {
    currentFunc = func;
    fireFunctionExecuted(new SpatialFunctionPanelEvent(this));
  }

  private void functionListValueChanged()
  {
    currentFunc = funcListPanel.getFunction();
    updateParameterControls();
  }
  
  private void updateParameterControls()
  {
    int numNonGeomParams = numNonGeomParams(currentFunc);
    // TODO: this is a bit of a hack, and should be made smarter
    setEnabled(txtDistance, numNonGeomParams >= 1);
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
  
  private static void setEnabled(Component comp, boolean isEnabled)
  {
    comp.setEnabled(isEnabled);
    if (isEnabled)
      comp.setBackground(SystemColor.text);
    else
      comp.setBackground(SystemColor.control);
  }

  public Object getResult() {
    Object result = null;
    if (currentFunc == null || JTSTestBuilder.getGeometryA() == null)
      return null;
    
    try {
      timer = new Stopwatch();
      result = currentFunc.invoke(JTSTestBuilder.getGeometryA(), getFunctionParams());
      timer.stop();
    }
    catch (Exception ex) {
      ex.printStackTrace(System.out);
      result = ex;
    }
    return result;
  }

  private Object[] getFunctionParams()
  {
  	// TODO: this is somewhat cheesy
    Class[] paramTypes = currentFunc.getParameterTypes();
    if (paramTypes.length == 1 
        && paramTypes[0] == Geometry.class)
      return new Object[] { JTSTestBuilder.getGeometryB() };
    
    if (paramTypes.length == 1 
        && (paramTypes[0] == Double.class || paramTypes[0] == double.class))
      return new Object[] { SwingUtil.getDouble(txtDistance, null) };
    
    if (paramTypes.length == 2 
        && paramTypes[0] == Geometry.class
      && (paramTypes[1] == Double.class || paramTypes[1] == double.class))
      return new Object[] { JTSTestBuilder.getGeometryB(), SwingUtil.getDouble(txtDistance, null) };
    
    if (paramTypes.length >= 2)
      return new Object[] { 
    		SwingUtil.getDouble(txtDistance, null)
        };
    
    return null;
  }
  
  public String getOpName() {
    if (currentFunc == null)
      return "";
    return currentFunc.getName();
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

