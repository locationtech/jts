/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlayNGZTest extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(OverlayNGZTest.class);
  }

  public OverlayNGZTest(String name) {
    super(name);
  }
  
  public void testPointXYPointDifference() {
    checkDifference("MULTIPOINT ((1 1), (5 5))", "POINT Z (5 5 99)",
        "POINT Z(1 1 99)");
  }

  // checks that Point Z is preserved
  public void testPointPolygonIntersection() {
    checkIntersection("POINT Z (5 5 99)", "POLYGON Z ((1 9 5, 9 9 9, 9 1 5, 1 1 1, 1 9 5))",
        "POINT Z(5 5 99)");
  }

  public void testLineIntersectionPointZInterpolated() {
    checkIntersection("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (10 0 0, 0 10 10)",
        "POINT(5 5 5)");
  }

  public void testLineIntersectionPointZValue() {
    checkIntersection("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (10 0 0, 5 5 999, 0 10 10)",
        "POINT(5 5 999)");
  }

  public void testLineOverlapUnion() {
    checkUnion("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (5 5 990, 15 15 999)",
        "MULTILINESTRING Z((0 0 0, 5 5 990), (5 5 990, 10 10 10), (10 10 10, 15 15 999))");
  }

  public void testLineLineXYDifferenceLineInterpolated() {
    checkDifference("LINESTRING (0 0 0, 10 10 10)", "LINESTRING (5 5, 6 6)",
        "MULTILINESTRING ((0 0 0, 5 5 5), (6 6 6, 10 10 10))");
  }

  // from https://trac.osgeo.org/geos/ticket/435
  public void testLineXYLineIntersection() {
    checkIntersection("LINESTRING(0 0,0 10,10 10,10 0)", "LINESTRING(10 10 4,10 0 5,0 0 5)",
        "GEOMETRYCOLLECTION Z(POINT Z(0 0 5), LINESTRING Z(10 0 5, 10 10 4))");
  }

  public void testLinePolygonIntersection() {
    checkIntersection("LINESTRING Z (0 0 0, 5 5 5)", "POLYGON Z ((1 9 5, 9 9 9, 9 1 5, 1 1 1, 1 9 5))",
        "LINESTRING Z (1 1 1, 5 5 5)");
  }

  public void testLinePolygonDifference() {
    checkDifference("LINESTRING Z (0 5 0, 10 5 10)", "POLYGON Z ((1 9 5, 9 9 9, 9 1 5, 1 1 1, 1 9 5))",
        "MULTILINESTRING Z((0 5 0, 1 5 2), (9 5 8, 10 5 10))");
  }

  public void testPointXYPolygonIntersection() {
    checkIntersection("POINT (5 5)", "POLYGON Z ((1 9 50, 9 9 90, 9 1 50, 1 1 10, 1 9 50))",
        "POINT Z(5 5 50)");
  }

  // XY Polygon gets Z value from Point
  public void testPointPolygonXYUnionn() {
    checkUnion("POINT Z (5 5 77)", "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))",
        "POLYGON Z((1 1 77, 1 9 77, 9 9 77, 9 1 77, 1 1 77))");
  }

  public void testLinePolygonXYDifference() {
    checkDifference("LINESTRING Z (0 5 0, 10 5 10)", "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))",
        "MULTILINESTRING Z((0 5 0, 1 5 1), (9 5 9, 10 5 10))");
  }
  
  public void testLineXYPolygonDifference() {
    checkDifference("LINESTRING (0 5, 10 5)", "POLYGON Z ((1 9 50, 9 9 90, 9 1 50, 1 1 10, 1 9 50))",
        "MULTILINESTRING Z((0 5 50, 1 5 30), (9 5 70, 10 5 50))");
  }

  public void testPolygonXYPolygonIntersection() {
    checkIntersection("POLYGON ((4 12, 2 6, 7 6, 11 4, 15 15, 4 12))", "POLYGON Z ((1 9 50, 9 9 90, 9 1 50, 1 1 10, 1 9 50))",
        "POLYGON Z((2 6 50, 3 9 60, 9 9 90, 9 5 70, 7 6 90, 2 6 50))");
  }

  public void testPolygonXYPolygonUnion() {
    checkUnion("POLYGON ((0 3, 3 3, 3 0, 0 0, 0 3))", "POLYGON Z ((1 9 50, 9 9 90, 9 1 50, 1 1 10, 1 9 50))",
        "POLYGON Z((0 0 10, 0 3 50, 1 3 20, 1 9 50, 9 9 90, 9 1 50, 3 1 20, 3 0 50, 0 0 10))");
  }

  // Test that operation on XY geoms produces XY (Z = NaN)
  public void testPolygonXYPolygonXYIntersection() {
    checkIntersection("POLYGON ((4 12, 2 6, 7 6, 11 4, 15 15, 4 12))", "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))",
        "POLYGON ((2 6, 3 9, 9 9, 9 5, 7 6, 2 6))");
  }

  //=================================================
  
  private void checkIntersection(String wktA, String wktB, String wktExpected) {
    checkOverlay(OverlayNG.INTERSECTION, wktA, wktB, wktExpected);
  }
  private void checkDifference(String wktA, String wktB, String wktExpected) {
    checkOverlay(OverlayNG.DIFFERENCE, wktA, wktB, wktExpected);
  }
  
  private void checkUnion(String wktA, String wktB, String wktExpected) {
    checkOverlay(OverlayNG.UNION, wktA, wktB, wktExpected);
  }
  
  private void checkOverlay(int opCode, String wktA, String wktB, String wktExpected) {
    Geometry a = read(wktA);
    Geometry b = read(wktB);
    Geometry result = OverlayNG.overlay(a, b, opCode);
    Geometry expected = read(wktExpected);
    checkEqualXYZ(expected, result);
  }
}
