package org.locationtech.jts.operation.valid;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class PolygonNodeTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(PolygonNodeTest.class);
  }

  public PolygonNodeTest(String name) { super(name); }
  
  public void testCrossing() {
    checkValid("LINESTRING (500 1000, 1000 1000, 1000 1500)",
        "LINESTRING (1000 500, 1000 1000, 500 1500)", false);
  }

  public void testValidQuadrant2() {
    checkValid("LINESTRING (500 1000, 1000 1000, 1000 1500)",
        "LINESTRING (300 1200, 1000 1000, 500 1500)"); 
  }

  public void testValidQuadrant4() {
    checkValid("LINESTRING (500 1000, 1000 1000, 1000 1500)",
        "LINESTRING (1000 500, 1000 1000, 1500 1000)");
  }

  private void checkValid(String wktA, String wktB) {
    checkValid(wktA, wktB, true);
  }

  private void checkValid(String wktA, String wktB, boolean isExpectedValid) {
    Coordinate[] a = readPts(wktA);
    Coordinate[] b = readPts(wktB);
    // assert: a[1] = b[1]
    boolean isValid = ! PolygonNode.isCrossing(a[1], a[0], a[2], b[0], b[2]);
    assertTrue(isValid == isExpectedValid);
  }

  private Coordinate[] readPts(String wkt) {
    LineString line = (LineString) read(wkt);
    return line.getCoordinates();
  }
}
