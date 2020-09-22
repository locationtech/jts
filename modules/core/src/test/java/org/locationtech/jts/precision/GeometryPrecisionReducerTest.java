
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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;


/**
 * @version 1.12
 */
public class GeometryPrecisionReducerTest
    extends GeometryTestCase
{
  private PrecisionModel pmFloat = new PrecisionModel();
  private PrecisionModel pmFixed1 = new PrecisionModel(1);
  private GeometryPrecisionReducer reducer = new GeometryPrecisionReducer(pmFixed1);
  private GeometryPrecisionReducer reducerKeepCollapse
  = new GeometryPrecisionReducer(pmFixed1);

  private GeometryFactory gfFloat = new GeometryFactory(pmFloat, 0);
  WKTReader reader = new WKTReader(gfFloat);

  public static void main(String args[]) {
    TestRunner.run(GeometryPrecisionReducerTest.class);
  }

  public GeometryPrecisionReducerTest(String name)
  {
    super(name);
    reducerKeepCollapse.setRemoveCollapsedComponents(false);
  }

  public void testSquare()
      throws Exception
  {
    checkReduceSameFactory("POLYGON (( 0 0, 0 1.4, 1.4 1.4, 1.4 0, 0 0 ))",
        "POLYGON (( 0 0, 0 1, 1 1, 1 0, 0 0 ))");
  }
  public void testTinySquareCollapse()
      throws Exception
  {
    checkReduceSameFactory("POLYGON (( 0 0, 0 .4, .4 .4, .4 0, 0 0 ))",
        "POLYGON EMPTY");
  }
  
  public void testSquareCollapse()
      throws Exception
  {
    checkReduceSameFactory("POLYGON (( 0 0, 0 1.4, .4 .4, .4 0, 0 0 ))",
        "POLYGON EMPTY");
  }
  
  public void testSquareKeepCollapse()
      throws Exception
  {
    checkReduceSameFactory("POLYGON (( 0 0, 0 1.4, .4 .4, .4 0, 0 0 ))",
        "POLYGON EMPTY");
  }
  
  public void testLine()
      throws Exception
  {
    checkReduceExactSameFactory("LINESTRING ( 0 0, 0 1.4 )",
        "LINESTRING (0 0, 0 1)");
  }
  
  public void testLineNotNoded()
      throws Exception
  {
    checkReduceExactSameFactory("LINESTRING(1 1, 3 3, 9 9, 5.1 5, 2.1 2)",
        "LINESTRING(1 1, 3 3, 9 9, 5 5, 2 2)");
  }
  
  public void testLineRemoveCollapse()
      throws Exception
  {
    checkReduceExactSameFactory("LINESTRING ( 0 0, 0 .4 )",
        "LINESTRING EMPTY");
  }
  
  public void testLineKeepCollapse()
      throws Exception
  {
    checkReduceExactSameFactory(reducerKeepCollapse,
        "LINESTRING ( 0 0, 0 .4 )",
        "LINESTRING ( 0 0, 0 0 )");
  }

  public void testPoint()
      throws Exception
  {
    checkReduceExactSameFactory("POINT(1.1 4.9)",
        "POINT(1 5)");
  }

  public void testMultiPoint()
      throws Exception
  {
    checkReduceExactSameFactory("MULTIPOINT( (1.1 4.9),(1.2 4.8), (3.3 6.6))",
        "MULTIPOINT((1 5), (1 5), (3 7))");
  }

  public void testPolgonWithCollapsedLine() throws Exception {
    checkReduceSameFactory("POLYGON ((10 10, 100 100, 200 10.1, 300 10, 10 10))",
        "POLYGON ((10 10, 100 100, 200 10, 10 10))");
	}
  
  public void testPolgonWithCollapsedLinePointwise() throws Exception {
		Geometry g  = reader.read("POLYGON ((10 10, 100 100, 200 10.1, 300 10, 10 10))");
		Geometry g2 = reader.read("POLYGON ((10 10, 100 100, 200 10,   300 10, 10 10))");
		Geometry gReduce = GeometryPrecisionReducer.reducePointwise(g, pmFixed1);
    assertEqualsExactAndHasSameFactory(gReduce, g2);
	}
  
  public void testPolgonWithCollapsedPoint() throws Exception {
    checkReduceSameFactory("POLYGON ((10 10, 100 100, 200 10.1, 300 100, 400 10, 10 10))",
        "MULTIPOLYGON (((10 10, 100 100, 200 10, 10 10)), ((200 10, 300 100, 400 10, 200 10)))");
	}

  public void testPolgonWithCollapsedPointPointwise() throws Exception {
		Geometry g  = reader.read("POLYGON ((10 10, 100 100, 200 10.1, 300 100, 400 10, 10 10))");
		Geometry g2 = reader.read("POLYGON ((10 10, 100 100, 200 10,   300 100, 400 10, 10 10))");
		Geometry gReduce = GeometryPrecisionReducer.reducePointwise(g, pmFixed1);
    assertEqualsExactAndHasSameFactory(gReduce, g2);
	}

  private void assertEqualsExactAndHasSameFactory(Geometry a, Geometry b)
  {
    assertTrue(a.equalsExact(b));
    assertTrue(a.getFactory() == b.getFactory());
  }

  private void checkReduceExactSameFactory(String wkt, String wktExpected) {
    checkReduceExactSameFactory(reducer, wkt, wktExpected);
  }
  
  private void checkReduceExactSameFactory(GeometryPrecisionReducer reducer, 
      String wkt,
      String wktExpected) {
    Geometry g = read(wkt);
    Geometry expected = read(wktExpected);
    Geometry actual = reducer.reduce(g);
    assertTrue(actual.equalsExact(expected));
    assertTrue(expected.getFactory() == expected.getFactory());
  }
  
  private void checkReduceSameFactory( 
      String wkt,
      String wktExpected) {
    Geometry g = read(wkt);
    Geometry expected = read(wktExpected);
    Geometry actual = reducer.reduce(g);
    checkEqual(expected, actual);
    assertTrue(expected.getFactory() == expected.getFactory());
  }
}
