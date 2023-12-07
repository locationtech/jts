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
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.util.Stopwatch;
import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Reproduce the performance benchmark scenario that
 * <a href="https://github.com/mourner/flatbush/blob/main/bench.js">Flatbush</a>
 * uses, and run against spatial indexes.
 */
public class FlatbushPerfTest extends PerformanceTestCase {
  private static final int NUM_ITEMS = 1_000_000;
  private static final int NUM_QUERIES = 1_000;
  private Envelope[] items;
  private Envelope[] queries;
  private HPRtree hprtree;
  private STRtree strtree;

  public static void main(String[] args) {
    PerformanceTestRunner.run(FlatbushPerfTest.class);
  }

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
    items = new Envelope[NUM_ITEMS];

    for (int i = 0; i < NUM_ITEMS; i++) {
      items[i] = randomBox(random, 1);
    }

    // warmup the jvm by building once and running queries
    warmupQueries(createIndex(HPRtree::new, HPRtree::build));
    warmupQueries(createIndex(STRtree::new, STRtree::build));

    Stopwatch sw = new Stopwatch();
    hprtree = createIndex(HPRtree::new, HPRtree::build);
    System.out.println("HPRTree Build time = " + sw.getTimeString());

    sw = new Stopwatch();
    strtree = createIndex(STRtree::new, STRtree::build);
    System.out.println("STRTree Build time = " + sw.getTimeString());
  }
  
  private <T extends SpatialIndex> T createIndex(Supplier<T> supplier, Consumer<T> builder) {
    T index = supplier.get();
    for (Envelope env : items) {
      index.insert(env, env);
    }
    builder.accept(index);
    return index;
  }

  private void warmupQueries(SpatialIndex index) {
    Random random = new Random(0);
    CountItemVisitor visitor = new CountItemVisitor();
    for (int i = 0; i < NUM_QUERIES; i++) {
      index.query(randomBox(random, 1), visitor);
    }
  }

  public void startRun(int size)
  {
    System.out.println("----- Query size: " + size);
    Random random = new Random(0);
    queries = new Envelope[NUM_QUERIES];
    for (int i = 0; i < NUM_QUERIES; i++) {
      queries[i] = randomBox(random, size);
    }
  }
  
  public void runQueriesHPR() {
    CountItemVisitor visitor = new CountItemVisitor();
    for (Envelope box : queries) {
        hprtree.query(box, visitor);
    }
    System.out.println("HPRTree query result items = " + visitor.count);
  }

  public void runQueriesSTR() {
    CountItemVisitor visitor = new CountItemVisitor();
    for (Envelope box : queries) {
      strtree.query(box, visitor);
    }
    System.out.println("STRTree query result items = " + visitor.count);
  }
}
