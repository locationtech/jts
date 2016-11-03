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
