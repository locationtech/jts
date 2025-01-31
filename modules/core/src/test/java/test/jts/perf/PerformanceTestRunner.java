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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.util.Stopwatch;


/**
 * Runs {@link PerformanceTestCase} classes which contain performance tests.
 * 
 * 
 * 
 * @author Martin Davis
 *
 */
public class PerformanceTestRunner
{
  private static final String RUN_PREFIX = "run";

  public static void run(Class clz)
  {
    PerformanceTestRunner runner = new PerformanceTestRunner();
    runner.runInternal(clz);
  }

  private PerformanceTestRunner()
  {
    
  }
  
  private void runInternal(Class clz)
  {
    try {
      Constructor ctor = clz.getConstructor(String.class);
      PerformanceTestCase test = (PerformanceTestCase) ctor.newInstance("Name");
      int[] runSize = test.getRunSize();
      int runIter = test.getRunIterations();
      Method[] runMethod = findMethods(clz, RUN_PREFIX);
      
      // do the run
      test.setUp();
      //-- initial times are zero (factor is not printed)
      long[] runTimePrev = new long[runMethod.length];
      
      for (int runNum = 0; runNum < runSize.length; runNum++)
      {
        int size = runSize[runNum];
        test.startRun(size);
        for (int i = 0; i < runMethod.length; i++) {
          Stopwatch sw = new Stopwatch();
          for (int iter = 0; iter < runIter; iter++) {
            runMethod[i].invoke(test);
          }
          long time = sw.getTime();
          long timePrev = runTimePrev[i];
          int sizePrev = runNum > 0 ? runSize[runNum-1] : -1;
          reportRun(runMethod[i].getName(), sw.getTimeString(), size, time, sizePrev, timePrev);
          runTimePrev[i] = time;
          test.setTime(runNum, time);
        }
        test.endRun();
      }
      test.tearDown();
    }
    catch (InvocationTargetException e) {
      e.getTargetException().printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  private void reportRun(String name, String timeString, int size, long time, int sizePrev, long timePrev) {
    String factorStr = "";
    if (sizePrev > 0 && timePrev > 0) {
      double sizeFactor = size / (double) sizePrev;
      double timeFactor = time / (double) timePrev;
      factorStr = String.format( "  ( %.1fx - size %.1fx)", timeFactor, sizeFactor);
    }
    System.out.println(name
        + " : " + timeString + factorStr);
  }

  private static Method[] findMethods(Class clz, String methodPrefix)
  {
    List runMeths = new ArrayList();
    Method meth[] = clz.getDeclaredMethods();
    for (int i = 0; i < meth.length; i++) {
      if (meth[i].getName().startsWith(RUN_PREFIX)) {
        runMeths.add(meth[i]);
      }
    }
    return (Method[]) runMeths.toArray(new Method[0]);
  }
}
