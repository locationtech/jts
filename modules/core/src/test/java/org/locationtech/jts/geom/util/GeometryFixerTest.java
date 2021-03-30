/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class GeometryFixerTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(GeometryFixerTest.class);
  }
  
	public GeometryFixerTest(String name) {
		super(name);
	}
	
  public void testPoint() {
    checkFix("POINT (0 0)", "POINT (0 0)");
  }

  public void testPointNaN() {
    checkFix("POINT (0 Nan)", "POINT EMPTY");
  }

  public void testPointEmpty() {
    checkFix("POINT EMPTY", "POINT EMPTY");
  }

  public void testPointPosInf() {
    checkFix( createPoint(0, Double.POSITIVE_INFINITY), "POINT EMPTY");
  }

  public void testPointNegInf() {
    checkFix( createPoint(0, Double.POSITIVE_INFINITY), "POINT EMPTY");
  }

  private Point createPoint(double x, double y) {
    Coordinate p = new Coordinate(x, y);
    Point pt = getGeometryFactory().createPoint(p);
    return pt;
  }
  
  //----------------------------------------

  public void testMultiPointNaN() {
    checkFix("MULTIPOINT ((0 Nan))",
        "MULTIPOINT EMPTY");
  }

  public void testMultiPoint() {
    checkFix("MULTIPOINT ((0 0), (1 1))",
        "MULTIPOINT ((0 0), (1 1))");
  }

  public void testMultiPointWithEmpty() {
    checkFix("MULTIPOINT ((0 0), EMPTY)",
        "MULTIPOINT ((0 0))");
  }

  public void testMultiPointWithMultiEmpty() {
    checkFix("MULTIPOINT (EMPTY, EMPTY)",
        "MULTIPOINT EMPTY");
  }

  //----------------------------------------

  public void testLineStringCollapse() {
    checkFix("LINESTRING (0 0, 1 NaN, 0 0)",
        "LINESTRING EMPTY");
  }

  public void testLineStringCollapseMultipleRepeated() {
    checkFix("LINESTRING (0 0, 0 0, 0 0)",
        "LINESTRING EMPTY");
  }

  public void testLineStringKeepCollapse() {
    checkFixKeepCollapse("LINESTRING (0 0, 0 0, 0 0)",
        "POINT (0 0)");
  }

  public void testLineStringRepeated() {
    checkFix("LINESTRING (0 0, 0 0, 0 0, 0 0, 0 0, 1 1)",
        "LINESTRING (0 0, 1 1)");
  }

  /**
   * Checks that self-crossing are valid, and that entire geometry is copied
   */
  public void testLineStringSelfCross() {
    checkFix("LINESTRING (0 0, 9 9, 9 5, 0 5)",
        "LINESTRING (0 0, 9 9, 9 5, 0 5)");
  }
  
  //----------------------------------------

  public void testMultiLineStringSelfCross() {
    checkFix("MULTILINESTRING ((10 90, 90 10, 90 90), (90 50, 10 50))",
        "MULTILINESTRING ((10 90, 90 10, 90 90), (90 50, 10 50))");
  }
  
  public void testMultiLineStringWithCollapse() {
    checkFix("MULTILINESTRING ((10 10, 90 90), (10 10, 10 10, 10 10))",
        "LINESTRING (10 10, 90 90))");
  }
  
  public void testMultiLineStringKeepCollapse() {
    checkFixKeepCollapse("MULTILINESTRING ((10 10, 90 90), (10 10, 10 10, 10 10))",
        "GEOMETRYCOLLECTION (POINT (10 10), LINESTRING (10 10, 90 90))");
  }
  
  public void testMultiLineStringWithEmpty() {
    checkFix("MULTILINESTRING ((10 10, 90 90), EMPTY)",
        "LINESTRING (10 10, 90 90))");
  }
  
  public void testMultiLineStringWithMultiEmpty() {
    checkFix("MULTILINESTRING (EMPTY, EMPTY)",
        "MULTILINESTRING EMPTY");
  }
  
  //----------------------------------------
  
  public void testPolygonBowtie() {
    checkFix("POLYGON ((10 90, 90 10, 90 90, 10 10, 10 90))",
        "MULTIPOLYGON (((10 90, 50 50, 10 10, 10 90)), ((50 50, 90 90, 90 10, 50 50)))");
  }

  public void testPolygonHolesZeroAreaOverlapping() {
    checkFix("POLYGON ((10 90, 90 90, 90 10, 10 10, 10 90), (80 70, 30 70, 30 20, 30 70, 80 70), (70 80, 70 30, 20 30, 70 30, 70 80))",
        "POLYGON ((90 90, 90 10, 10 10, 10 90, 90 90))");
  }

  public void testPolygonPosAndNegOverlap() {
    checkFix("POLYGON ((10 90, 50 90, 50 30, 70 30, 70 50, 30 50, 30 70, 90 70, 90 10, 10 10, 10 90))",
        "POLYGON ((10 90, 50 90, 50 70, 90 70, 90 10, 10 10, 10 90), (50 50, 50 30, 70 30, 70 50, 50 50))");
  }

  public void testHolesTouching() {
    checkFix("POLYGON ((0 0, 0 5, 6 5, 6 0, 0 0), (3 1, 4 1, 4 2, 3 2, 3 1), (3 2, 1 4, 5 4, 4 2, 4 3, 3 2, 2 3, 3 2))",
        "MULTIPOLYGON (((0 0, 0 5, 6 5, 6 0, 0 0), (1 4, 2 3, 3 2, 3 1, 4 1, 4 2, 5 4, 1 4)), ((3 2, 4 3, 4 2, 3 2)))");
  }

  public void testPolygonNaN() {
    checkFix("POLYGON ((10 90, 90 NaN, 90 10, 10 10, 10 90))",
        "POLYGON ((10 10, 10 90, 90 10, 10 10))");
  }

  public void testPolygonRepeated() {
    checkFix("POLYGON ((10 90, 90 10, 90 10, 90 10, 90 10, 90 10, 10 10, 10 90))",
        "POLYGON ((10 10, 10 90, 90 10, 10 10))");
  }

  public void testPolygonShellCollapse() {
    checkFix("POLYGON ((10 10, 10 90, 90 90, 10 90, 10 10), (20 80, 60 80, 60 40, 20 40, 20 80))",
        "POLYGON EMPTY");
  }

  public void testPolygonShellCollapseNaN() {
    checkFix("POLYGON ((10 10, 10 NaN, 90 NaN, 10 NaN, 10 10))",
        "POLYGON EMPTY");
  }

  public void testPolygonShellKeepCollapseNaN() {
    checkFixKeepCollapse("POLYGON ((10 10, 10 NaN, 90 NaN, 10 NaN, 10 10))",
        "POINT (10 10)");
  }

  public void testPolygonShellKeepCollapse() {
    checkFixKeepCollapse("POLYGON ((10 10, 10 90, 90 90, 10 90, 10 10), (20 80, 60 80, 60 40, 20 40, 20 80))",
        "LINESTRING (10 10, 10 90, 90 90, 10 90, 10 10)");
  }

  public void testPolygonHoleCollapse() {
    checkFix("POLYGON ((10 90, 90 90, 90 10, 10 10, 10 90), (80 80, 20 80, 20 20, 20 80, 80 80))",
        "POLYGON ((10 10, 10 90, 90 90, 90 10, 10 10))");
  }

  public void testPolygonHoleKeepCollapse() {
    checkFixKeepCollapse("POLYGON ((10 90, 90 90, 90 10, 10 10, 10 90), (80 80, 20 80, 20 20, 20 80, 80 80))",
        "POLYGON ((10 10, 10 90, 90 90, 90 10, 10 10))");
  }
  
  //----------------------------------------

  public void testMultiPolygonEmpty() {
    checkFix("MULTIPOLYGON EMPTY",
        "MULTIPOLYGON EMPTY");
  }

  public void testMultiPolygonMultiEmpty() {
    checkFix("MULTIPOLYGON (EMPTY, EMPTY)",
        "MULTIPOLYGON EMPTY");
  }

  public void testMultiPolygonWithEmpty() {
    checkFix("MULTIPOLYGON (((10 40, 40 40, 40 10, 10 10, 10 40)), EMPTY, ((50 40, 80 40, 80 10, 50 10, 50 40)))",
        "MULTIPOLYGON (((10 40, 40 40, 40 10, 10 10, 10 40)), ((50 40, 80 40, 80 10, 50 10, 50 40)))");
  }

  public void testMultiPolygonWithCollapse() {
    checkFix("MULTIPOLYGON (((10 40, 40 40, 40 10, 10 10, 10 40)), ((50 40, 50 40, 50 40, 50 40, 50 40)))",
        "POLYGON ((10 10, 10 40, 40 40, 40 10, 10 10))");
  }

  public void testMultiPolygonKeepCollapse() {
    checkFixKeepCollapse("MULTIPOLYGON (((10 40, 40 40, 40 10, 10 10, 10 40)), ((50 40, 50 40, 50 40, 50 40, 50 40)))",
        "GEOMETRYCOLLECTION (POINT (50 40), POLYGON ((10 10, 10 40, 40 40, 40 10, 10 10)))");
  }

  //----------------------------------------

  public void testGCEmpty() {
    checkFix("GEOMETRYCOLLECTION EMPTY",
        "GEOMETRYCOLLECTION EMPTY");
  }

  public void testGCWithAllEmpty() {
    checkFix("GEOMETRYCOLLECTION (POINT EMPTY, LINESTRING EMPTY, POLYGON EMPTY)",
        "GEOMETRYCOLLECTION (POINT EMPTY, LINESTRING EMPTY, POLYGON EMPTY)");
  }

  //================================================
  
  
  private void checkFix(String wkt, String wktExpected) {
    Geometry geom = read(wkt);
    checkFix(geom, false, wktExpected);
  }
  
  private void checkFixKeepCollapse(String wkt, String wktExpected) {
    Geometry geom = read(wkt);
    checkFix(geom, true, wktExpected);
  }
  
  private void checkFix(Geometry input, String wktExpected) {
    checkFix(input, false, wktExpected);
  }
  
  private void checkFixKeepCollapse(Geometry input, String wktExpected) {
    checkFix(input, true, wktExpected);
  }
  
  private void checkFix(Geometry input, boolean keepCollapse, String wktExpected) {
    Geometry actual;
    if (keepCollapse) {
      GeometryFixer fixer = new GeometryFixer(input);
      fixer.setKeepCollapsed(true);
      actual = fixer.getResult();
    }
    else {
      actual= GeometryFixer.fix(input);
    }
    
    assertTrue("Result is invalid", actual.isValid());
    assertTrue("Input geometry was not copied", input != actual);
    assertTrue("Result has aliased coordinates", checkDeepCopy(input, actual));
    
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }

  private boolean checkDeepCopy(Geometry geom1, Geometry geom2) {
    Coordinate[] pts1 = geom1.getCoordinates();
    Coordinate[] pts2 = geom2.getCoordinates();
    for (Coordinate p2 : pts2) {
      if (isIn(p2, pts1)) {
        return false;
      }
    }
    return true;
  }

  private boolean isIn(Coordinate p, Coordinate[] pts) {
    for (int i = 0; i < pts.length; i++) {
      if (p == pts[i]) return true;
    }
    return false;
  }

}
