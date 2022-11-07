
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

package org.locationtech.jts.algorithm;

import java.util.Stack;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.jts.GeometryTestCase;



/**
 * Test for {@link ConvexHull}.
 *
 * @version 1.7
 */
public class ConvexHullTest extends GeometryTestCase {

  PrecisionModel precisionModel = new PrecisionModel(1000);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public ConvexHullTest(String name) { super(name); }

  public static Test suite() { return new TestSuite(ConvexHullTest.class); }

  public void testManyIdenticalPoints() throws Exception {
    Coordinate[] pts = new Coordinate[100];
    for (int i = 0; i < 99; i++)
      pts[i] = new Coordinate(0,0);
    pts[99] = new Coordinate(1,1);
    ConvexHull ch = new ConvexHull(pts, geometryFactory);
    Geometry actualGeometry = ch.getConvexHull();
    Geometry expectedGeometry = reader.read("LINESTRING (0 0, 1 1)");
    assertTrue(expectedGeometry.equalsExact(actualGeometry));
  }

  public void testAllIdenticalPoints() throws Exception {
    Coordinate[] pts = new Coordinate[100];
    for (int i = 0; i < 100; i++)
      pts[i] = new Coordinate(0,0);
    ConvexHull ch = new ConvexHull(pts, geometryFactory);
    Geometry actualGeometry = ch.getConvexHull();
    Geometry expectedGeometry = reader.read("POINT (0 0)");
    assertTrue(expectedGeometry.equalsExact(actualGeometry));
  }

  public void testLineCollinear() throws Exception {
    checkConvexHull(
        "LINESTRING (30 220, 240 220, 240 220)",
        "LINESTRING (30 220, 240 220)");
  }

  public void testLineCollinear2() throws Exception {
    checkConvexHull(
        "MULTIPOINT (130 240, 130 240, 130 240, 570 240, 570 240, 570 240, 650 240)",
        "LINESTRING (130 240, 650 240)");
   }

  public void testMultiCollinearEqual12() throws Exception {
    checkConvexHull(
        "MULTIPOINT (0 0, 0 0, 10 0)",
        "LINESTRING (0 0, 10 0)");
  }

  public void testMultiPointCollinearEqual23() throws Exception {
    checkConvexHull(
        "MULTIPOINT (0 0, 10 0, 10 0)",
        "LINESTRING (0 0, 10 0)");
  }

  public void testMultiPointCollinearEqualNone() throws Exception {
    checkConvexHull(
        "MULTIPOINT (0 0, 5 0, 10 0)",
        "LINESTRING (0 0, 10 0)");
  }

  public void testMultiPoint() throws Exception {
    checkConvexHull(
        "MULTIPOINT (0 0, 5 1, 10 0)",
        "POLYGON ((0 0, 5 1, 10 0, 0 0))");
  }

  public void testMultiPointLinear() throws Exception {
    checkConvexHull(
        "MULTIPOINT (0 0, 0 0, 5 0, 5 0, 10 0, 10 0)",
        "LINESTRING (0 0, 10 0)");
  }

  public void testCollinearPoints() throws Exception {
    checkConvexHull("MULTIPOINT ((-0.2 -0.1), (0 -0.1), (0.2 -0.1), (0 -0.1), (-0.2 0.1), (0 0.1), (0.2 0.1), (0 0.1))",
        "POLYGON ((-0.2 -0.1, -0.2 0.1, 0.2 0.1, 0.2 -0.1, -0.2 -0.1))");
  }

