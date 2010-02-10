package com.vividsolutions.jtstest.testbuilder;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Stopwatch;
import com.vividsolutions.jtstest.testbuilder.ui.SwingWorker;

public class ResultController 
{
	JTSTestBuilderFrame frame;
	
	public ResultController(JTSTestBuilderFrame frame)
	{
		this.frame = frame;
	}
	
  public void spatialFunctionPanel_functionChanged(SpatialFunctionPanelEvent e) 
  {
    String opName = frame.testCasePanel.getSpatialFunctionPanel().getFullName();
    frame.resultWKTPanel.setOpName(opName);
    // initialize UI view
    clearResult();
    // don't run anything if function is null
    if (opName == null) {
      return;
    }

    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    frame.testCasePanel.getSpatialFunctionPanel().enableExecuteControl(false);
    startFunctionMonitor();
    runFunctionWorker();
    frame.showResultWKTTab();
  }

  private void clearResult()
  {
    frame.resultWKTPanel.clearResult();
    updateResult(null);
  }
  	
  private void updateResult(Object result)
  {
    frame.resultWKTPanel.setResult(result);
    if (result == null || result instanceof Geometry) {
    	frame.tbModel.getCurrentTestCaseEdit().setResult((Geometry) result);
    }

    frame.getTestCasePanel().getGeometryEditPanel().updateView();
  }
  
  private SwingWorker worker = null;
  
  private void runFunctionWorker()
  {
    worker = new SwingWorker() {
      public Object construct()
      {
        return frame.testCasePanel.getSpatialFunctionPanel().getResult();
      }
      
      public void finished() {
        stopFunctionMonitor();
        frame.testCasePanel.getSpatialFunctionPanel().enableExecuteControl(true);
        Object result = getValue();
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        Stopwatch timer = frame.testCasePanel.getSpatialFunctionPanel().getTimer();
        frame.resultWKTPanel.setExecutedTime(timer.getTimeString());
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
        frame.resultWKTPanel.setRunningTime((runMillis/1000.0) + " s");
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
    String opName = frame.testCasePanel.getScalarFunctionPanel().getOpName();
    // initialize UI view
    frame.resultValuePanel.setResult(opName, "", null);
    
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    Object result = frame.testCasePanel.getScalarFunctionPanel().getResult();
    Stopwatch timer = frame.testCasePanel.getScalarFunctionPanel().getTimer();
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    
    frame.resultValuePanel.setResult(opName, timer.getTimeString(), result);
    frame.showResultValueTab();
  }


}
