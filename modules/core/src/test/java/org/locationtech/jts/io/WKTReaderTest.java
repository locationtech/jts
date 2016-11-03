
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

package org.locationtech.jts.io;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Test for {@link WKTReader}
 *
 * @version 1.7
 */
public class WKTReaderTest extends TestCase {

  WKTWriter writer = new WKTWriter();
  PrecisionModel precisionModel = new PrecisionModel(1);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public WKTReaderTest(String name) { super(name); }

  public static Test suite() { return new TestSuite(WKTReaderTest.class); }

  public void testReadNaN() throws Exception {
    assertEquals("POINT (10 10)", writer.write(reader.read("POINT (10 10 NaN)")));
    assertEquals("POINT (10 10)", writer.write(reader.read("POINT (10 10 nan)")));
    assertEquals("POINT (10 10)", writer.write(reader.read("POINT (10 10 NAN)")));
}

  public void testReadPoint() throws Exception {
    assertEquals("POINT (10 10)", writer.write(reader.read("POINT (10 10)")));
    assertEquals("POINT EMPTY", writer.write(reader.read("POINT EMPTY")));
}

  public void testReadLineString() throws Exception {
    assertEquals("LINESTRING (10 10, 20 20, 30 40)", writer.write(reader.read("LINESTRING (10 10, 20 20, 30 40)")));
    assertEquals("LINESTRING EMPTY", writer.write(reader.read("LINESTRING EMPTY")));
  }

  public void testReadLinearRing() throws Exception {
      try {
          reader.read("LINEARRING (10 10, 20 20, 30 40, 10 99)");
      }
      catch (IllegalArgumentException e) {
          assertTrue(e.getMessage().indexOf("not form a closed linestring") > -1);
      }
      assertEquals("LINEARRING (10 10, 20 20, 30 40, 10 10)", writer.write(reader.read("LINEARRING (10 10, 20 20, 30 40, 10 10)")));
      assertEquals("LINEARRING EMPTY", writer.write(reader.read("LINEARRING EMPTY")));
  }

  public void testReadPolygon() throws Exception {
      assertEquals("POLYGON ((10 10, 10 20, 20 20, 20 15, 10 10))", writer.write(reader.read("POLYGON ((10 10, 10 20, 20 20, 20 15, 10 10))")));
      assertEquals("POLYGON EMPTY", writer.write(reader.read("POLYGON EMPTY")));
  }

  public void testReadMultiPoint() throws Exception {
    assertEquals("MULTIPOINT ((10 10), (20 20))", writer.write(reader.read("MULTIPOINT ((10 10), (20 20))")));
    assertEquals("MULTIPOINT EMPTY", writer.write(reader.read("MULTIPOINT EMPTY")));
  }

  public void testReadMultiLineString() throws Exception {
    assertEquals("MULTILINESTRING ((10 10, 20 20), (15 15, 30 15))", writer.write(reader.read("MULTILINESTRING ((10 10, 20 20), (15 15, 30 15))")));
    assertEquals("MULTILINESTRING EMPTY", writer.write(reader.read("MULTILINESTRING EMPTY")));
  }

  public void testReadMultiPolygon() throws Exception {
    assertEquals("MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))", writer.write(reader.read("MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))")));
    assertEquals("MULTIPOLYGON EMPTY", writer.write(reader.read("MULTIPOLYGON EMPTY")));
  }

  public void testReadGeometryCollection() throws Exception {
      assertEquals("GEOMETRYCOLLECTION (POINT (10 10), POINT (30 30), LINESTRING (15 15, 20 20))", writer.write(reader.read("GEOMETRYCOLLECTION (POINT (10 10), POINT (30 30), LINESTRING (15 15, 20 20))")));
      assertEquals("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING EMPTY, LINESTRING (15 15, 20 20))", writer.write(reader.read("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING EMPTY, LINESTRING (15 15, 20 20))")));
      assertEquals("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING (10 10, 20 20, 30 40, 10 10), LINESTRING (15 15, 20 20))", writer.write(reader.read("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING (10 10, 20 20, 30 40, 10 10), LINESTRING (15 15, 20 20))")));
      assertEquals("GEOMETRYCOLLECTION EMPTY", writer.write(reader.read("GEOMETRYCOLLECTION EMPTY")));
  }

  public void testReadZ() throws Exception {
      assertEquals(new Coordinate(1, 2, 3), reader.read("POINT(1 2 3)").getCoordinate());
  }

  public void testReadLargeNumbers() throws Exception {
    PrecisionModel precisionModel = new PrecisionModel(1E9);
    GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
    WKTReader reader = new WKTReader(geometryFactory);
    Geometry point1 = reader.read("POINT (123456789.01234567890 10)");
    Point point2 = geometryFactory.createPoint(new Coordinate(123456789.01234567890, 10));
    assertEquals(point1.getCoordinate().x, point2.getCoordinate().x, 1E-7);
    assertEquals(point1.getCoordinate().y, point2.getCoordinate().y, 1E-7);
  }

}
