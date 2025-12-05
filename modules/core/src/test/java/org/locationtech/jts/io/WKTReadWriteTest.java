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

    checkReadWrite("POINT (NaN NaN)");
    
    final String pt = "POINT (10 10)";
    assertEquals(pt, readWrite("POINT (10 10 NaN)"));
    assertEquals(pt, readWrite("POINT (10 10 nan)"));
    assertEquals(pt, readWrite("POINT (10 10 NAN)"));
  }

  public void testReadInf() {
    checkReadWrite("POINT (Inf -Inf)");
  }

  public void testReadPoint() {
    checkReadWrite("POINT (10 10)");
    checkReadWrite("POINT EMPTY");
  }

  public void testReadLineString() {
    checkReadWrite("LINESTRING (10 10, 20 20, 30 40)");
    checkReadWrite("LINESTRING EMPTY");
  }

  public void testReadLinearRing() {
    checkReadWrite("LINEARRING (10 10, 20 20, 30 40, 10 10)");
    checkReadWrite("LINEARRING EMPTY");
  }

  public void testReadPolygon() {
    checkReadWrite("POLYGON ((10 10, 10 20, 20 20, 20 15, 10 10))");
    checkReadWrite("POLYGON EMPTY");
  }

  public void testReadMultiPoint() {
    checkReadWrite("MULTIPOINT ((10 10), (20 20))");
    checkReadWrite("MULTIPOINT EMPTY");
    checkReadWrite("MULTIPOINT (EMPTY, EMPTY)");
  }

  public void testReadMultiLineString() {
    checkReadWrite("MULTILINESTRING ((10 10, 20 20), (15 15, 30 15))");
    checkReadWrite("MULTILINESTRING EMPTY");
    checkReadWrite("MULTILINESTRING (EMPTY, EMPTY)");
  }

  public void testReadMultiPolygon() {
    checkReadWrite("MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))");
    checkReadWrite("MULTIPOLYGON EMPTY");
    checkReadWrite("MULTIPOLYGON (EMPTY, EMPTY)");
  }

  public void testReadGeometryCollection() {
    checkReadWrite("GEOMETRYCOLLECTION (POINT (10 10), POINT (30 30), LINESTRING (15 15, 20 20))");
    checkReadWrite("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING EMPTY, LINESTRING (15 15, 20 20))");
    checkReadWrite("GEOMETRYCOLLECTION (POINT (10 10), LINEARRING (10 10, 20 20, 30 40, 10 10), LINESTRING (15 15, 20 20))");
    checkReadWrite("GEOMETRYCOLLECTION EMPTY");
  }
  
  public void testReadGeometryCollectionEmptyWithElements() {
    checkReadWrite("GEOMETRYCOLLECTION (POINT EMPTY)");
    checkReadWrite("GEOMETRYCOLLECTION (POINT EMPTY, LINESTRING EMPTY)");
  }
  
  //===============================================
  
  private void checkReadWrite(final String wkt) {
    String wktResult = readWrite(wkt);
    assertEquals(wkt, wktResult);
  }

  private String readWrite(final String wkt) {
    String wktResult = null;
    try {
      return writer.write(reader.read(wkt));
    }
    catch (ParseException ex) {
      fail(ex.getMessage());
    }
    return wktResult;
  }
}
