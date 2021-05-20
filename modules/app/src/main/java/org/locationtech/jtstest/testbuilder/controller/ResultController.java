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

package org.locationtech.jtstest.testbuilder.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.Timer;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.Stopwatch;
import org.locationtech.jtstest.geomfunction.GeometryFunction;
import org.locationtech.jtstest.geomfunction.GeometryFunctionInvocation;
import org.locationtech.jtstest.testbuilder.FunctionPanel;
import org.locationtech.jtstest.testbuilder.JTSTestBuilder;
import org.locationtech.jtstest.testbuilder.JTSTestBuilderFrame;
import org.locationtech.jtstest.testbuilder.ScalarFunctionPanel;
import org.locationtech.jtstest.testbuilder.SpatialFunctionPanel;
import org.locationtech.jtstest.testbuilder.event.SpatialFunctionPanelEvent;
import org.locationtech.jtstest.testbuilder.model.TestBuilderModel;
import org.locationtech.jtstest.testbuilder.ui.SwingWorker;


public class ResultController 
{
  private static NumberFormat timeFmt;
  static {
    timeFmt = NumberFormat.getNumberInstance();
    timeFmt.setMinimumFractionDigits(3);
  }

	JTSTestBuilderFrame frame;
	TestBuilderModel model = null;
	
	public ResultController(JTSTestBuilderFrame frame)
	{
		this.frame = frame;
		model = JTSTestBuilder.model();
	}
	
  public void spatialFunctionPanel_functionExecuted(SpatialFunctionPanelEvent e) 
  {
    SpatialFunctionPanel spatialPanel = frame.getTestCasePanel().getSpatialFunctionPanel();
    GeometryFunctionInvocation functionDesc = functionInvocation(spatialPanel);
    model.setOpName(functionDesc.getSignature());
    frame.getResultWKTPanel().setOpName(model.getOpName());
    // initialize UI view
    clearResult();
    // don't run anything if function is null
  	if (! frame.getTestCasePanel().getSpatialFunctionPanel().isFunctionSelected()) {
  		return;
  	}

    frame.setCursorWait();
    frame.getTestCasePanel().getSpatialFunctionPanel().enableExecuteControl(false);
    startFunctionMonitor();
    boolean isCreateNew = e.isCreateNew();
    runFunctionWorker(functionDesc, isCreateNew);
    // show result unless create new, in which case new case is shown
    if (! isCreateNew) frame.showResultWKTTab();
  }

  private GeometryFunctionInvocation functionInvocation(FunctionPanel functionPanel) {
    GeometryFunctionInvocation functionDesc = new GeometryFunctionInvocation(
        functionPanel.getFunction(), 
        model.getGeometryEditModel().getGeometry(0),
        functionPanel.getFunctionParams());
    return functionDesc;
  }

  private void clearResult()
  {
    frame.getResultWKTPanel().clearResult();
    // for good measure do a GC
    System.gc();
    updateResult(null,null,null);
  }
  	
  /**
   * If result is null, clears result info.
   * 
   * @param result
   * @param object 
   * @param object 
   * @param timer
   */
  private void resetUI() {
     frame.getTestCasePanel().getSpatialFunctionPanel()
         .enableExecuteControl(true);
     frame.setCursorNormal();
   }
  private void updateResult(GeometryFunctionInvocation function, Object result, Stopwatch timer) {
     model.setResult(result);
     String timeString = timer != null ? timer.getTimeString() : "";
     frame.getResultWKTPanel().setExecutedTime(timeString);
     frame.getResultWKTPanel().setResult(result);
     JTSTestBuilder.controller().geometryViewChanged();
     // log it
     resultLogEntry(function, timeString, result);
   }
  
  private void resultLogEntry(GeometryFunctionInvocation function, String timeString, Object result) {
    if (function == null) return;
    String funTimeLine = function.getSignature() + " : " + timeString;
    String entry = funTimeLine;
    String resultDesc = GeometryFunctionInvocation.toString(result);
    if (resultDesc != null && resultDesc.length() < 40) entry += "\n ==> " + resultDesc;
    JTSTestBuilder.controller().displayInfo(entry, false);
  }
  
  
  
  private SwingWorker worker = null;
  
  private void runFunctionWorker(final GeometryFunctionInvocation functionInvoc, final boolean createNew)
  {
    worker = new SwingWorker() {
    	Stopwatch timer;
    	
      public Object construct()
      {
        return computeResult();
      }
      
      private Object computeResult() {
        Object result = null;
        GeometryFunction currentFunc = functionInvoc.getFunction();
        if (currentFunc == null)
          return null;
        
        try {
          timer = new Stopwatch();
          try {
            result = currentFunc.invoke(model.getGeometryEditModel()
                .getGeometry(0), functionInvoc.getArgs());
          } finally {
            timer.stop();
          }
          // result = currentState.getActualValue();
        }
        catch (Exception ex) {
          ex.printStackTrace(System.out);
          result = ex;
        }
        return result;
      }

      public void finished() {
        stopFunctionMonitor();
        resetUI();
        Object result = getValue();
        if (createNew) {
          String desc = "Result of " + functionInvoc.getSignature();
          JTSTestBuilder.controller().addTestCase(new Geometry[] { (Geometry) result, null }, desc);          
        } else {
          updateResult(functionInvoc, result, timer);
        }
        worker = null;
      }
    };
    worker.start();
  }
  
  private void clearFunctionWorker()
  {
    
  }
  
  private Timer funcTimer;
  private long runMillis = 0;
  private static final int TIMER_DELAY_IN_MILLIS = 10;
  
  private void startFunctionMonitor()
  {
    runMillis = 0;
    funcTimer = new Timer(TIMER_DELAY_IN_MILLIS, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//        Stopwatch timer = testCasePanel.getSpatialFunctionPanel().getTimer();
        runMillis += TIMER_DELAY_IN_MILLIS;
        String timeStr = "";
        if (runMillis < 10000) {
          timeStr = runMillis + " ms";
        }
        else {
          timeStr = timeFmt.format(runMillis/1000.0) + " s";
        }
        frame.getResultWKTPanel().setRunningTime(timeStr);
      }
    });
    funcTimer.setInitialDelay(0);
    funcTimer.start(); 
  }
  
  private void stopFunctionMonitor()
  {
    funcTimer.stop();
  }

  public void executeScalarFunction() 
  {
    /**
     * For now scalar functions are executed on the calling thread.
     * They are expected to be of short duration
     */
    ScalarFunctionPanel scalarPanel = frame.getTestCasePanel().getScalarFunctionPanel();
    String opName = scalarPanel.getOpName();
    // initialize UI view
    frame.getResultValuePanel().setResult(opName, "", null);
    frame.showResultValueTab(); 
    
    frame.setCursorWait();
    Object result = scalarPanel.getResult();
    frame.setCursorNormal();
    
    Stopwatch timer = scalarPanel.getTimer();
    String timeString = timer.getTimeString();
    
    frame.getResultValuePanel().setResult(opName, timer.getTimeString(), result);

    resultLogEntry(functionInvocation(scalarPanel), timeString, result);
  }

}
