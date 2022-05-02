package org.locationtech.jts.algorithm.hull;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class ConstrainedConcaveHullTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(ConstrainedConcaveHullTest.class);
  }

  public ConstrainedConcaveHullTest(String name) { super(name); }
  
  public void testSimple() {
    checkHull("MULTIPOLYGON (((100 200, 100 300, 150 250, 200 300, 200 200, 100 200)), ((100 100, 200 100, 150 50, 100 100)))", 
       70, "POLYGON ((100 100, 100 200, 200 200, 200 100, 100 100))" );
  }

  private void checkHull(String wkt, double threshold, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConstrainedConcaveHull.hull(geom, threshold);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
}
