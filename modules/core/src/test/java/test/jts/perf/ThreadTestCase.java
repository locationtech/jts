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
