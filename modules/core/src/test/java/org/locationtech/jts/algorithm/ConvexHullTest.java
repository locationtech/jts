
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

package org.locationtech.jts.algorithm;

import java.util.Stack;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;



/**
 * Test for {@link ConvexHull}.
 *
 * @version 1.7
 */
public class ConvexHullTest extends TestCase {

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

  public void test1() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1), 0));
    LineString lineString = (LineString) reader.read("LINESTRING (30 220, 240 220, 240 220)");
    LineString convexHull = (LineString) reader.read("LINESTRING (30 220, 240 220)");
    assertTrue(convexHull.equalsExact(lineString.convexHull()));
  }

  public void test2() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1), 0));
    Geometry geometry = reader.read("MULTIPOINT (130 240, 130 240, 130 240, 570 240, 570 240, 570 240, 650 240)");
    LineString convexHull = (LineString) reader.read("LINESTRING (130 240, 650 240)");
    assertTrue(convexHull.equalsExact(geometry.convexHull()));
  }

  public void test3() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1), 0));
    Geometry geometry = reader.read("MULTIPOINT (0 0, 0 0, 10 0)");
    LineString convexHull = (LineString) reader.read("LINESTRING (0 0, 10 0)");
    assertTrue(convexHull.equalsExact(geometry.convexHull()));
  }

  public void test4() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1), 0));
    Geometry geometry = reader.read("MULTIPOINT (0 0, 10 0, 10 0)");
    LineString convexHull = (LineString) reader.read("LINESTRING (0 0, 10 0)");
    assertTrue(convexHull.equalsExact(geometry.convexHull()));
  }

  public void test5() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1), 0));
    Geometry geometry = reader.read("MULTIPOINT (0 0, 5 0, 10 0)");
    LineString convexHull = (LineString) reader.read("LINESTRING (0 0, 10 0)");
    assertTrue(convexHull.equalsExact(geometry.convexHull()));
  }

  public void test6() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1), 0));
    Geometry actualGeometry = reader.read("MULTIPOINT (0 0, 5 1, 10 0)").convexHull();
    Geometry expectedGeometry = reader.read("POLYGON ((0 0, 5 1, 10 0, 0 0))");
    assertEquals(expectedGeometry.toString(), actualGeometry.toString());
  }

  public void testToArray() throws Exception {
    ConvexHullEx convexHull = new ConvexHullEx(geometryFactory.createGeometryCollection(null));
    Stack stack = new Stack();
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

  public void test7() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1), 0));
    Geometry geometry = reader.read("MULTIPOINT (0 0, 0 0, 5 0, 5 0, 10 0, 10 0)");
    LineString convexHull = (LineString) reader.read("LINESTRING (0 0, 10 0)");
    assertTrue(convexHull.equalsExact(geometry.convexHull()));
  }



}
