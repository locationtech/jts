package org.locationtech.jts.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests primarily the API for OverlayNG with floating precision.
 * 
 * @author Martin Davis
 *
 */
public class OverlayNGFloatingNoderTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(OverlayNGFloatingNoderTest.class);
  }

  public OverlayNGFloatingNoderTest(String name) { super(name); }
  
  public void testTriangleIntersection() {
    Geometry a = read("POLYGON ((0 0, 8 0, 8 3, 0 0))");
    Geometry b = read("POLYGON ((0 5, 5 0, 0 0, 0 5))");
    Geometry expected = read("POLYGON ((0 0, 3.6363636363636362 1.3636363636363635, 5 0, 0 0))");
    Geometry actual = intersection(a, b);
    checkEqual(expected, actual);
  }
  
  static Geometry intersection(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, INTERSECTION);
  }
}
