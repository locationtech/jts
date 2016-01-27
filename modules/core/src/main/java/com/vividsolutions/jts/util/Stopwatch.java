
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jts.util;

/**
 * Implements a timer function which can compute
 * elapsed time as well as split times.
 *
 * @version 1.7
 */
public class Stopwatch {

  private long startTimestamp;
  private long totalTime = 0;
  private boolean isRunning = false;

  public Stopwatch()
  {
    start();
  }

  public void start()
  {
    if (isRunning) return;
    startTimestamp = System.currentTimeMillis();
    isRunning = true;
  }

  public long stop()
  {
    if (isRunning) {
      updateTotalTime();
      isRunning = false;
    }
    return totalTime;
  }

  public void reset()
  {
    totalTime = 0;
    startTimestamp = System.currentTimeMillis();
  }

  public long split()
  {
    if (isRunning)
      updateTotalTime();
    return totalTime;
  }

  private void updateTotalTime()
  {
    long endTimestamp = System.currentTimeMillis();
    long elapsedTime = endTimestamp - startTimestamp;
    startTimestamp = endTimestamp;
    totalTime += elapsedTime;
  }

  public long getTime()
  {
    updateTotalTime();
    return totalTime;
  }

  public String getTimeString()
  {
    long totalTime = getTime();
    return getTimeString(totalTime);
  }

  public static String getTimeString(long timeMillis) {
    String totalTimeStr = timeMillis < 10000 
        ? timeMillis + " ms" 
        : (double) timeMillis / 1000.0 + " s";
    return totalTimeStr;
  }
}
