
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

package org.locationtech.jts.geom;

import org.locationtech.jts.io.WKTReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Test for com.vividsolutions.jts.geom.impl.LineStringImpl.
 *
 * @version 1.7
 */
public class LineStringImplTest extends TestCase {

  PrecisionModel precisionModel = new PrecisionModel(1000);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public LineStringImplTest(String name) { super(name); }

  public static Test suite() { return new TestSuite(LineStringImplTest.class); }

  public void testIsSimple() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING (0 0, 10 10, 10 0, 0 10, 0 0)");
    assertTrue(! l1.isSimple());
    LineString l2 = (LineString) reader.read("LINESTRING (0 0, 10 10, 10 0, 0 10)");
    assertTrue(! l2.isSimple());
  }

  public void testIsCoordinate() throws Exception {
    LineString l = (LineString) reader.read("LINESTRING (0 0, 10 10, 10 0)");
    assertTrue(l.isCoordinate(new Coordinate(0, 0)));
    assertTrue(! l.isCoordinate(new Coordinate(5, 0)));
  }

  public void testUnclosedLinearRing() {
      try {
      geometryFactory.createLinearRing(new Coordinate[]{
          new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(2, 1)});
      assertTrue(false);
      }
      catch (Exception e) {
          assertTrue(e instanceof IllegalArgumentException);
      }
  }

  public void testEquals1() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    assertTrue(l1.equals(l2));
  }

  public void testEquals2() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(3.333 4.444, 1.111 2.222)");
    assertTrue(l1.equals(l2));
  }

  public void testEquals3() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(3.333 4.443, 1.111 2.222)");
    assertTrue(! l1.equals(l2));
  }

  public void testEquals4() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(3.333 4.4445, 1.111 2.222)");
    assertTrue(! l1.equals(l2));
  }

  public void testEquals5() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(3.333 4.4446, 1.111 2.222)");
    assertTrue(! l1.equals(l2));
  }

  public void testEquals6() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444, 5.555 6.666)");
    LineString l2 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444, 5.555 6.666)");
    assertTrue(l1.equals(l2));
  }

  public void testEquals7() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 5.555 6.666, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444, 5.555 6.666)");
    assertTrue(!l1.equals(l2));
  }

  public void testGetCoordinates() throws Exception {
    LineString l = (LineString) reader.read("LINESTRING(1.111 2.222, 5.555 6.666, 3.333 4.444)");
    Coordinate[] coordinates = l.getCoordinates();
    assertEquals(new Coordinate(5.555, 6.666), coordinates[1]);
  }

  public void testIsClosed() throws Exception {
    LineString l = (LineString) reader.read("LINESTRING EMPTY");
    assertTrue(l.isEmpty());
    assertTrue(! l.isClosed());

    LinearRing r = geometryFactory.createLinearRing((CoordinateSequence)null);
    assertTrue(r.isEmpty());
    assertTrue(r.isClosed());

    MultiLineString m = geometryFactory.createMultiLineString(
          new LineString[] {l, r});
    assertTrue(! m.isClosed());

    MultiLineString m2 = geometryFactory.createMultiLineString(
          new LineString[] {r});
    assertTrue(! m2.isClosed());
  }

  public void testGetGeometryType() throws Exception {
    LineString l = (LineString) reader.read("LINESTRING EMPTY");
    assertEquals("LineString", l.getGeometryType());
  }

  public void testEquals8() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1000), 0));
    MultiLineString l1 = (MultiLineString) reader.read("MULTILINESTRING((1732328800 519578384, 1732026179 519976285, 1731627364 519674014, 1731929984 519276112, 1732328800 519578384))");
    MultiLineString l2 = (MultiLineString) reader.read("MULTILINESTRING((1731627364 519674014, 1731929984 519276112, 1732328800 519578384, 1732026179 519976285, 1731627364 519674014))");
    assertTrue(l1.equals(l2));
  }

  public void testEquals9() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1), 0));
    MultiLineString l1 = (MultiLineString) reader.read("MULTILINESTRING((1732328800 519578384, 1732026179 519976285, 1731627364 519674014, 1731929984 519276112, 1732328800 519578384))");
    MultiLineString l2 = (MultiLineString) reader.read("MULTILINESTRING((1731627364 519674014, 1731929984 519276112, 1732328800 519578384, 1732026179 519976285, 1731627364 519674014))");
    assertTrue(l1.equals(l2));
  }

  public void testEquals10() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1), 0));
    Geometry l1 = reader.read("POLYGON((1732328800 519578384, 1732026179 519976285, 1731627364 519674014, 1731929984 519276112, 1732328800 519578384))");
    Geometry l2 = reader.read("POLYGON((1731627364 519674014, 1731929984 519276112, 1732328800 519578384, 1732026179 519976285, 1731627364 519674014))");
    l1.normalize();
    l2.normalize();
    assertTrue(l1.equalsExact(l2));
  }

  public void testFiveZeros() {
    LineString ls = new GeometryFactory().createLineString(new Coordinate[]{
              new Coordinate(0, 0),
              new Coordinate(0, 0),
              new Coordinate(0, 0),
              new Coordinate(0, 0),
              new Coordinate(0, 0)});
    assertTrue(ls.isClosed());
  }

  public void testLinearRingConstructor() throws Exception {
    try {
      LinearRing ring =
        new GeometryFactory().createLinearRing(
          new Coordinate[] {
            new Coordinate(0, 0),
            new Coordinate(10, 10),
            new Coordinate(0, 0)});
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

}
