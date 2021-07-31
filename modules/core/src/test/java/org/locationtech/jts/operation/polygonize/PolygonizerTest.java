/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.polygonize;

import java.util.Collection;
import java.util.List;

import test.jts.GeometryTestCase;

/**
 * @version 1.7
 */
public class PolygonizerTest extends GeometryTestCase {

  public PolygonizerTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(PolygonizerTest.class);
  }

  public void testEmptyInput() {
    checkPolygonize(new String[]{"LINESTRING EMPTY", "LINESTRING EMPTY"},
      new String[]{});
  }

  public void testPolygonWithTouchingHole() {
    checkPolygonize(new String[]{
"LINESTRING (100 180, 20 20, 160 20, 100 180)",
"LINESTRING (100 180, 80 60, 120 60, 100 180)",
    },
    new String[]{
"POLYGON ((100 180, 120 60, 80 60, 100 180))",
"POLYGON ((100 180, 160 20, 20 20, 100 180), (100 180, 80 60, 120 60, 100 180))"
    });
  }

  public void testPolygonWithTouchingHoleAndNotch() {
    checkPolygonize(new String[]{
        "LINESTRING (0 0, 4 0)",
        "LINESTRING (4 0, 5 3)",
"LINESTRING (5 3, 4 6, 6 6, 5 3)",
"LINESTRING (5 3, 6 0)",
"LINESTRING (6 0, 10 0, 5 10, 0 0)",
"LINESTRING (4 0, 6 0)"
    },
    new String[]{
"POLYGON ((5 3, 4 0, 0 0, 5 10, 10 0, 6 0, 5 3), (5 3, 6 6, 4 6, 5 3))",
"POLYGON ((5 3, 4 6, 6 6, 5 3))",
"POLYGON ((4 0, 5 3, 6 0, 4 0))"
    });
  }

  public void testPolygonal1() {
    checkPolygonize(true, new String[]{
        "LINESTRING (100 100, 100 300, 300 300, 300 100, 100 100)",
        "LINESTRING (150 150, 150 250, 250 250, 250 150, 150 150)"
    },
    new String[]{
"POLYGON ((100 100, 100 300, 300 300, 300 100, 100 100), (150 150, 150 250, 250 250, 250 150, 150 150))"
    });
  }

  public void testPolygonal2() {
    checkPolygonize(true, new String[]{
        "LINESTRING (100 100, 100 0, 0 0, 0 100, 100 100)" 
            ,"LINESTRING (10 10, 10 30, 20 30)"
            ,"LINESTRING (20 30, 30 30, 30 20)"
            ,"LINESTRING (30 20, 30 10, 10 10)"
            ,"LINESTRING (40 40, 40 20, 30 20)" 
            ,"LINESTRING (30 20, 20 20, 20 30)" 
            ,"LINESTRING (20 30, 20 40, 40 40))"
    },
    new String[]{
"POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0), (10 10, 30 10, 30 20, 40 20, 40 40, 20 40, 20 30, 10 30, 10 10))", 
"POLYGON ((20 20, 20 30, 30 30, 30 20, 20 20))"
    });
  }

  public void testPolygonal_OuterOnly_1() {
    checkPolygonize(true, new String[] {
        "LINESTRING (10 10, 10 20, 20 20)" 
            ,"LINESTRING (20 20, 20 10)"
            ,"LINESTRING (20 10, 10 10)"
            ,"LINESTRING (20 20, 30 20, 30 10, 20 10)"
    },
    new String[]{
"POLYGON ((20 20, 20 10, 10 10, 10 20, 20 20))"
    });
  }

  public void testPolygonal_OuterOnly_2() {
    checkPolygonize(true, new String[] {
        "LINESTRING (100 400, 200 400, 200 300)" 
            ,"LINESTRING (200 300, 150 300)"
            ,"LINESTRING (150 300, 100 300, 100 400)"
            ,"LINESTRING (200 300, 250 300, 250 200)"
            ,"LINESTRING (250 200, 200 200)"
            ,"LINESTRING (200 200, 150 200, 150 300)"
            ,"LINESTRING (250 200, 300 200, 300 100, 200 100, 200 200)"
    },
    new String[]{
        "POLYGON ((150 300, 100 300, 100 400, 200 400, 200 300, 150 300))"
       ,"POLYGON ((200 200, 250 200, 300 200, 300 100, 200 100, 200 200))"
    });
  }

  String[] LINES_CHECKERBOARD = new String[] {
      "LINESTRING (10 20, 20 20)", 
      "LINESTRING (10 20, 10 30)",
      "LINESTRING (20 10, 10 10, 10 20)", 
      "LINESTRING (10 30, 20 30)", 
      "LINESTRING (10 30, 10 40, 20 40)", 
      "LINESTRING (30 10, 20 10)", 
      "LINESTRING (20 20, 20 10)", 
      "LINESTRING (20 20, 30 20)", 
      "LINESTRING (20 30, 20 20)", 
      "LINESTRING (20 30, 30 30)", 
      "LINESTRING (20 40, 20 30)", 
      "LINESTRING (20 40, 30 40)", 
      "LINESTRING (40 20, 40 10, 30 10)", 
      "LINESTRING (30 20, 30 10)", 
      "LINESTRING (30 20, 40 20)", 
      "LINESTRING (30 30, 30 20)", 
      "LINESTRING (30 30, 40 30)", 
      "LINESTRING (30 40, 30 30)", 
      "LINESTRING (30 40, 40 40, 40 30)", 
      "LINESTRING (40 30, 40 20)"
  };
      
  public void testPolygonal_OuterOnly_Checkerboard() {
    checkPolygonize(true, LINES_CHECKERBOARD,
    new String[]{
        "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))"
        ,"POLYGON ((20 30, 10 30, 10 40, 20 40, 20 30))"
        ,"POLYGON ((30 20, 20 20, 20 30, 30 30, 30 20))"
        ,"POLYGON ((30 10, 30 20, 40 20, 40 10, 30 10))"
        ,"POLYGON ((30 40, 40 40, 40 30, 30 30, 30 40))"
    });
  }

  /**
   * Input is invalid (non-noded), but Polygonizer should still not fail.
   * Output is undefined, however.
   */
  public void testNonNodedWithHoleNotAssignable() {
    checkPolygonizeNoError(    
        new String[]{
        "MULTILINESTRING ((10 90, 30 90, 30 30, 70 30, 70 90, 90 90, 90 10, 10 10, 10 90), (30 90, 70 90, 70 30, 30 30, 30 90))"
    });
  }
  
