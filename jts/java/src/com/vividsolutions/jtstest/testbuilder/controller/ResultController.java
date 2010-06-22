package com.vividsolutions.jtstest.testbuilder.controller;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Stopwatch;
import com.vividsolutions.jtstest.function.GeometryFunction;
import com.vividsolutions.jtstest.testbuilder.JTSTestBuilder;
import com.vividsolutions.jtstest.testbuilder.JTSTestBuilderFrame;
import com.vividsolutions.jtstest.testbuilder.SpatialFunctionPanelEvent;
import com.vividsolutions.jtstest.testbuilder.model.TestBuilderModel;
import com.vividsolutions.jtstest.testbuilder.ui.SwingWorker;

public class ResultController 
{
	JTSTestBuilderFrame frame;
	TestBuilderModel model = null;
	
	public ResultController(JTSTestBuilderFrame frame)
	{
		this.frame = frame;
		model = JTSTestBuilder.model();
	}
	
  public void spatialFunctionPanel_functionChanged(SpatialFunctionPanelEvent e) 
  {
    // don't run anything if function is null
  	if (! frame.getTestCasePanel().getSpatialFunctionPanel().isFunctionSelected())
  		return;
  	
    String opName = frame.getTestCasePanel().getSpatialFunctionPanel().getFunctionSignature();
    model.setOpName(opName);
    frame.getResultWKTPanel().setOpName(model.getOpName());
    // initialize UI view
    clearResult();

    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    frame.getTestCasePanel().getSpatialFunctionPanel().enableExecuteControl(false);
    startFunctionMonitor();
    runFunctionWorker();
    frame.showResultWKTTab();
  }

  private void clearResult()
  {
    frame.getResultWKTPanel().clearResult();
    updateResult(null);
  }
  	
  private void updateResult(Object result)
  {
  	model.setResult(result);
    frame.getResultWKTPanel().updateResult();
    frame.getTestCasePanel().getGeometryEditPanel().updateView();
  }
  
  private SwingWorker worker = null;
  
  private void runFunctionWorker()
  {
    worker = new SwingWorker() {
    	Stopwatch timer;
    	
      public Object construct()
      {
        return getResult();
      }
      
      private Object getResult() {
        Object result = null;
        GeometryFunction currentFunc = frame.getTestCasePanel().getSpatialFunctionPanel().getFunction();
        if (currentFunc == null)
          return null;
        
        try {
        	timer = new Stopwatch();
          result = currentFunc.invoke(model.getGeometryEditModel().getGeometry(0), 
          		frame.getTestCasePanel().getSpatialFunctionPanel().getFunctionParams());
          timer.stop();
//          result = currentState.getActualValue();
        }
        catch (Exception ex) {
          ex.printStackTrace(System.out);
          result = ex;
        }
        return result;
      }

      public void finished() {
        stopFunctionMonitor();
        frame.getTestCasePanel().getSpatialFunctionPanel().enableExecuteControl(true);
        Object result = getValue();
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        //Stopwatch timer = frame.getTestCasePanel().getSpatialFunctionPanel().getTimer();
        frame.getResultWKTPanel().setExecutedTime(timer.getTimeString());
        updateResult(result);
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
  private static final int DELAY_IN_MILLIS = 100;
  
  private void startFunctionMonitor()
  {
    runMillis = 0;
    funcTimer = new Timer(DELAY_IN_MILLIS, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//        Stopwatch timer = testCasePanel.getSpatialFunctionPanel().getTimer();
        runMillis += DELAY_IN_MILLIS;
        frame.getResultWKTPanel().setRunningTime((runMillis/1000.0) + " s");
      }
    });
    funcTimer.setInitialDelay(0);
    funcTimer.start(); 
  }
  
  private void stopFunctionMonitor()
  {
    funcTimer.stop();
  }

  public void scalarFunctionPanel_functionChanged(SpatialFunctionPanelEvent e) 
  {
    String opName = frame.getTestCasePanel().getScalarFunctionPanel().getOpName();
    // initialize UI view
    frame.getResultValuePanel().setResult(opName, "", null);
    
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    Object result = frame.getTestCasePanel().getScalarFunctionPanel().getResult();
    Stopwatch timer = frame.getTestCasePanel().getScalarFunctionPanel().getTimer();
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    
    frame.getResultValuePanel().setResult(opName, timer.getTimeString(), result);
    frame.showResultValueTab();
  }


}
