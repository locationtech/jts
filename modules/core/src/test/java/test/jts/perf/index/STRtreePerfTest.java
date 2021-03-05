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
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.util.Stopwatch;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class STRtreePerfTest
extends PerformanceTestCase {

  public static void main(String args[]) {
    PerformanceTestRunner.run(STRtreePerfTest.class);
  }
  
  private STRtree index;

  public STRtreePerfTest(String name) {
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
    index = new STRtree(); 
    int side = (int) Math.sqrt(size);
    for (int i = 0; i < side; i++) {
      for (int j = 0; j < side; j++) {
        Envelope env = new Envelope(i, i + 10, j, j + 10 );
        index.insert(env, i+"-"+j);
      }
    }
    Stopwatch sw = new Stopwatch();
    index.build();
    System.out.println("Build time = " + sw.getTimeString());
  }
  
  public void runQueries() {

    CountItemVisitor visitor = new CountItemVisitor();
    
    int size = index.size();
    int side = (int) Math.sqrt(size);
    //side = 10;
    for (int i = 0; i < side; i++) {
      for (int j = 0; j < side; j++) {
        Envelope env = new Envelope(i, i+40, j, j+40);
        index.query(env, visitor);
        //System.out.println(visitor.count);
      }
    }
    //System.out.println("Node compares = " + index.nodeIntersectsCount);
    System.out.println("Total query result items = " + visitor.count);

  };
}
