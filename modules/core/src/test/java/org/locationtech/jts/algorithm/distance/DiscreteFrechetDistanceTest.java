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

import junit.framework.TestCase;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.util.Assert;
import test.jts.GeometryTestCase;
import test.jts.util.IOUtil;

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

  private static final double TOLERANCE = 0.00001;

  private void runTest(String wkt1, String wkt2, double expectedDistance)
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);

    double distance1 = DiscreteFrechetDistance.distance(g1, g2);
    assertEquals(expectedDistance, distance1, TOLERANCE);
    double distance2 = DiscreteFrechetDistance.distance(g2, g1);
    assertEquals(distance1, distance2);
  }
}
