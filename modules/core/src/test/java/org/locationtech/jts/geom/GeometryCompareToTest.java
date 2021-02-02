package org.locationtech.jts.geom;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;
import test.jts.GeometryTestData;

public class GeometryCompareToTest extends GeometryTestCase{
  public static void main(String args[]) {
    TestRunner.run(GeometryCompareToTest.class);
  }

  public GeometryCompareToTest(String name) { super(name); }
  
  public void testPoints() {
    checkCompareTo(-1, "POINT (0 0)", "POINT (1 0)");
    checkCompareTo(-1, "POINT (0 0)", "POINT (0 1)");
    checkCompareTo(1, "POINT (1 0)", "POINT (0 1)");
  }

  public void testLines() {
    checkCompareTo(-1, 
        "LINESTRING ( 0 0, 1 1, 0 1)",
        "LINESTRING ( 0 0, 1 1, 0 2)");
  }

  public void testPolygonToPolygonWithHole() {
    checkCompareTo(-1, GeometryTestData.WKT_POLY, GeometryTestData.WKT_POLY_HOLE);
  }

  public void testEqual() {
    checkCompareTo(0, GeometryTestData.WKT_POINT,GeometryTestData.WKT_POINT);
    checkCompareTo(0, GeometryTestData.WKT_LINESTRING,GeometryTestData.WKT_LINESTRING);
    checkCompareTo(0, GeometryTestData.WKT_POLY,GeometryTestData.WKT_POLY);
    checkCompareTo(0, GeometryTestData.WKT_POLY_HOLE,GeometryTestData.WKT_POLY_HOLE);
  }

  public void testOrdering() {
    checkCompareTo(-1, GeometryTestData.WKT_POINT,GeometryTestData.WKT_MULTIPOINT);
    checkCompareTo(-1, GeometryTestData.WKT_MULTIPOINT,GeometryTestData.WKT_LINESTRING);
    checkCompareTo(-1, GeometryTestData.WKT_LINESTRING,GeometryTestData.WKT_LINEARRING);
    checkCompareTo(-1, GeometryTestData.WKT_LINEARRING,GeometryTestData.WKT_MULTILINESTRING);
    checkCompareTo(-1, GeometryTestData.WKT_MULTILINESTRING,GeometryTestData.WKT_POLY);
    checkCompareTo(-1, GeometryTestData.WKT_POLY,GeometryTestData.WKT_MULTIPOLYGON);    
    checkCompareTo(-1, GeometryTestData.WKT_MULTIPOLYGON,GeometryTestData.WKT_GC);    
  }
  private void checkCompareTo(int compExpected, String wkt1, String wkt2 ) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    int comp = g1.compareTo(g2);
    assertEquals(compExpected, comp);
  }
}
