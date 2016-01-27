/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Martin Davis
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Martin Davis BSD
 * License v1.0 (found at the root of the repository).
 * 
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
