package org.locationtech.jts.geom.prep;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class PreparedGeometryTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(PreparedGeometryTest.class);
  }
  
  public PreparedGeometryTest(String name) {
    super(name);
  }
  
  public void testEmptyElement() {
    Geometry geomA = read("MULTIPOLYGON (((9 9, 9 1, 1 1, 2 4, 7 7, 9 9)), EMPTY)");
    Geometry geomB = read("MULTIPOLYGON (((7 6, 7 3, 4 3, 7 6)), EMPTY)");
    PreparedGeometry prepA = PreparedGeometryFactory.prepare(geomA);
    assertTrue( prepA.covers(geomB));
    assertTrue( prepA.contains(geomB));
    assertTrue( prepA.intersects(geomB));
  }
}
