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
 * An example of the usage of the {@PerformanceTestRunner}.
 * 
 * @author Martin Davis
 *
 */
public class ExamplePerfTest
extends PerformanceTestCase
{

  public static void main(String args[]) {
    PerformanceTestRunner.run(ExamplePerfTest.class);
  }

  public ExamplePerfTest(String name)
  {
    super(name);
    setRunSize(new int[] { 5, 10, 20 });
    setRunIterations(10);
  }

  public void setUp()
  {
    // read data and allocate resources here
  }
  
  public void startRun(int size)
  {
    System.out.println("Running with size " + size);
    iter = 0;
  }
  
  private int iter = 0;
  
  public void runTest1()
  {
    System.out.println("Test 1 : Iter # " + iter++);
    // do test work here
  }
  
  public void runTest2()
  {
    System.out.println("Test 2 : Iter # " + iter++);
    // do test work here
  }
  
  public void tearDown()
  {
    // deallocate resources here
  }
}
