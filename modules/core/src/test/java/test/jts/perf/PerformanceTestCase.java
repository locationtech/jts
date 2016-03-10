/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf;

/**
 * A base class for classes implementing performance tests
 * to be run by the {@link PerformanceTestRunner}.
 * <p>
 * In a subclass of this class,
 * all public methods which start with <code>run</code> are 
 * executed as performance tests.
 * <p>
 * Multiple test runs with different run sizes may be made.
 * Within each run, each <code>run</code> method is executed 
 * the specified number of iterations.
 * The time to run the method is printed for each one.
 * 
 * @author Martin Davis
 *
 */
public abstract class PerformanceTestCase
{
  private String name;
  private int[] runSize = new int[] { 1 };
  private int runIter = 1;
  private long[] runTime;
  
  public PerformanceTestCase(String name)
  {
    this.name = name;
  }
  
  public String getName()
  {
    return name;
  }
  
  /**
   * Sets the size(s) for the runs of the test.
   * 
   * @param runSize a list of the sizes for the test runs
   */
  protected void setRunSize(int[] runSize)
  {
    this.runSize = runSize;
    runTime = new long[runSize.length];
  }
  
  public int[] getRunSize()
  {
    return runSize;
  }
  
  public long[] getRunTime()
  {
    return runTime;
  }
  
  /**
   * Sets the number of iterations to execute the test methods in each test run.
   * 
   * @param runIter the number of iterations to execute.
   */
  protected void setRunIterations(int runIter)
  {
    this.runIter = runIter;
  }
  
  public int getRunIterations()
  {
    return runIter;
  }
  
  /**
   * Sets up any fixtures needed for the test runs.
   * 
   * @throws Exception
   */
  public void setUp()
  throws Exception
  {
    
  }
  
  /**
   * Starts a test run with the given size.
   * 
   * @param size
   * @throws Exception
   */
  public void startRun(int size)
  throws Exception
  {
    
  }
  
  /**
   * Ends a test run.
   * 
   * @throws Exception
   */
  public void endRun()
  throws Exception
  {
    
  }
  
  /**
   * Tear down any fixtures made for the testing.
   * 
   * @throws Exception
   */
  public void tearDown()
  throws Exception
  {
    
  }

  void setTime(int runNum, long time) {
    runTime[runNum] = time;
  }
  
  
}
