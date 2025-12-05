package org.locationtech.jts.io;

import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class WKTReadWriteTest extends TestCase {

  // We deliberately chose a coordinate sequence factory that can handle 4 dimensions
  private final CoordinateSequenceFactory csFactory =
          PackedCoordinateSequenceFactory.DOUBLE_FACTORY;
  private final GeometryFactory geometryFactory =
          new GeometryFactory(csFactory);
  private final WKTReader reader =
          new WKTReader(geometryFactory);

  private final WKTWriter writer =
          new WKTWriter(4);

  public static void main(String[] args) {
    TestRunner.run(new TestSuite(WKTReadWriteTest.class));
  }

  public WKTReadWriteTest(String name) {
    super(name);
    writer.setOutputOrdinates(Ordinate.createXY());
  }

  public void testReadNaN() throws Exception {

    final String pt = "POINT (10 10)";
    assertEquals(pt, writer.write(reader.read("POINT (10 10 NaN)")));
    assertEquals(pt, writer.write(reader.read("POINT (10 10 nan)")));
    assertEquals(pt, writer.write(reader.read("POINT (10 10 NAN)")));
  }

  public void testReadInf() throws Exception {
    final String pt = "POINT (Inf -Inf)";
    assertEquals(pt, writer.write(reader.read("POINT (Inf -Inf)")));
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
      assertTrue(e.getMessage().contains("not form a closed linestring"));
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
    assertEquals("MULTIPOINT (EMPTY, EMPTY)", writer.write(reader.read("MULTIPOINT (EMPTY, EMPTY)")));
  }

  public void testReadMultiLineString() throws Exception {
    assertEquals("MULTILINESTRING ((10 10, 20 20), (15 15, 30 15))", writer.write(reader.read("MULTILINESTRING ((10 10, 20 20), (15 15, 30 15))")));
    assertEquals("MULTILINESTRING EMPTY", writer.write(reader.read("MULTILINESTRING EMPTY")));
    assertEquals("MULTILINESTRING (EMPTY, EMPTY)", writer.write(reader.read("MULTILINESTRING (EMPTY, EMPTY)")));
  }

  public void testReadMultiPolygon() throws Exception {
    assertEquals("MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))", writer.write(reader.read("MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))")));
    assertEquals("MULTIPOLYGON EMPTY", writer.write(reader.read("MULTIPOLYGON EMPTY")));
    assertEquals("MULTIPOLYGON (EMPTY, EMPTY)", writer.write(reader.read("MULTIPOLYGON (EMPTY, EMPTY)")));
  }

  public void testReadGeometryCollection() throws Exception {
    assertEquals("GEOMETRYCOLLECTION (POINT (10 10), POINT (30 30), LINESTRING (15 15, 20 20))", writer.write(reader.read("GEOMETRYCOLLECTION (POINT (10 10), POINT (30 30), LINESTRING (15 15, 20 20))")));
    assertEquals("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING EMPTY, LINESTRING (15 15, 20 20))", writer.write(reader.read("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING EMPTY, LINESTRING (15 15, 20 20))")));
    assertEquals("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING (10 10, 20 20, 30 40, 10 10), LINESTRING (15 15, 20 20))", writer.write(reader.read("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING (10 10, 20 20, 30 40, 10 10), LINESTRING (15 15, 20 20))")));
    assertEquals("GEOMETRYCOLLECTION EMPTY", writer.write(reader.read("GEOMETRYCOLLECTION EMPTY")));
  }
  
  public void testReadGeometryCollectionEmptyWithElements() throws Exception {
    assertEquals("GEOMETRYCOLLECTION (POINT EMPTY)", writer.write(reader.read("GEOMETRYCOLLECTION ( POINT EMPTY )")));
    assertEquals("GEOMETRYCOLLECTION (POINT EMPTY, LINESTRING EMPTY)", writer.write(reader.read("GEOMETRYCOLLECTION ( POINT EMPTY, LINESTRING EMPTY )")));
  }
}
