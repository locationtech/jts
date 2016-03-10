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
 * Base class for test cases which depend on threading.
 * A common example of usage is to test for race conditions.
 * 
 * @author Martin Davis
 *
 */
public abstract class ThreadTestCase
{
  public int getThreadCount()
  {
    return ThreadTestRunner.DEFAULT_THREAD_COUNT;
    
  }
  public abstract void setup();

  public abstract Runnable getRunnable(int threadIndex);
  
}
