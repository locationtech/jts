/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf;

/**
 * A base class for classes implementing performance tests
 * to be run by the {@link PerformanceTestRunner}.
 * <p>
 * The {@link #setUp()} is called at the start of test class execution, 
 * and {@link #tearDown()} is called at the end.  
 * These allow creating and release resources needed for testing
 * (e.g. a database connection).
 * <p>
 * Subclasses provide performance tests as
 * public methods which start with <code>run</code>.
 * Each test is executed once per test run.
 * The number of runs is determined by the length
 * of the array provided to {@link #setRunSize(int[])}.  
 * The array entry for each run specifies a size for the run.
 * The size is provided at the start of each run via {@link #startRun(int)}.
 * It can be used to determine the size of data to generate for the run.
 * <p>
 * Within a run, each <code>run</code> test method is executed 
 * for the number of iterations specified by {@link #setRunIterations(int).
 * This allows running tests of fast operations long enough for accurate timing.
 * A performance report is printed after each test.
 * <p>
 * {@link #endRun()} is called at the end of the run.
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
  
  /**
   * Gets the name of this test case.
   * 
   * @return the name of the test case
   */
  public String getName()
  {
    return name;
  }
  
  /**
   * Sets the size(s) for the runs of the test(s).
   * The default is one run with a size of 1.
   * 
   * @param runSize a list of the sizes for the test runs
   */
  protected void setRunSize(int[] runSize)
  {
    this.runSize = runSize;
    runTime = new long[runSize.length];
  }
  
  /**
   * Gets the array of run sizes.
   * 
   * @return the array of run sizes
   */
  public int[] getRunSize()
  {
    return runSize;
  }
  
  /**
   * Gets the run times for the final run method in each run.
   * This allows comparing run times across different run sizes
   * (e.g. to compute a time factor).
   * 
   * @return the run times
   */
  public long[] getRunTime()
  {
    return runTime;
  }
  
  /**
   * Sets the number of iterations to execute the test methods in each test run.
   * The default is 1 iteration.
   * 
   * @param runIter the number of iterations to execute.
   */
  protected void setRunIterations(int runIter)
  {
    this.runIter = runIter;
  }
  
  /**
   * Gets the number of iterations for the run methods.
   * 
   * @return the number of iterations
   */
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