/*
  public void test2() {
    doTest(new String[]{

"LINESTRING(20 20, 20 100)",
"LINESTRING  (20 100, 20 180, 100 180)",
"LINESTRING  (100 180, 180 180, 180 100)",
"LINESTRING  (180 100, 180 20, 100 20)",
"LINESTRING  (100 20, 20 20)",
"LINESTRING  (100 20, 20 100)",
"LINESTRING  (20 100, 100 180)",
"LINESTRING  (100 180, 180 100)",
"LINESTRING  (180 100, 100 20)"
    },
      new String[]{});
  }
*/

  private void checkPolygonize(String[] inputWKT, String[] expectedWKT) {
    checkPolygonize(false, inputWKT, expectedWKT);
  }

  private void checkPolygonize(boolean extractOnlyPolygonal, String[] inputWKT, String[] expectedWKT) {
    Polygonizer polygonizer = new Polygonizer(extractOnlyPolygonal);
    polygonizer.add(readList(inputWKT));
    List expected = readList(expectedWKT);
    Collection actual = polygonizer.getPolygons();
    checkEqual(expected, actual);
  }

  private void checkPolygonizeNoError(String[] inputWKT) {
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(readList(inputWKT));
    try {
      Collection actual = polygonizer.getPolygons();
    }
    catch (Throwable ex) {
      ex.printStackTrace();
      fail("Polygonizer threw an unexpected error");
    }
  }
}
