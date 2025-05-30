package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class CoverageUnionTest extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(CoverageUnionTest.class);
  }
  
  public CoverageUnionTest(String name) {
    super(name);
  }

  public void testPolygonsSimple( ) {
    checkUnion("MULTIPOLYGON (((5 5, 1 5, 5 1, 5 5)), ((5 9, 1 5, 5 5, 5 9)), ((9 5, 5 5, 5 9, 9 5)), ((9 5, 5 1, 5 5, 9 5)))",
        "POLYGON ((1 5, 5 9, 9 5, 5 1, 1 5))");
  }

  public void testPolygonsConcentricDonuts( ) {
    checkUnion("MULTIPOLYGON (((1 9, 9 9, 9 1, 1 1, 1 9), (2 8, 8 8, 8 2, 2 2, 2 8)), ((3 7, 7 7, 7 3, 3 3, 3 7), (4 6, 6 6, 6 4, 4 4, 4 6)))",
        "MULTIPOLYGON (((9 1, 1 1, 1 9, 9 9, 9 1), (8 8, 2 8, 2 2, 8 2, 8 8)), ((7 7, 7 3, 3 3, 3 7, 7 7), (4 4, 6 4, 6 6, 4 6, 4 4)))");
  }

  public void testPolygonsConcentricHalfDonuts( ) {
    checkUnion("MULTIPOLYGON (((6 9, 1 9, 1 1, 6 1, 6 2, 2 2, 2 8, 6 8, 6 9)), ((6 9, 9 9, 9 1, 6 1, 6 2, 8 2, 8 8, 6 8, 6 9)), ((5 7, 3 7, 3 3, 5 3, 5 4, 4 4, 4 6, 5 6, 5 7)), ((5 4, 5 3, 7 3, 7 7, 5 7, 5 6, 6 6, 6 4, 5 4)))",
        "MULTIPOLYGON (((1 9, 6 9, 9 9, 9 1, 6 1, 1 1, 1 9), (2 8, 2 2, 6 2, 8 2, 8 8, 6 8, 2 8)), ((5 3, 3 3, 3 7, 5 7, 7 7, 7 3, 5 3), (5 4, 6 4, 6 6, 5 6, 4 6, 4 4, 5 4)))");
  }

  public void testHoleTouchingSide() {
    checkUnion(
        "GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 6, 2 6, 1 9)), POLYGON ((1 1, 1 9, 2 6, 5 3, 9 6, 9 1, 1 1)))",
        "POLYGON ((9 6, 9 1, 1 1, 1 9, 9 9, 9 6), (9 6, 2 6, 5 3, 9 6))"
            );
  }
  
  public void testHolesTouchingSide() {
    checkUnion(
        "GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 6, 5 7, 2 6, 1 9)), POLYGON ((1 1, 1 9, 2 6, 4 3, 5 7, 7 3, 9 6, 9 1, 1 1)))",
        "POLYGON ((9 9, 9 6, 9 1, 1 1, 1 9, 9 9), (5 7, 7 3, 9 6, 5 7), (2 6, 4 3, 5 7, 2 6))"
            );
  }
  
  public void testHolesTouching() {
    checkUnion(
        "GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 6, 7 7, 5 7, 2 6, 1 9)), POLYGON ((1 1, 1 9, 2 6, 4 3, 5 7, 7 3, 7 7, 9 6, 9 1, 1 1)))",
        "POLYGON ((9 9, 9 6, 9 1, 1 1, 1 9, 9 9), (5 7, 7 3, 7 7, 5 7), (2 6, 4 3, 5 7, 2 6))"
            );
  }

  public void testPolygonsFormingHole( ) {
    checkUnion("MULTIPOLYGON (((1 1, 4 3, 5 6, 5 9, 1 1)), ((1 1, 9 1, 6 3, 4 3, 1 1)), ((9 1, 5 9, 5 6, 6 3, 9 1)))",
        "POLYGON ((9 1, 1 1, 5 9, 9 1), (6 3, 5 6, 4 3, 6 3))");
  }

  public void testPolygonsSquareGrid( ) {
    checkUnion("MULTIPOLYGON (((0 0, 0 25, 25 25, 25 0, 0 0)), ((0 25, 0 50, 25 50, 25 25, 0 25)), ((0 50, 0 75, 25 75, 25 50, 0 50)), ((0 75, 0 100, 25 100, 25 75, 0 75)), ((25 0, 25 25, 50 25, 50 0, 25 0)), ((25 25, 25 50, 50 50, 50 25, 25 25)), ((25 50, 25 75, 50 75, 50 50, 25 50)), ((25 75, 25 100, 50 100, 50 75, 25 75)), ((50 0, 50 25, 75 25, 75 0, 50 0)), ((50 25, 50 50, 75 50, 75 25, 50 25)), ((50 50, 50 75, 75 75, 75 50, 50 50)), ((50 75, 50 100, 75 100, 75 75, 50 75)), ((75 0, 75 25, 100 25, 100 0, 75 0)), ((75 25, 75 50, 100 50, 100 25, 75 25)), ((75 50, 75 75, 100 75, 100 50, 75 50)), ((75 75, 75 100, 100 100, 100 75, 75 75)))",
        "POLYGON ((0 25, 0 50, 0 75, 0 100, 25 100, 50 100, 75 100, 100 100, 100 75, 100 50, 100 25, 100 0, 75 0, 50 0, 25 0, 0 0, 0 25))");
  }

  public void testPolygonsNested( ) {
    checkUnion("GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (3 7, 3 3, 7 3, 7 7, 3 7)), POLYGON ((3 7, 7 7, 7 3, 3 3, 3 7)))",
        "POLYGON ((1 1, 1 9, 9 9, 9 1, 1 1))");
  }

  //------------------------------------------------------------
  
  /**
   * Sequential lines are still noded
   */
  public void testLinesSequential( ) {
    checkUnion("MULTILINESTRING ((1 1, 5 1), (9 1, 5 1))",
        "MULTILINESTRING ((1 1, 5 1), (5 1, 9 1))");
  }

  /**
   * Overlapping lines are noded with common portions merged 
   */
  public void testLinesOverlapping( ) {
    checkUnion("MULTILINESTRING ((1 1, 2 1, 3 1), (4 1, 3 1, 2 1))",
        "MULTILINESTRING ((1 1, 2 1), (2 1, 3 1), (3 1, 4 1))");
  }

  /**
   * A network of lines is dissolved noded at degree > 2 vertices
   */
  public void testLinesNetwork( ) {
    checkUnion("MULTILINESTRING ((1 9, 3.1 8, 5 7, 7 8, 9 9), (5 7, 5 3, 4 3, 2 3), (9 5, 7 4, 5 3, 8 1))",
        "MULTILINESTRING ((1 9, 3.1 8), (2 3, 4 3), (3.1 8, 5 7), (4 3, 5 3), (5 3, 5 7), (5 3, 7 4), (5 3, 8 1), (5 7, 7 8), (7 4, 9 5), (7 8, 9 9))");
  }

  //=======================================================
  
  private void checkUnion(String wkt, String wktExpected) {
    Geometry coverage = read(wkt);
    Geometry expected = read(wktExpected);
    Geometry result = CoverageUnion.union(coverage);
    checkEqual(expected, result);
  }
}
