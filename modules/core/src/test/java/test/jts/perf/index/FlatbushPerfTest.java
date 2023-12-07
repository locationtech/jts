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
import org.locationtech.jts.index.hprtree.HPRtree;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.util.Stopwatch;
import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

import java.util.Random;

/**
 * Reproduce the performance benchmark scenario that
 * <a href="https://github.com/mourner/flatbush/blob/main/bench.js">Flatbush</a>
 * uses, and run against spatial indexes.
 */
public class FlatbushPerfTest extends PerformanceTestCase {
  private static final int ITEMS = 1_000_000;
  private static final int QUERIES = 1_000;

  public static void main(String[] args) {
    PerformanceTestRunner.run(FlatbushPerfTest.class);
  }

  private HPRtree hprtree;
  private STRtree strtree;
  private Envelope[] boxes;

  public FlatbushPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 1, 10, (int) (100 * Math.sqrt(0.1))});
    setRunIterations(1);
  }

  private static Envelope randomBox(Random random, double boxSize) {
    double x = random.nextDouble() * (100d - boxSize);
    double y = random.nextDouble() * (100d - boxSize);
    double x2 = x + random.nextDouble() * boxSize;
    double y2 = y + random.nextDouble() * boxSize;
    return new Envelope(x, x2, y, y2);
  }

  public void setUp()
  {
    Random random = new Random(0);
    Envelope[] envs = new Envelope[ITEMS];

    for (int i = 0; i < ITEMS; i++) {
      envs[i] = randomBox(random, 1);
    }

    hprtree = new HPRtree();
    Stopwatch sw = new Stopwatch();
    for (Envelope env : envs) {
      hprtree.insert(env, env);
    }
    hprtree.build();
    System.out.println("HPRTree Build time = " + sw.getTimeString());

    strtree = new STRtree();
    sw = new Stopwatch();
    for (Envelope env : envs) {
      strtree.insert(env, env);
    }
    strtree.build();
    System.out.println("STRTree Build time = " + sw.getTimeString());
  }
  
  public void startRun(int size)
  {
    System.out.println("----- Query size: " + size);
    Random random = new Random(0);
    boxes = new Envelope[QUERIES];
    for (int i = 0; i < QUERIES; i++) {
      boxes[i] = randomBox(random, size);
    }
  }
  
  public void runQueriesHPR() {
    CountItemVisitor visitor = new CountItemVisitor();
    for (Envelope box : boxes) {
        hprtree.query(box, visitor);
    }
    System.out.println("HPRTree query result items = " + visitor.count);
  }

  public void runQueriesSTR() {
    CountItemVisitor visitor = new CountItemVisitor();
    for (Envelope box : boxes) {
      strtree.query(box, visitor);
    }
    System.out.println("STRTree query result items = " + visitor.count);
  }
}
