package org.locationtech.jts.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.SYMDIFFERENCE;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlayNGStrictModeTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(OverlayNGStrictModeTest.class);
  }

  public OverlayNGStrictModeTest(String name) { super(name); }
  
  public void testPolygonTouchALPIntersection() {
    Geometry a = read("POLYGON ((10 10, 10 30, 30 30, 30 10, 10 10))");
    Geometry b = read("POLYGON ((40 10, 30 10, 35 15, 30 15, 30 20, 35 20, 25 30, 40 30, 40 10))");
    Geometry expected = read("POLYGON ((30 25, 25 30, 30 30, 30 25))");
    Geometry actual = intersection(a, b);
    checkEqual(expected, actual);
  }
  
  public void testPolygonTouchALIntersection() {
    Geometry a = read("POLYGON ((10 30, 60 30, 60 10, 10 10, 10 30))");
    Geometry b = read("POLYGON ((10 50, 60 50, 60 30, 30 30, 10 10, 10 50))");
    Geometry expected = read("POLYGON ((30 30, 10 10, 10 30, 30 30))");
    Geometry actual = intersection(a, b);
    checkEqual(expected, actual);
  }
  
  public void testPolygonTouchLPIntersection() {
    Geometry a = read("POLYGON ((10 10, 10 30, 30 30, 30 10, 10 10))");
    Geometry b = read("POLYGON ((40 25, 30 25, 30 20, 35 20, 30 15, 40 15, 40 25))");
    Geometry expected = read("LINESTRING (30 25, 30 20)");
    Geometry actual = intersection(a, b);
    checkEqual(expected, actual);
  }

  public void testLineTouchLPIntersection() {
    Geometry a = read("LINESTRING (10 10, 20 10, 20 20, 30 10)");
    Geometry b = read("LINESTRING (10 10, 30 10)");
    Geometry expected = read("LINESTRING (10 10, 20 10)");
    Geometry actual = intersection(a, b);
    checkEqual(expected, actual);
  }

  public void testPolygonResultMixedIntersection() {
    Geometry a = read("POLYGON ((10 30, 60 30, 60 10, 10 10, 10 30))");
    Geometry b = read("POLYGON ((10 50, 60 50, 60 30, 30 30, 10 10, 10 50))");
    Geometry expected = read("POLYGON ((30 30, 10 10, 10 30, 30 30))");
    Geometry actual = intersection(a, b);
    checkEqual(expected, actual);
  }
  
  public void testPolygonResultLineIntersection() {
    Geometry a = read("POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))");
    Geometry b = read("POLYGON ((30 20, 30 10, 20 10, 20 20, 30 20))");
    Geometry expected = read("LINESTRING (20 20, 20 10)");
    Geometry actual = intersection(a, b);
    checkEqual(expected, actual);
  }
  
  /**
   * Symmetric Difference is the one exception 
   * to the Strict Mode homogeneous output rule.
   */
  public void testPolygonLineSymDifference() {
    Geometry a = read("POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))");
    Geometry b = read("LINESTRING (15 15, 25 15)");
    Geometry expected = read("GEOMETRYCOLLECTION (POLYGON ((20 20, 20 15, 20 10, 10 10, 10 20, 20 20)), LINESTRING (20 15, 25 15))");
    Geometry actual = symDifference(a, b);
    checkEqual(expected, actual);
  }
  
  public void testPolygonLineUnion() {
    Geometry a = read("POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))");
    Geometry b = read("LINESTRING (15 15, 25 15)");
    Geometry expected = read("GEOMETRYCOLLECTION (POLYGON ((20 20, 20 15, 20 10, 10 10, 10 20, 20 20)), LINESTRING (20 15, 25 15))");
    Geometry actual = union(a, b);
    checkEqual(expected, actual);
  }
  
  /**
   * Check that result does not include collapsed line intersection
   */
  public void testPolygonIntersectionCollapse() {
    Geometry a = read("POLYGON ((1 1, 1 5, 3 5, 3 2, 9 1, 1 1))");
    Geometry b = read("POLYGON ((7 5, 9 5, 9 1, 7 1, 7 5))");
    Geometry expected = read("POLYGON EMPTY");
    Geometry actual = intersection(a, b, 1);
    checkEqual(expected, actual);
  }
  
  public void testPolygonUnionCollapse() {
    Geometry a = read("POLYGON ((1 1, 1 5, 3 5, 3 1.4, 7 1, 1 1))");
    Geometry b = read("POLYGON ((7 5, 9 5, 9 1, 7 1, 7 5))");
    Geometry expected = read("MULTIPOLYGON (((1 1, 1 5, 3 5, 3 1, 1 1)), ((7 1, 7 5, 9 5, 9 1, 7 1)))");
    Geometry actual = union(a, b, 1);
    checkEqual(expected, actual);
  }
  
  static Geometry intersection(Geometry a, Geometry b) {
    return overlay(a, b, INTERSECTION);
  }
  
  static Geometry symDifference(Geometry a, Geometry b) {
    return overlay(a, b, SYMDIFFERENCE);
  }
  
  static Geometry union(Geometry a, Geometry b) {
    return overlay(a, b, UNION);
  }
  
  static Geometry overlay(Geometry a, Geometry b, int opCode) {
    OverlayNG ov = new OverlayNG(a, b, opCode);
    ov.setStrictMode(true);
    return ov.getResult();
  }
  
  static Geometry intersection(Geometry a, Geometry b, double scaleFactor) {
    return overlay(a, b, scaleFactor, INTERSECTION);
  }
  static Geometry union(Geometry a, Geometry b, double scaleFactor) {
    return overlay(a, b, scaleFactor, UNION);
  }
  static Geometry overlay(Geometry a, Geometry b, double scaleFactor, int opCode) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    OverlayNG ov = new OverlayNG(a, b, pm, opCode);
    ov.setStrictMode(true);
    return ov.getResult();
  }
}
