/*
 * Copyright (c) 2021 Felix Obermaier.
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

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import test.jts.GeometryTestCase;

public class DiscreteFrechetDistanceTest extends GeometryTestCase {

  public DiscreteFrechetDistanceTest(String name) {
    super(name);
  }

  @Test
  public void testLineSegments()
  {
    runTest(
      "LINESTRING(0 0, 1 0.0, 2 0.0, 3 0.0, 4 0)",
      "LINESTRING(0 1, 1 1.1, 2 1.2, 3 1.1, 4 1)", 1.2);
  }

  @Test
  public void testOrientation()
  {
    runTest(
      "LINESTRING(0 0, 10 10, 20 15)",
      "LINESTRING(0 1,  8  9, 12 11, 21 15)",2.23606797749979);
  }
  @Test
  public void testFromDHD() {
    runTest(
      "LINESTRING (130 0, 0 0, 0 150)",
      "LINESTRING (10 10, 10 150, 130 10)", 191.049731745428);
  }

  @Test
  public void testFromDHDScaled() {
    runTest(
      "LINESTRING (130 0, 0 0, 0 150)",
      "LINESTRING (10 10, 10 150, 130 10)", new ScaledCartesianDistance(), 1910.49731745428);
  }

  // https://github.com/joaofig/discrete-frechet/blob/master/recursive-vs-linear.ipynb
  public void test2() {
    runTest("LINESTRING(0.2 2.0, 1.5 2.8, 2.3 1.6, 2.9 1.8, 4.1 3.1, 5.6 2.9, 7.2 1.3, 8.2 1.1)",
      "LINESTRING(0.3 1.6, 3.2 3.0, 3.8 1.8, 5.2 3.1, 6.5 2.8, 7.0 0.8, 8.9 0.6)", 1.697056);
  }

  private static final double TOLERANCE = 0.00001;

  private void runTest(String wkt1, String wkt2, double expectedDistance) {
    runTest(wkt1, wkt2, CartesianDistance.getInstance(), expectedDistance);
  }
  private void runTest(String wkt1, String wkt2, DistanceMetric distanceMetric, double expectedDistance)
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);

    double distance0 = DiscreteFrechetDistanceLinear.distance(g1, g2, distanceMetric);
    assertEquals(expectedDistance, distance0, TOLERANCE);

    double distance1 = DiscreteFrechetDistance.distance(g1, g2, distanceMetric);
    assertEquals(expectedDistance, distance1, TOLERANCE);
    double distance2 = DiscreteFrechetDistance.distance(g2, g1, distanceMetric);
    assertEquals(distance1, distance2);


  }

  private class ScaledCartesianDistance implements DistanceMetric {

    @Override
    public double distance(Coordinate p0, Coordinate p1) {
      return 10d * CartesianDistance.getInstance().distance(p0, p1);
    }
  }

}
