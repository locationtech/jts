package org.locationtech.jts.awt;

import java.awt.Shape;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class PolygonShapeTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(PolygonShapeTest.class);
  }

  public PolygonShapeTest(String name) { super(name); }
  
  public void testFlatness() {
    Geometry geom = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    ShapeWriter sw = new ShapeWriter();
    Shape shp = sw.toShape(geom);
    
    Geometry geom2 = ShapeReader.read(shp, 0.5, geom.getFactory());
    Geometry geomExpected = read("POLYGON ((100 -200, 200 -200, 200 -100, 100 -100, 100 -200))");
    assertTrue(geomExpected.equalsExact(geom2));
  }
  
  public void testEmptyHole() {
    Geometry geom = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200), EMPTY)");
    ShapeWriter sw = new ShapeWriter();
    Shape shp = sw.toShape(geom);
    
    Geometry geom2 = ShapeReader.read(shp, 0.5, geom.getFactory());
    Geometry geomExpected = read("POLYGON ((100 -200, 200 -200, 200 -100, 100 -100, 100 -200))");
    assertTrue(geomExpected.equalsExact(geom2));
  }
}
