package org.locationtech.jts.algorithm.hull;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class PolygonConcaveHullTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(PolygonConcaveHullTest.class);
  }

  public PolygonConcaveHullTest(String name) { super(name); }
  
  public void testSimple() {
    checkHull("POLYGON ((30 90, 10 40, 40 10, 70 10, 90 30, 80 80, 70 40, 30 40, 50 50, 60 70, 30 90))", 
        0, "POLYGON ((10 40, 30 90, 80 80, 90 30, 70 10, 40 10, 10 40))");
  }

  public void testZGore() {
    checkHull("POLYGON ((10 90, 40 60, 20 40, 40 20, 70 50, 40 30, 30 40, 59.5 69, 60 70, 50 90, 90 90, 90 10, 10 10, 10 90))", 
        0.7, "POLYGON ((10 10, 10 90, 40 60, 20 40, 30 40, 60 70, 50 90, 90 90, 90 10, 10 10))");
  }

  public void testFlat() {
    checkHull("POLYGON ((10 10, 10 90, 90 90, 90 50, 90 10, 50 10, 10 10))", 
        0.7, "POLYGON ((10 10, 10 90, 90 90, 90 10, 10 10))");
  }

  private void checkHull(String wkt, double vertexCountFraction, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = PolygonConcaveHull.hull(geom, vertexCountFraction);
    //System.out.println(actual);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
}
