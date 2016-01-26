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
 * Runs a {@link ThreadTestCase}.
 * 
 * @author Martin Davis
 *
 */
public class ThreadTestRunner
{

  public static final int DEFAULT_THREAD_COUNT = 10;

  public static void run(ThreadTestCase testcase)
  {
    testcase.setup();
    
    for (int i = 0; i < testcase.getThreadCount(); i++) {
      Runnable runnable = testcase.getRunnable(i);
      Thread t = new Thread(runnable);
      t.start();
    }
  }
  
 
}
