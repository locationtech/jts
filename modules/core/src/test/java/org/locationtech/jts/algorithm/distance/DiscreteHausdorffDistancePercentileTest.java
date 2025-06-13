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

import java.time.Duration;
import java.time.Instant;

public class DiscreteHausdorffDistancePercentileTest
extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(DiscreteHausdorffDistancePercentileTest.class);
  }

  public DiscreteHausdorffDistancePercentileTest(String name) { super(name); }
  
  public void testLinePoints()
  {
    runTestP("LINESTRING (0 0, 2 0)", "MULTIPOINT (0 2, 1 0, 2 1)", 1.0, "LINESTRING (0 0, 0 2)");
  }

  public void testOrientedDistanceWithPercentile()
  {
    String wkt1 = "LINESTRING (0 100, 100 0)";
    String wkt2 = "LINESTRING (100 0, 0 0)";
    String expected0 = "LINESTRING (100 0, 100 0)";
    String expected100 = "LINESTRING (0 0, 0 100)";
    String expected95 = "LINESTRING (5 0, 5 95)";
    String expected70 = "LINESTRING (30 0, 30 70)";

    double percentile0 = 0;
    double percentile100 = 1;
    double percentile95 = 0.95;
    double percentile70 = 0.70;
    double offset = 0.0001;

    runOrientedP(wkt1, wkt2, percentile0, expected0);
    runOrientedP(wkt1, wkt2, percentile100, expected100);
    runOrientedP(wkt1, wkt2, percentile95, offset, expected95);
    runOrientedP(wkt1, wkt2, percentile70, offset, expected70);
  }

  public void testLinesShowingDiscretenessEffect()
  {
    String wkt1 = "LINESTRING (130 0, 0 0, 0 150)";
    String wkt2 = "LINESTRING (10 10, 10 150, 130 10)";
    double percentile = 0.95;
    runTestP(wkt1, wkt2, percentile,"LINESTRING (10 10, 0 0)");
    runTestP(wkt1, wkt2, percentile,90.0, "LINESTRING (0 80, 70 80)");
    
    runOrientedP(wkt1, wkt2, percentile, "LINESTRING (10 10, 0 0)");
    runOrientedP(wkt1, wkt2, percentile,1.0, "LINESTRING (110.8 32.4, 73 0)");
  }

  public void testLineSegments()
  {
    String wkt1 = "LINESTRING (1 6, 3 5, 1 4)";
    String wkt2 = "LINESTRING (1 9, 9 5, 1 1)";
    double percentile = .9;
    double offset = 0.0001;
    runTestP(wkt1, wkt2, percentile, offset, "LINESTRING (3 5, 8 4.5)");
  }

  private static final double TOLERANCE = 0.0001;

  private void runTestP(String wkt1, String wkt2, double percentile, String wktExpected)
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);

    Geometry result = DiscreteHausdorffDistancePercentile.distanceLinePercentile(g1, g2, percentile);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);

    double resultDistance = DiscreteHausdorffDistance.distance(g1, g2);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }

  private void runTestP(String wkt1, String wkt2, double percentile, double densifyOffset, String wktExpected)
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);

    Geometry result = DiscreteHausdorffDistancePercentile.distanceLinePercentile(g1, g2, percentile, densifyOffset);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);

    double resultDistance = DiscreteHausdorffDistancePercentile.distancePercentile(g1, g2, percentile, densifyOffset);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }

  private void runOrientedP(String wkt1, String wkt2, double percentile, String wktExpected) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);

    Geometry result = DiscreteHausdorffDistancePercentile.orientedDistanceLinePercentile(g1, g2, percentile);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);

    double resultDistance = DiscreteHausdorffDistancePercentile.orientedDistancePercentile(g1, g2, percentile);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }

  private void runOrientedP(String wkt1, String wkt2, double percentile, double densifyOffset, String wktExpected) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);

    Geometry result = DiscreteHausdorffDistancePercentile.orientedDistanceLinePercentile(g1, g2, percentile, densifyOffset);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);

    double resultDistance = DiscreteHausdorffDistancePercentile.orientedDistancePercentile(g1, g2, percentile, densifyOffset);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }
}