  /**
   * See https://trac.osgeo.org/geos/ticket/850
   */
  public void testGEOS_850() {
    checkConvexHull("01040000001100000001010000002bd3a24002bcb0417ff59d2051e25c4101010000003aebcec70a8b3cbfdb123fe713a2e8be0101000000afa0bb8638b770bf7fc1d77d0dda1cbf01010000009519cb944ce070bf1a46cd7df4201dbf010100000079444b4cd1937cbfa6ca29ada6a928bf010100000083323f09e16c7cbfd36d07ee0b8828bf01010000009081b8f066967ebf915fbc9ebe652abf0101000000134cf280633bc1bf37b754972dbe6dbf0101000000ea992c094df585bf1bbabc8a42f332bf0101000000c0a13c7fb31186bf9af7b10cc50b33bf0101000000a0bba15a0a7188bf8fba7870e91735bf01010000000fc8701903db93bf93bdbe93b52241bf01010000007701a73b29cc90bfb770bc3732fe3cbf010100000036fa45b75b8b8cbf1cfca5bf59a238bf0101000000a54e773f7f287ebf910d4621e5062abf01010000004b5b5dc4196f55bfa51f0579717f02bf01010000007e549489513a5fbfa57bacea34f30abf",
        "POLYGON ((-0.1346248988744213 -0.0036307230426677, -0.0019059940589774 -0.0000514030956167, 280756800.63603467 7571780.50964105, -0.1346248988744213 -0.0036307230426677))",
        0.000000000001);
  }

  /**
   * Tests robustness issue in radial sort.
   * See https://github.com/libgeos/geos/issues/722
   */
  public void testCollinearPointsTinyX() {
    checkConvexHull("MULTIPOINT (-0.2 -0.1, 1.38777878e-17 -0.1, 0.2 -0.1, -1.38777878e-17 -0.1, -0.2 0.1, 1.38777878e-17 0.1, 0.2 0.1, -1.38777878e-17 0.1)",
        "POLYGON ((-0.2 -0.1, -0.2 0.1, 0.2 0.1, 0.2 -0.1, -0.2 -0.1))");
  }

  public void testCollinearPointsLessTinyX() {
    checkConvexHull("MULTIPOINT (-0.2 -0.1, 1.38777878e-7 -0.1, 0.2 -0.1, -1.38777878e-7 -0.1, -0.2 0.1, 1.38777878e-7 0.1, 0.2 0.1, -1.38777878e-7 0.1)",
        "POLYGON ((-0.2 -0.1, -0.2 0.1, 0.2 0.1, 0.2 -0.1, -0.2 -0.1))");
  }

  /**
   * Test case fails in GEOS due to incorrect fix to radial sorting.
   * This did not trigger a failure in JTS, probably because the sorting
   * is less strict.
   */
  public void testGEOSSortFailure() {
    checkConvexHull("MULTIPOINT ((140 350), (510 140), (110 140), (250 290), (250 50), (300 370), (450 310), (440 160), (290 280), (220 160), (100 260), (320 230), (200 280), (360 130), (330 210), (380 80), (220 210), (380 310), (260 150), (260 110), (170 130))",
        "POLYGON ((100 260, 140 350, 300 370, 450 310, 510 140, 380 80, 250 50, 110 140, 100 260))");
  }
  
  //==========================================================

  private void checkConvexHull(String wkt, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = geom.convexHull();
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }

  private void checkConvexHull(String wkt, String wktExpected, double tolerance) {
    Geometry geom = read(wkt);
    Geometry actual = geom.convexHull();
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual, tolerance);
  }

  //==========================================================
  
  public void testToArray() throws Exception {
    ConvexHullEx convexHull = new ConvexHullEx(geometryFactory.createGeometryCollection(null));
    Stack<Coordinate> stack = new Stack<Coordinate>();
    stack.push(new Coordinate(0, 0));
    stack.push(new Coordinate(1, 1));
    stack.push(new Coordinate(2, 2));
    Object[] array1 = convexHull.toCoordinateArray(stack);
    assertEquals(3, array1.length);
    assertEquals(new Coordinate(0, 0), array1[0]);
    assertEquals(new Coordinate(1, 1), array1[1]);
    assertEquals(new Coordinate(2, 2), array1[2]);
    assertTrue(!array1[0].equals(array1[1]));
  }

  private static class ConvexHullEx extends ConvexHull {
    public ConvexHullEx(Geometry geometry) {
      super(geometry);
    }
    protected Coordinate[] toCoordinateArray(Stack stack) {
      return super.toCoordinateArray(stack);
    }
  }
}
