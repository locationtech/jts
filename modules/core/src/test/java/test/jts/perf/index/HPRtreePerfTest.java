/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.index;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.hprtree.HPRtree;
import org.locationtech.jts.util.Stopwatch;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class HPRtreePerfTest
extends PerformanceTestCase {

  private static final int NODE_SIZE = 32;
  private static final int ITEM_ENV_SIZE = 10;
  private static final int QUERY_ENV_SIZE = 40;

  public static void main(String args[]) {
    PerformanceTestRunner.run(HPRtreePerfTest.class);
  }
  
  private HPRtree index;

  public HPRtreePerfTest(String name) {
    super(name);
    setRunSize(new int[] { 100, 10000, 100000 });
    setRunIterations(1);
  }

  public void setUp()
  {
    
  }
  
  public void startRun(int size)
  {
    System.out.println("----- Tree size: " + size);
    
    index = new HPRtree(NODE_SIZE);
    int side = (int) Math.sqrt(size);
    loadGrid(side, index);
    
    Stopwatch sw = new Stopwatch();
    index.build();
    System.out.println("Build time = " + sw.getTimeString());
  }

  private static void loadGrid(int side, SpatialIndex index) {
    for (int i = 0; i < side; i++) {
      for (int j = 0; j < side; j++) {
        Envelope env = new Envelope(i, i + ITEM_ENV_SIZE, j, j + ITEM_ENV_SIZE);
        index.insert(env, i+"-"+j);
      }
    }
  }
  
  public void runQueries() {
    CountItemVisitor visitor = new CountItemVisitor();
    
    int size = index.size();
    int side = (int) Math.sqrt(size);
    //side = 10;
    for (int i = 0; i < side; i++) {
      for (int j = 0; j < side; j++) {
        Envelope env = new Envelope(i, i + QUERY_ENV_SIZE, j, j + QUERY_ENV_SIZE);
        index.query(env, visitor);
        //System.out.println(visitor.count);
      }
    }
    //System.out.println("Node compares = " + index.nodeIntersectsCount);
    System.out.println("Total query result items = " + visitor.count);
  }
}
