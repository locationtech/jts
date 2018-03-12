package org.locationtech.jts.operation.relate;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class IntersectsBugTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(IntersectsBugTest.class);
  }

  private GeometryFactory fact = new GeometryFactory();
  private WKTReader rdr = new WKTReader(fact);

  public IntersectsBugTest(String name) {
    super(name);
  }
  
  public void testIntersects() {
    Geometry g1 = read("LINESTRING(0.0 0.0, -10.0 1.2246467991473533E-15)");
    Geometry g2 = read("LINESTRING(-9.999143275740073 -0.13089595571333978, -10.0 1.0535676356486768E-13)");
    boolean isIntersects = g1.intersects(g2);
    assertTrue( isIntersects );
  }
}
