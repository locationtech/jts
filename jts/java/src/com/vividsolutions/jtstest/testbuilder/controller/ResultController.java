package com.vividsolutions.jtstest.testbuilder.controller;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

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
    model.setOpName(frame.getTestCasePanel().getSpatialFunctionPanel().getFunctionCall());
    frame.getResultWKTPanel().setOpName(model.getOpName());
    // initialize UI view
    clearResult();
    // don't run anything if function is null
  	if (! frame.getTestCasePanel().getSpatialFunctionPanel().isFunctionSelected()) {
  		return;
  	}

    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    frame.getTestCasePanel().getSpatialFunctionPanel().enableExecuteControl(false);
    startFunctionMonitor();
    runFunctionWorker();
    frame.showResultWKTTab();
  }

  private void clearResult()
  {
    frame.getResultWKTPanel().clearResult();
    // for good measure do a GC
    System.gc();
    updateResult(null,null);
  }
  	
  /**
   * If result is null, clears result info.
   * 
   * @param result
   * @param timer
   */
  private void updateResult(Object result, Stopwatch timer)
  {
  	model.setResult(result);
  	String timeString = timer != null ? timer.getTimeString() : "";
    frame.getResultWKTPanel().setExecutedTime(timeString);
    frame.getResultWKTPanel().updateResult();
    JTSTestBuilderController.geometryViewChanged();
    frame.getTestCasePanel().getSpatialFunctionPanel().enableExecuteControl(true);
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    // log it
    if (result != null) {
    JTSTestBuilderFrame.instance().displayInfo(
        frame.getTestCasePanel().getSpatialFunctionPanel().getFunctionCall()
        + " : " + timeString,
        false);
    }
  }
  
  private SwingWorker worker = null;
  
  private void runFunctionWorker()
  {
    worker = new SwingWorker() {
    	Stopwatch timer;
    	
      public Object construct()
      {
        return computeResult();
      }
      
      private Object computeResult() {
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
        updateResult(getValue(), timer);
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

  public void scalarFunctionPanel_functionExecuted(SpatialFunctionPanelEvent e) 
  {
    /**
     * For now scalar functions are executed on the calling thread.
     * They are expected to be of short duration
     */
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
