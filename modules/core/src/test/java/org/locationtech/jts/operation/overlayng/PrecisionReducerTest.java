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
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class PrecisionReducerTest extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(PrecisionReducerTest.class);
  }
  
  public PrecisionReducerTest(String name) {
    super(name);
  }

  public void testPolygonBoxEmpty( ) {
    checkReduce("POLYGON ((1 1.4, 7.3 1.4, 7.3 1.2, 1 1.2, 1 1.4))",
        1, "POLYGON EMPTY");
  }

  public void testPolygonThinEmpty( ) {
    checkReduce("POLYGON ((1 1.4, 3.05 1.4, 3 4.1, 6 5, 3.2 4, 3.2 1.4, 7.3 1.4, 7.3 1.2, 1 1.2, 1 1.4))",
        1, "POLYGON EMPTY");
  }

  public void testPolygonGore( ) {
    checkReduce("POLYGON ((2 1, 9 1, 9 5, 3 5, 9 5.3, 9 9, 2 9, 2 1))",
        1, "POLYGON ((9 1, 2 1, 2 9, 9 9, 9 5, 9 1))");
  }

  public void testPolygonGore2( ) {
    checkReduce("POLYGON ((9 1, 1 1, 1 9, 9 9, 9 5, 5 5.1, 5 4.9, 9 4.9, 9 1))",
        1, "POLYGON ((9 1, 1 1, 1 9, 9 9, 9 5, 9 1))");
  }

  public void testPolygonGoreToHole( ) {
    checkReduce("POLYGON ((9 1, 1 1, 1 9, 9 9, 9 5, 5 5.9, 5 4.9, 9 4.9, 9 1))",
        1, "POLYGON ((9 1, 1 1, 1 9, 9 9, 9 5, 9 1), (9 5, 5 6, 5 5, 9 5))");
  }

  public void testPolygonSpike( ) {
    checkReduce("POLYGON ((1 1, 9 1, 5 1.4, 5 5, 1 5, 1 1))",
        1, "POLYGON ((5 5, 5 1, 1 1, 1 5, 5 5))");
  }

  public void testPolygonNarrowHole( ) {
    checkReduce("POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (2 5, 8 5, 8 5.3, 2 5))",
        1, "POLYGON ((9 1, 1 1, 1 9, 9 9, 9 1))");
  }

  public void testPolygonWideHole( ) {
    checkReduce("POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (2 5, 8 5, 8 5.8, 2 5))",
        1, "POLYGON ((9 1, 1 1, 1 9, 9 9, 9 1), (8 5, 8 6, 2 5, 8 5))");
  }

  public void testMultiPolygonGap( ) {
    checkReduce("MULTIPOLYGON (((1 9, 9.1 9.1, 9 9, 9 4, 1 4.3, 1 9)), ((1 1, 1 4, 9 3.6, 9 1, 1 1)))",
        1, "POLYGON ((9 1, 1 1, 1 4, 1 9, 9 9, 9 4, 9 1))");
  }

  public void testMultiPolygonGapToHole( ) {
    checkReduce("MULTIPOLYGON (((1 9, 9 9, 9.05 4.35, 6 4.35, 4 6, 2.6 4.25, 1 4, 1 9)), ((1 1, 1 4, 9 4, 9 1, 1 1)))",
        1, "POLYGON ((9 1, 1 1, 1 4, 1 9, 9 9, 9 4, 9 1), (6 4, 4 6, 3 4, 6 4))");
  }

  public void testLine( ) {
    checkReduce("LINESTRING(-3 6, 9 1)",
        0.5, "LINESTRING (-2 6, 10 2)");
  }
  
  public void testCollapsedLine( ) {
    checkReduce("LINESTRING(1 1, 1 9, 1.1 1)",
        1, "LINESTRING (1 1, 1 9)");
  }
  
  public void testCollapsedNodedLine( ) {
    checkReduce("LINESTRING(1 1, 3 3, 9 9, 5.1 5, 2.1 2)",
        1, "MULTILINESTRING ((1 1, 2 2), (2 2, 3 3), (3 3, 5 5), (5 5, 9 9))");
  }
  
  private void checkReduce(String wkt, double scaleFactor, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry expected = read(wktExpected);
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    Geometry result = PrecisionReducer.reducePrecision(geom, pm);
    checkEqual(expected, result);
  }
}
