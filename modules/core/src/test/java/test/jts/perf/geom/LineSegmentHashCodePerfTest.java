/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.geom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.LineSegment;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

/**
 * Tests the performance due to the {@link LineSegment#hashCode}.
 * See https://github.com/locationtech/jts/issues/871.
 * The original implementation produced a lot of identical hashcodes;
 * this is being replaced with a better algorithm.
 * 
 *   Timings
 * =============
 * 
 * Grid Size    Original      Improved
 * ---------    --------      --------
 *  50            117 ms         15 ms
 * 100            837 ms         29 ms
 * 200           4890 ms         98 ms
 * 400         103.623 s        354 ms
 * 
 * @author Martin Davis
 *
 */
public class LineSegmentHashCodePerfTest extends PerformanceTestCase
{
  private static final int NUM_ITER = 1;

  public static void main(String[] args) {
    PerformanceTestRunner.run(LineSegmentHashCodePerfTest.class);
  }

  private List<LineSegment> grid;
  
  public LineSegmentHashCodePerfTest(String name) {
    super(name);
    setRunSize(new int[] { 50, 100, 200, 400 });
  }

  @Override
  public void startRun(int size)
  {
    System.out.println("\nRunning with grid size " + size);
    grid = createGrid(size);
  }

  public void runLineCount() {
    //-- don't really care about total since its random
    double total = 0;
    for (int i = 0; i < NUM_ITER; i++) {
      total += sumSegmentWeights(grid);
    }
  }
  
  private double sumSegmentWeights(List<LineSegment> lines) {
    Map<LineSegment, Double> weights = new HashMap<LineSegment, Double>();
    double total = 0;
    //-- store data against LineSegment keys
    for (LineSegment line : lines)
    {
        weights.put(line, Math.random());
        System.out.format("%s - Hash code: %d   original:  %d\n",
            line, line.hashCode(), hashCodeOriginal(line));
    }

    // pull data from the map for all keys
    for (LineSegment line : lines)
    {
      total += weights.get(line);
    }
    return total;
  }

  List<LineSegment> createGrid(int gridSize) {
    List<LineSegment> grid = new ArrayList<LineSegment>();
    for (int gx = 0; gx < gridSize * 10; gx += 10)
    {
        for (int gy = 0; gy < gridSize * 10; gy += 10)
        {
          grid.add(new LineSegment(gx, gy, gx + 10, gy));
          grid.add(new LineSegment(gx, gy, gx, gy + 10));
        }
    }
    return grid;
  }
  
  /**
   * Original LineSegment hashCode implementation.
   * Produces a lot of identical hash codes for this test.
   * 
   * 
   * @param ls
   * @return
   */
  public static int hashCodeOriginal(LineSegment ls) {

    long bits0 = java.lang.Double.doubleToLongBits(ls.p0.x);
    bits0 ^= java.lang.Double.doubleToLongBits( ls.p0.y) * 31;
    int hash0 = (((int) bits0) ^ ((int) (bits0  >> 32)));
    
    long bits1 = java.lang.Double.doubleToLongBits( ls.p1.x);
    bits1 ^= java.lang.Double.doubleToLongBits( ls.p1.y) * 31;
    int hash1 = (((int) bits1) ^ ((int) (bits1  >> 32)));

    // XOR is supposed to be a good way to combine hashcodes
    return hash0 ^ hash1;
  }
}
