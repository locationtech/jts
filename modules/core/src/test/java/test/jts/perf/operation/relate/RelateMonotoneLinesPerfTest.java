/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.operation.relate;

import java.io.PrintStream;

import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.relate.RelateOp;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;
import test.jts.util.IOUtil;


/**
 * Tests the performance of {@link RelateOp} (via {@link Geometry#intersects(Geometry)}
 * on monotone linestrings, to confirm that the Monotone Chain comparison logic
 * is working as expected.
 * (In particular, Monotone Chains can be tested for intersections very efficiently, 
 * since the monotone property allows subchain envelopes to be computed dynamically,
 * and thus binary search can be used to determine if two monotone chains intersect).
 * This should result in roughly linear performance for testing intersection of 
 * chains (since the construction of the chain dominates the computation).
 * This test demonstrates that this occurs in practice.
 * 
 * @author mdavis
 *
 */
public class RelateMonotoneLinesPerfTest  extends PerformanceTestCase 
{
  private static final int DENSIFY_FACTOR = 1000;

  public static void main(String args[]) {
    PerformanceTestRunner.run(RelateMonotoneLinesPerfTest.class);
  }

  public RelateMonotoneLinesPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 2, 4, 8, 16, 32, 64, 128, 256, 512 });
    setRunIterations(1);
  }

  LineString line1;
  LineString line2;
  
  public void startRun(int runSize) {
    int nVertices = runSize * DENSIFY_FACTOR;
    line1 = createLine("LINESTRING (0 0, 100 100)", nVertices);
    line2 = createLine("LINESTRING (0 1, 100 99)", nVertices );
    
    // force compilation of intersects code
    line1.intersects(line2);
  }
  
  private LineString createLine(String wkt, int nVertices) {
    double distanceTolerance = 100.0 / nVertices;
    Geometry line = IOUtil.read(wkt);
    LineString lineDense = (LineString) Densifier.densify(line, distanceTolerance);
    return lineDense;
  }

  public void runIntersects()
  {
    System.out.println("Line size: " + line2.getNumPoints());
    @SuppressWarnings("unused")
    boolean isIntersects = line1.intersects(line2);
  }

  public void tearDown() {
    double[] timeFactor = computeTimeFactors();
    System.out.print("Time factors: ");
    printArray(timeFactor, System.out);
    System.out.println();
  }

  private void printArray(double[] timeFactor, PrintStream out) {
    for (double d : timeFactor) {
      out.print(d + " ");
    }
  }

  private double[] computeTimeFactors() {
    long[] runTime = getRunTime();
    double[] timeFactor = new double[runTime.length - 1];
    for (int i = 0; i < runTime.length - 1; i++) {
      timeFactor[i] = (double) runTime[i + 1] / (double) runTime[i];
    }
    return timeFactor;
  }
  

  
}
