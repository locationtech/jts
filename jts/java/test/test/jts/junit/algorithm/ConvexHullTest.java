
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package test.jts.junit.algorithm;

import java.util.Stack;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;


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
    assertEquals(expectedGeometry.toString(), actualGeometry.toString());
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
