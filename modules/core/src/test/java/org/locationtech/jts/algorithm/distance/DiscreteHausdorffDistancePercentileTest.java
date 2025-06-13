/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.algorithm.distance;

import junit.textui.TestRunner;
import org.locationtech.jts.geom.Geometry;
import test.jts.GeometryTestCase;

public class DiscreteHausdorffDistancePercentileTest
extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(DiscreteHausdorffDistancePercentileTest.class);
  }

  public DiscreteHausdorffDistancePercentileTest(String name) { super(name); }
  
  public void testLinePoints()
  {
    runTest("LINESTRING (0 0, 2 0)", "MULTIPOINT (0 2, 1 0, 2 1)",
            1.0, "LINESTRING (0 0, 0 2)");
  }

  public void testOrientedDistanceWithPercentile()
  {
    String wkt1 = "LINESTRING (0 100, 100 0)";
    String wkt2 = "LINESTRING (0 0, 100 0)";
    String expected0 = "LINESTRING (100 0, 100 0)";
    String expected100 = "LINESTRING (0 0, 0 100)";
    String expected95 = "LINESTRING (5.0 0.0, 5.0 95)";
    String expected70 = "LINESTRING (30 70.0, 30 0.0)";

    double percentile0 = 0;
    double percentile100 = 1;
    double percentile95 = 0.95;
    double percentile70 = 0.70;
    double offset = 0.001;

    runOriented(wkt1, wkt2, percentile0, expected0);
    runOriented(wkt1, wkt2, percentile100, expected100);
    runOriented(wkt1, wkt2, percentile95, offset, expected95);
    runOriented(wkt1, wkt2, percentile70, offset, expected70);
  }

  public void testLinesShowingDiscretenessEffect()
  {
    String wkt1 = "LINESTRING (130 0, 0 0, 0 150)";
    String wkt2 = "LINESTRING (10 10, 10 150, 130 10)";
    double percentile = 0.95;
    runTest(wkt1, wkt2, percentile,"LINESTRING (10 10, 0 0)");
    runTest(wkt1, wkt2, percentile,90.0, "LINESTRING (0 80, 70 80)");
    
    runOriented(wkt1, wkt2, percentile, "LINESTRING (10 10, 0 0)");
    runOriented(wkt1, wkt2, percentile,1.0, "LINESTRING (110.8 32.4, 73 0)");
  }

  public void testSameResultForSmallAmountOfPoints()
  {
    Geometry g1 = read("LINESTRING (1 6, 3 5, 1 4)");
    Geometry g2 = read("LINESTRING (1 9, 9 5, 1 1)");
    double percentile100 = 1.0;
    double percentile95 = 0.95;
    double offset = 2.5;
    double result1 = DiscreteHausdorffDistancePercentile.distance(g1, g2, percentile100, offset);
    double result2 = DiscreteHausdorffDistancePercentile.distance(g1, g2, percentile95, offset);
    assertEquals(result1, result2);
  }

  public void testIgnoreOutliers()
  {
    Geometry g1 = read("LINESTRING (0 2, 9 2, 10 3)");
    Geometry g2 = read("LINESTRING (0 1, 10 1)");
    double percentile = 0.9;
    double result = DiscreteHausdorffDistancePercentile.distance(g1, g2, percentile, 1.0);
    assertEquals(1.0, result);
  }

  private static final double TOLERANCE = 0.001;

  private void runTest(String wkt1, String wkt2, double percentile, String wktExpected)
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);

    Geometry result = DiscreteHausdorffDistancePercentile.distanceLine(g1, g2, percentile);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);

    double resultDistance = DiscreteHausdorffDistance.distance(g1, g2);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }

  private void runTest(String wkt1, String wkt2, double percentile, double densifyOffset, String wktExpected)
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);

    Geometry result = DiscreteHausdorffDistancePercentile.distanceLine(g1, g2, percentile, densifyOffset);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);

    double resultDistance = DiscreteHausdorffDistancePercentile.distance(g1, g2, percentile, densifyOffset);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }

  private void runOriented(String wkt1, String wkt2, double percentile, String wktExpected) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);

    Geometry result = DiscreteHausdorffDistancePercentile.orientedDistanceLine(g1, g2, percentile);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);

    double resultDistance = DiscreteHausdorffDistancePercentile.orientedDistance(g1, g2, percentile);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }

  private void runOriented(String wkt1, String wkt2, double percentile, double densifyOffset, String wktExpected) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);

    Geometry result = DiscreteHausdorffDistancePercentile.orientedDistanceLine(g1, g2, percentile, densifyOffset);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);

    double resultDistance = DiscreteHausdorffDistancePercentile.orientedDistance(g1, g2, percentile, densifyOffset);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }
}
