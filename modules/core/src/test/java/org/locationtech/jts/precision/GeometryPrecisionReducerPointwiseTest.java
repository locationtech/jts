/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.precision;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;


/**
 * @version 1.12
 */
public class GeometryPrecisionReducerPointwiseTest
    extends GeometryTestCase
{

  public static void main(String args[]) {
    TestRunner.run(GeometryPrecisionReducerPointwiseTest.class);
  }

  public GeometryPrecisionReducerPointwiseTest(String name)
  {
    super(name);
  }
  
  public void testLineWithCollapse() throws Exception {
    checkReducePointwise(
        "LINESTRING (0 0,  0.1 0,  1 0)",
        "LINESTRING (0 0,  0   0,  1 0)");
  }

  public void testLineDuplicatePointsPreserved() throws Exception {
    checkReducePointwise(
        "LINESTRING (0 0,  0.1 0,  0.1 0,  1 0, 1 0)",
        "LINESTRING (0 0,  0   0,  0   0,  1 0, 1 0)");
  }

  public void testLineFullCollapse() throws Exception {
    checkReducePointwise(
        "LINESTRING (0 0,  0.1 0)",
        "LINESTRING (0 0,  0   0)");
  }

  public void testPolygonFullCollapse() throws Exception {
    checkReducePointwise(
        "POLYGON ((0.1 0.3, 0.3 0.3, 0.3 0.1, 0.1 0.1, 0.1 0.3))",
        "POLYGON ((0 0, 0 0, 0 0, 0 0, 0 0))");
  }

  public void testPolygonWithCollapsedLine() throws Exception {
    checkReducePointwise(
        "POLYGON ((10 10, 100 100, 200 10.1, 300 10, 10 10))",
        "POLYGON ((10 10, 100 100, 200 10,   300 10, 10 10))");
  }

  public void testPolygonWithCollapsedPoint() throws Exception {
    checkReducePointwise(
        "POLYGON ((10 10, 100 100, 200 10.1, 300 100, 400 10, 10 10))",
        "POLYGON ((10 10, 100 100, 200 10,   300 100, 400 10, 10 10))");
	}

  //=======================================
  
  private void checkReducePointwise(String wkt, String wktExpected) {
    Geometry g  =        read(wkt);
    Geometry gExpected = read(wktExpected);
    PrecisionModel pm = new PrecisionModel(1);
    Geometry gReduce = GeometryPrecisionReducer.reducePointwise(g, pm);
    assertEqualsExactAndHasSameFactory(gExpected, gReduce);
  }
  
  private void assertEqualsExactAndHasSameFactory(Geometry expected, Geometry actual)
  {
    checkEqual(expected, actual);
    assertTrue("Factories are not the same", expected.getFactory() == actual.getFactory());
  }
}
