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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.util.Stopwatch;
import org.locationtech.jtstest.geomfunction.BaseGeometryFunction;
import org.locationtech.jtstest.geomfunction.GeometryFunction;
import org.locationtech.jtstest.geomfunction.GeometryFunctionRegistry;
import org.locationtech.jtstest.geomfunction.RepeaterGeometryFunction;
import org.locationtech.jtstest.geomfunction.SpreaderGeometryFunction;
import org.locationtech.jtstest.testbuilder.controller.JTSTestBuilderController;
import org.locationtech.jtstest.testbuilder.event.GeometryFunctionEvent;
import org.locationtech.jtstest.testbuilder.event.GeometryFunctionListener;
import org.locationtech.jtstest.testbuilder.event.SpatialFunctionPanelEvent;
import org.locationtech.jtstest.testbuilder.event.SpatialFunctionPanelListener;
import org.locationtech.jtstest.testbuilder.ui.SwingUtil;
import org.locationtech.jtstest.util.ClassUtil;


/**
 * @version 1.7
 */
public class SpatialFunctionPanel 
extends JPanel implements FunctionPanel 
{
  private static final EmptyBorder LABEL_BORDER = new EmptyBorder(3,5,3,5);

  private static final String[] PARAM_DEFAULT = { "10", "0", "0", "0", "0" };
  
  private static String[] capStyleItems = new String[] { "Round", "Flat", "Square" };
  private static Object[] capStyleValues = new Object[] { 
  		BufferParameters.CAP_ROUND,
  		BufferParameters.CAP_FLAT,
  		BufferParameters.CAP_SQUARE
  		};
  private static String[] joinStyleItems = new String[] { "Round", "Mitre", "Bevel" };
  private static Object[] joinStyleValues = new Object[] { 
  		BufferParameters.JOIN_ROUND,
  		BufferParameters.JOIN_MITRE,
  		BufferParameters.JOIN_BEVEL
  };

	
	
  JPanel panelExecControl = new JPanel();
//  GeometryFunctionListPanel geomFuncPanel = new GeometryFunctionListPanel();
  GeometryFunctionTreePanel geomFuncPanel = new GeometryFunctionTreePanel();
  GridLayout gridLayout1 = new GridLayout();
  GridLayout gridLayout2 = new GridLayout();


  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();

  JPanel panelParam = new JPanel();
  JPanel panelExec = new JPanel();
  JPanel panelExecMeta = new JPanel();
  JPanel panelExecParam = new JPanel();
  FlowLayout flowLayout = new FlowLayout();
  FlowLayout flowLayout1 = new FlowLayout();
  FlowLayout flowLayout2 = new FlowLayout();
  
  private JButton execButton = new JButton();
  private JButton execToNewButton = new JButton();
  
  private final ImageIcon clearIcon = new ImageIcon(this.getClass().getResource("clear.gif"));
  private final ImageIcon expandDownIcon = new ImageIcon(this.getClass().getResource("Expand-Down.png"));
  
  private transient Vector spatialFunctionPanelListeners;
  private JPanel panelControl = new JPanel();
  private JCheckBox displayAAndBCheckBox = new JCheckBox();
  private JCheckBox cbExecEachA = new JCheckBox();
  private JCheckBox cbExecEachB = new JCheckBox();
  private JCheckBox cbExecRepeat = new JCheckBox();
  private final JTextField txtRepeatCount = new JTextField();
  private JButton btnClearResult = new JButton();

  private JLabel lblFunctionName = new JLabel();
  private JLabel lblFunction = new JLabel();
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

  private JButton btnExecEach;
  
  public SpatialFunctionPanel() {
    try {
      uiInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  void uiInit() throws Exception {
    this.setLayout(borderLayout1);
    
//    geomFuncPanel.populate(JTSTestBuilder.getFunctionRegistry().getGeometryFunctions());
    geomFuncPanel.populate(JTSTestBuilder.getFunctionRegistry().getCategorizedGeometryFunctions());

    panelParam.setLayout(gridLayout2);
    gridLayout2.setRows(6);
    gridLayout2.setColumns(2);
    panelExec.setLayout(flowLayout);
    panelExecMeta.setLayout(flowLayout2);
    panelExecParam.setLayout(borderLayout2);

    
    displayAAndBCheckBox.setSelected(true);
    displayAAndBCheckBox.setToolTipText("");
    displayAAndBCheckBox.setText("Display Input");
    displayAAndBCheckBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          displayAAndBCheckBox_actionPerformed(e);
        }
      });

    lblFunction.setText("Function");
    lblFunction.setHorizontalAlignment(SwingConstants.RIGHT);
    lblFunction.setBorder(LABEL_BORDER);//top,left,bottom,right
    
    lblFunctionName.setHorizontalAlignment(SwingConstants.LEFT);
    lblFunctionName.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
    
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

    initLabels(paramLabel);
    


    btnClearResult.setToolTipText("");
    btnClearResult.setMargin(new Insets(0, 10, 0, 10));
    btnClearResult.setSelected(true);
    btnClearResult.setText("Clear Result");
    btnClearResult.addActionListener(new java.awt.event.ActionListener() {

        public void actionPerformed(ActionEvent e) {
          clearResultButton_actionPerformed(e);
        }
      });

    panelParam.add(lblFunction);
    panelParam.add(lblFunctionName);
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

    panelControl.setLayout(flowLayout1);
    panelControl.add(displayAAndBCheckBox, null);
    //panelControl.add(btnClearResult, null);
    

    cbExecEachA.setToolTipText("Compute for each A geometry element");
    cbExecEachA.setText("Each A");
    
    cbExecEachB.setToolTipText("Compute for each B geometry element");
    cbExecEachB.setText("Each B");
    
    cbExecRepeat.setToolTipText("Repeat function a number of times, incrementing the first parameter");
    cbExecRepeat.setText("Repeat");
    
    txtRepeatCount.setMaximumSize(new Dimension(25, 2147483647));
    txtRepeatCount.setMinimumSize(new Dimension(30, 21));
    txtRepeatCount.setPreferredSize(new Dimension(30, 21));
    txtRepeatCount.setText("10");
    txtRepeatCount.setHorizontalAlignment(SwingConstants.RIGHT); 
    
    execButton = SwingUtil.createButton(AppIcons.EXECUTE, AppStrings.TIP_EXECUTE,
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execButton_actionPerformed(e);
      }
    });
    execButton.setEnabled(false);
    
    execToNewButton = SwingUtil.createButton("New", AppIcons.EXECUTE, "Compute function result to a new case",
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execToNewButton_actionPerformed(e);
      }
    });
    execToNewButton.setEnabled(false); 
    
    JButton btnShowExecExt = SwingUtil.createButton(expandDownIcon, "Show extended/meta Compute tools",
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearExtended();
        panelExecMeta.setVisible(! panelExecMeta.isVisible());
      }
    });
    btnShowExecExt.setPreferredSize(new Dimension(20, 20));
    btnShowExecExt.setBorder(BorderFactory.createEmptyBorder());
    btnShowExecExt.setContentAreaFilled(false);
    btnShowExecExt.setFocusable(false);

    panelExec.add(execButton);
    // disabled until behaviour is worked out
    panelExec.add(execToNewButton);
    //panelExec.add(btnShowExecExt);

    JPanel panelExecHolder = new JPanel();
    panelExecHolder.setLayout(new BorderLayout());
    panelExecHolder.add(panelExec, BorderLayout.CENTER);
    panelExecHolder.add(btnShowExecExt, BorderLayout.EAST);

    panelExecMeta.add(cbExecEachA);
    panelExecMeta.add(cbExecEachB);
    panelExecMeta.add(cbExecRepeat);
    panelExecMeta.add(txtRepeatCount);
    panelExecMeta.setVisible(false);
    
    panelExecControl.setLayout(new BoxLayout(panelExecControl, BoxLayout.Y_AXIS));
    panelExecControl.add(panelExecHolder);
    panelExecControl.add(panelExecMeta);
    panelExecControl.add(panelControl);
    
    panelExecParam.add(panelParam, BorderLayout.CENTER);
    panelExecParam.add(panelExecControl, BorderLayout.SOUTH);
    
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
    
    hideAllParams(paramComp, paramLabel);
  }

  static void initLabels(JLabel[] paramLabel)
  {
    for (int i = 0; i < paramLabel.length; i++) {
      JLabel lbl = paramLabel[i];
      lbl.setHorizontalAlignment(SwingConstants.RIGHT);
      lbl.setBorder(LABEL_BORDER);
    }
  }

  public void enableExecuteControl(boolean isEnabled)
  {
    execButton.setEnabled(isEnabled);
    execToNewButton.setEnabled(isEnabled);
  }
  
  void clearResultButton_actionPerformed(ActionEvent e) {
    clearFunction();
  }

  void execButton_actionPerformed(ActionEvent e) {
    execFunction(getMetaFunction(), false);
  }

  void execToNewButton_actionPerformed(ActionEvent e) {
    execFunction(getMetaFunction(), true);
  }

  GeometryFunction getMetaFunction() {
    GeometryFunction funToRun = geomFuncPanel.getFunction();
    if (! isMetaFunctionEnabled()) return funToRun;
    
    if (isFunctionRepeated()) {
      int count = SwingUtil.getInteger(txtRepeatCount, 10);
      funToRun = new RepeaterGeometryFunction(funToRun, count);
    }
    if (isFunctionEach()) {
      funToRun = new SpreaderGeometryFunction(funToRun, isEachA(), isEachB());
    }
    return funToRun;
  }


  private boolean isMetaFunctionEnabled() {
    return panelExecMeta.isVisible();
  }

  private boolean isFunctionRepeated() {
    return cbExecRepeat.isSelected();
  }
  private boolean isFunctionEach() {
    return cbExecEachA.isSelected() || cbExecEachB.isSelected();
  }
  private boolean isEachA() {
    return cbExecEachA.isSelected();
  }
  private boolean isEachB() {
    return cbExecEachB.isSelected();
  }
  void clearExtended() {
    cbExecRepeat.setSelected(false);
    cbExecEachA.setSelected(false);
    cbExecEachB.setSelected(false);
  }
  
  void displayAAndBCheckBox_actionPerformed(ActionEvent e) {
    JTSTestBuilderController.editPanel().setShowingInput(displayAAndBCheckBox.isSelected());
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
    lblFunctionName.setText(func.getName());
    lblFunctionName.setToolTipText( GeometryFunctionRegistry.functionDescriptionHTML(func) );
    
    updateParameters(func, paramComp, paramLabel);
    
    execButton.setEnabled(true);
    execToNewButton.setEnabled(true); 
  }
  
  static void updateParameters(GeometryFunction func, JComponent[] paramComp, JLabel[] paramLabel) {
    int numNonGeomParams = numNonGeomParams(func);
    int indexOffset = BaseGeometryFunction.firstScalarParamIndex(func);
    for (int i = 0; i < paramComp.length; i++) {
      boolean isUsed = numNonGeomParams > i;
      if (isUsed) {
        paramLabel[i].setText(func.getParameterNames()[i+indexOffset]);
      }      
      paramComp[i].setVisible(isUsed);
      paramLabel[i].setVisible(isUsed);
      SpatialFunctionPanel.setToolTipText(paramComp[i], func, i);      
    }
  }
  
  static void hideAllParams(JComponent[] paramComp, JLabel[] paramLabel) {
    for (int i = 0; i < paramComp.length; i++) {     
      paramComp[i].setVisible(false);
      paramLabel[i].setVisible(false);     
    }    
  }
  
  private static void setToolTipText(JComponent control, GeometryFunction func, int i) {
    String txt = null;
    if (func.getParameterTypes().length > i) {
      txt = "Enter a " + func.getParameterTypes()[i].getSimpleName();
    }
    control.setToolTipText(txt);
  }
  
  private static int numNonGeomParams(GeometryFunction func)
  {
    int count = 0;
    Class[] paramTypes = func.getParameterTypes();
    for (int i = 0; i < paramTypes.length; i++) {
      if (! ClassUtil.isGeometry(paramTypes[i]))
        count++;
    }
    return count;
  }
  
  public static int attributeParamOffset(GeometryFunction func) {
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
      return JTSTestBuilder.controller().getGeometryB();
    
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

  public static String valOrDefault(String s, String defaultVal) {
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

