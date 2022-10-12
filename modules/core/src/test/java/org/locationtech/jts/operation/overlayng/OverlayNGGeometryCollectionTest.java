package org.locationtech.jts.operation.overlayng;

import junit.textui.TestRunner;

/**
 * Tests supported OverlayNG semantics for GeometryCollection inputs.
 * 
 * Note: currently only "simple" GCs are supported.
 * Simple GCs are ones which can be flattened to a valid Multi-geometry.
 * 
 * @author mdavis
 *
 */
public class OverlayNGGeometryCollectionTest extends OverlayNGTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(OverlayNGGeometryCollectionTest.class);
  }

  public OverlayNGGeometryCollectionTest(String name) { super(name); }
  
  public void testSimpleA_mP() {
    String a = "POLYGON ((0 0, 0 1, 1 1, 0 0))";
    String b = "GEOMETRYCOLLECTION ( MULTIPOINT ((0 0), (99 99)) )";
    checkIntersection(a, b, 
        "POINT (0 0)");
    checkUnion(a, b, 
        "GEOMETRYCOLLECTION (POINT (99 99), POLYGON ((0 0, 0 1, 1 1, 0 0)))");
  }
  
  public void testSimpleP_mP() {
    String a = "POINT(0 0)";
    String b = "GEOMETRYCOLLECTION ( MULTIPOINT ((0 0), (99 99)) )";
    checkIntersection(a, b, 
        "POINT (0 0)");
    checkUnion(a, b, 
        "MULTIPOINT ((0 0), (99 99))");
  }
  
  public void testSimpleP_mL() {
    String a = "POINT(5 5)";
    String b = "GEOMETRYCOLLECTION ( MULTILINESTRING ((1 9, 9 1), (1 1, 9 9)) )";
    checkIntersection(a, b, 
        "POINT (5 5)");
    checkUnion(a, b, 
        "MULTILINESTRING ((1 1, 5 5), (1 9, 5 5), (5 5, 9 1), (5 5, 9 9))");
  }
  
  public void testSimpleP_mA() {
    String a = "POINT(5 5)";
    String b = "GEOMETRYCOLLECTION ( MULTIPOLYGON (((1 1, 1 5, 5 5, 5 1, 1 1)), ((9 9, 9 5, 5 5, 5 9, 9 9))) )";
    checkIntersection(a, b, 
        "POINT (5 5)");
    checkUnion(a, b, 
        "MULTIPOLYGON (((1 1, 1 5, 5 5, 5 1, 1 1)), ((9 9, 9 5, 5 5, 5 9, 9 9)))");
  }
  
  public void testSimpleP_AA() {
    String a = "POINT(5 5)";
    String b = "GEOMETRYCOLLECTION ( POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1)), POLYGON ((9 9, 9 5, 5 5, 5 9, 9 9)) )";
    checkIntersection(a, b, 
        "POINT (5 5)");
    checkUnion(a, b, 
        "MULTIPOLYGON (((1 1, 1 5, 5 5, 5 1, 1 1)), ((9 9, 9 5, 5 5, 5 9, 9 9)))");
  }
  
  public void testSimpleL_AA() {
    String a = "LINESTRING (0 0, 10 10)";
    String b = "GEOMETRYCOLLECTION ( POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1)), POLYGON ((9 9, 9 5, 5 5, 5 9, 9 9)) )";
    checkIntersection(a, b, 
        "MULTILINESTRING ((1 1, 5 5), (5 5, 9 9))");
    checkUnion(a, b, 
        "GEOMETRYCOLLECTION (LINESTRING (0 0, 1 1), LINESTRING (9 9, 10 10), POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1)), POLYGON ((5 5, 5 9, 9 9, 9 5, 5 5)))");
  }
  
  public void testSimpleA_AA() {
    String a = "POLYGON ((2 8, 8 8, 8 2, 2 2, 2 8))";
    String b = "GEOMETRYCOLLECTION ( POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1)), POLYGON ((9 9, 9 5, 5 5, 5 9, 9 9)) )";
    checkIntersection(a, b, 
        "MULTIPOLYGON (((2 2, 2 5, 5 5, 5 2, 2 2)), ((5 5, 5 8, 8 8, 8 5, 5 5)))");
    checkUnion(a, b, 
        "POLYGON ((1 1, 1 5, 2 5, 2 8, 5 8, 5 9, 9 9, 9 5, 8 5, 8 2, 5 2, 5 1, 1 1))");
  }
}
