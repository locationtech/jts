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
package org.locationtech.jts.algorithm.match;

import org.locationtech.jts.geom.Geometry;
import test.jts.GeometryTestCase;

public class HausdorffSimilarityMeasureTest extends GeometryTestCase {
  public HausdorffSimilarityMeasureTest(String name) {
    super(name);
  }

  public void testEqualGeometriesReturn1() {
    Geometry g1 = read("POINT(1 1)");
    Geometry g2 = read("POINT(1 1)");
    assertEquals("Point", 1d, new HausdorffSimilarityMeasure().measure(g1, g2));

    g1 = read("LINESTRING(1 1, 2 1)");
    g2 = read("LINESTRING(1 1, 2 1)");
    assertEquals("LineString", 1d, new HausdorffSimilarityMeasure().measure(g1, g2));

    g1 = read("POLYGON((0 0, 0 10, 10 0, 0 0), (1 1, 7.58 1, 1 7.58, 1 1))");
    g2 = read("POLYGON((0 0, 0 10, 10 0, 0 0), (1 1, 7.58 1, 1 7.58, 1 1))");
    assertEquals("POLYGON", 1d, new HausdorffSimilarityMeasure().measure(g1, g2));
  }

  public void testGreaterHausdorffDistanceReturnsPoorerSimilarity()
  {
    Geometry g1 = read("LINESTRING(1 1, 2 1.0, 3 1)");
    Geometry g2 = read("LINESTRING(1 1, 2 1.1, 3 1)");
    Geometry g3 = read("LINESTRING(1 1, 2 1.2, 3 1)");

    SimilarityMeasure sm = new HausdorffSimilarityMeasure();
    double m12 = sm.measure(g1, g2);
    double m13 = sm.measure(g1, g3);

    assertTrue("Greater distance, poorer similarity", m13 < m12);
  }

}
