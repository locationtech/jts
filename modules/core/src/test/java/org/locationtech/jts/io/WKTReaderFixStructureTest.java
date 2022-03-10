package org.locationtech.jts.io;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class WKTReaderFixStructureTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(WKTReaderFixStructureTest.class);
  }

  private WKTReader readerFix;
  private WKTReader reader;

  public WKTReaderFixStructureTest(String name) {
    super(name);
    
    reader = new WKTReader();
    readerFix = new WKTReader();
    readerFix.setFixStructure(true);
  }
  
  public void testLineaStringShort() throws ParseException {
    checkFixStructure("LINESTRING (0 0)");
  }
  
  public void testLinearRingUnclosed() throws ParseException {
    checkFixStructure("LINEARRING (0 0, 0 1, 1 0)");
  }
  
  public void testLinearRingShort() throws ParseException {
    checkFixStructure("LINEARRING (0 0, 0 1)");
  }
  
  public void testPolygonShort() throws ParseException {
    checkFixStructure("POLYGON ((0 0))");
  }
  
  public void testPolygonUnclosed() throws ParseException {
    checkFixStructure("POLYGON ((0 0, 0 1, 1 0))");
  }
  
  public void testPolygonUnclosedHole() throws ParseException {
    checkFixStructure("POLYGON ((0 0, 0 10, 10 0, 0 0), (0 0, 1 0, 0 1))");
  }
  
  public void testCollection() throws ParseException {
    checkFixStructure("GEOMETRYCOLLECTION (LINESTRING (0 0), LINEARRING (0 0, 0 1), POLYGON ((0 0, 0 10, 10 0, 0 0), (0 0, 1 0, 0 1)) )");
  }
  
  private void checkFixStructure(String wkt) throws ParseException {
    checkHasBadStructure(wkt);
    checkFixed(wkt);
  }
  
  private void checkFixed(String wkt) throws ParseException {
    // if not fixed will fail with IllegalArgumentException 
    readerFix.read(wkt);
  }
  
  private void checkHasBadStructure(String wkt) throws ParseException {
    try {
      reader.read(wkt);
      fail("Input does not have non-closed rings");
    } catch (IllegalArgumentException e) {
      // ok, do nothing
    }
  }
  
}
