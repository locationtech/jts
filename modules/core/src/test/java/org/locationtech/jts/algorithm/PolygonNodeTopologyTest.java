package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class PolygonNodeTopologyTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(PolygonNodeTopologyTest.class);
  }

  public PolygonNodeTopologyTest(String name) { super(name); }
  
  public void testNonCrossing() {
    checkCrossing("LINESTRING (500 1000, 1000 1000, 1000 1500)",
        "LINESTRING (1000 500, 1000 1000, 500 1500)");
  }

  public void testNonCrossingQuadrant2() {
    checkNonCrossing("LINESTRING (500 1000, 1000 1000, 1000 1500)",
        "LINESTRING (300 1200, 1000 1000, 500 1500)"); 
  }

  public void testNonCrossingQuadrant4() {
    checkNonCrossing("LINESTRING (500 1000, 1000 1000, 1000 1500)",
        "LINESTRING (1000 500, 1000 1000, 1500 1000)");
  }

  public void testNonCrossingCollinear() {
    checkNonCrossing("LINESTRING (3 1, 5 5, 9 9)",
        "LINESTRING (2 1, 5 5, 9 9)");
  }

  public void testNonCrossingBothCollinear() {
    checkNonCrossing("LINESTRING (3 1, 5 5, 9 9)",
        "LINESTRING (3 1, 5 5, 9 9)");
  }

  public void testInteriorSegment() {
    checkInterior("LINESTRING (5 9, 5 5, 9 5)", 
        "LINESTRING (5 5, 0 0)");
  }
  
  public void testExteriorSegment() {
    checkExterior("LINESTRING (5 9, 5 5, 9 5)", 
        "LINESTRING (5 5, 9 9)");
  }
  //-----------------------------------------------
  
  private void checkCrossing(String wktA, String wktB) {
    checkCrossing(wktA, wktB, true);
  }

  private void checkNonCrossing(String wktA, String wktB) {
    checkCrossing(wktA, wktB, false);
  }

  private void checkCrossing(String wktA, String wktB, boolean isExpected) {
    Coordinate[] a = readPts(wktA);
    Coordinate[] b = readPts(wktB);
    // assert: a[1] = b[1]
    boolean isCrossing = PolygonNodeTopology.isCrossing(a[1], a[0], a[2], b[0], b[2]);
    assertTrue(isCrossing == isExpected);
  }
  
  private void checkInterior(String wktA, String wktB) {
    checkInteriorSegment(wktA, wktB, true);
  }
  
  private void checkExterior(String wktA, String wktB) {
    checkInteriorSegment(wktA, wktB, false);
  }
  
  private void checkInteriorSegment(String wktA, String wktB, boolean isExpected) {
    Coordinate[] a = readPts(wktA);
    Coordinate[] b = readPts(wktB);
    // assert: a[1] = b[1]
    boolean isInterior = PolygonNodeTopology.isInteriorSegment(a[1], a[0], a[2], b[1]);
    assertTrue(isInterior == isExpected);
  }

  private Coordinate[] readPts(String wkt) {
    LineString line = (LineString) read(wkt);
    return line.getCoordinates();
  }
}
