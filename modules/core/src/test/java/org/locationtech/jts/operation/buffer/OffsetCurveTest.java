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
package org.locationtech.jts.operation.buffer;

import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

/**
 * 
 * Note: most expected results are rounded to precision of 100, to reduce
 * size and improve robustness.
 * 
 * @author Martin Davis
 *
 */
public class OffsetCurveTest extends GeometryTestCase {
  public static void main(String[] args) {
    junit.textui.TestRunner.run(OffsetCurveTest.class);
  }
  
  public OffsetCurveTest(String name) {
    super(name);
  }
  
  public void testPoint() {
    checkOffsetCurve(
        "POINT (0 0)", 1,
        "LINESTRING EMPTY"
        );
  }

  public void testEmpty() {
    checkOffsetCurve(
        "LINESTRING EMPTY", 1,
        "LINESTRING EMPTY"
        );
  }

  public void testZeroLenLine() {
    checkOffsetCurve(
        "LINESTRING (1 1, 1 1)", 1,
        "LINESTRING EMPTY"
        );
  }

  public void testSegment1Short() {
    checkOffsetCurve(
        "LINESTRING (2 2, 2 2.0000001)", 1,
        "LINESTRING (1 2, 1 2.0000001)", 
        0.00000001
        );
  }

  public void testSegment1() {
    checkOffsetCurve(
        "LINESTRING (0 0, 9 9)", 1,
        "LINESTRING (-0.71 0.71, 8.29 9.71)"
        );
  }

  public void testSegment1Neg() {
    checkOffsetCurve(
        "LINESTRING (0 0, 9 9)", -1,
        "LINESTRING (0.71 -0.71, 9.71 8.29)"
        );
  }

  public void testSegments2() {
    checkOffsetCurve(
        "LINESTRING (0 0, 9 9, 25 0)", 1,
        "LINESTRING (-0.707 0.707, 8.293 9.707, 8.435 9.825, 8.597 9.915, 8.773 9.974, 8.956 9.999, 9.141 9.99, 9.321 9.947, 9.49 9.872, 25.49 0.872)"
        );
  }

  public void testSegments3() {
    checkOffsetCurve(
        "LINESTRING (0 0, 9 9, 25 0, 30 15)", 1,
        "LINESTRING (-0.71 0.71, 8.29 9.71, 8.44 9.83, 8.6 9.92, 8.77 9.97, 8.96 10, 9.14 9.99, 9.32 9.95, 9.49 9.87, 24.43 1.47, 29.05 15.32)"
        );
  }
  
  public void testZigzagOneEndCurved4() {
    checkOffsetCurve(
        "LINESTRING (1 3, 6 3, 4 5, 9 5)", 4,
        "LINESTRING (0.53 6.95, 0.67 7.22, 1.17 7.83, 1.78 8.33, 2.47 8.7, 3.22 8.92, 4 9, 9 9)"
        );
  }

  public void testZigzagOneEndCurved1() {
    checkOffsetCurve(
        "LINESTRING (1 3, 6 3, 4 5, 9 5)", 1,
        "LINESTRING (1 4, 3.59 4, 3.29 4.29, 3.17 4.44, 3.08 4.62, 3.02 4.8, 3 5, 3.02 5.2, 3.08 5.38, 3.17 5.56, 3.29 5.71, 3.44 5.83, 3.62 5.92, 3.8 5.98, 4 6, 9 6)"
        );
  }

  public void testEmptyResult() {
    checkOffsetCurve(
        "LINESTRING (3 5, 5 7, 7 5)", -4,
        "LINESTRING EMPTY"
        );
  }

  public void testSelfCross() {
    checkOffsetCurve(
        "LINESTRING (50 90, 50 10, 90 50, 10 50)", 10,
        "LINESTRING (60 90, 60 60)" );
  }

  public void testSelfCrossNeg() {
    checkOffsetCurve(
        "LINESTRING (50 90, 50 10, 90 50, 10 50)", -10,
        "LINESTRING (40 90, 40 60, 10 60)" );
  }

  public void testRing() {
    checkOffsetCurve(
        "LINESTRING (10 10, 50 90, 90 10, 10 10)", -10,
        "LINESTRING (26.18 20, 50 67.63, 73.81 20, 26.18 20)" );
  }
    
  public void testClosedCurve() {
    checkOffsetCurve(
        "LINESTRING (30 70, 80 80, 50 10, 10 80, 60 70)", 10,
        "LINESTRING (45 83.2, 78.04 89.81, 80 90, 81.96 89.81, 83.85 89.23, 85.59 88.29, 87.11 87.04, 88.35 85.5, 89.27 83.76, 89.82 81.87, 90 79.9, 89.79 77.94, 89.19 76.06, 59.19 6.06, 58.22 4.3, 56.91 2.77, 55.32 1.53, 53.52 0.64, 51.57 0.12, 49.56 0.01, 47.57 0.3, 45.68 0.98, 43.96 2.03, 42.49 3.4, 41.32 5.04, 1.32 75.04, 0.53 76.77, 0.09 78.63, 0.01 80.53, 0.29 82.41, 0.93 84.2, 1.89 85.85, 3.14 87.28, 4.65 88.45, 6.34 89.31, 8.17 89.83, 10.07 90, 11.96 89.81, 45 83.2)"
    );
  }
  
  public void testMultiLine() {
    checkOffsetCurve(
        "MULTILINESTRING ((20 30, 60 10, 80 60), (40 50, 80 30))", 10,
        "MULTILINESTRING ((24.47 38.94, 54.75 23.8, 70.72 63.71), (44.47 58.94, 84.47 38.94))"
    );
  }
  
  public void testPolygon() {
    checkOffsetCurve(
        "POLYGON ((100 200, 200 100, 100 100, 100 200))", 10,
        "LINESTRING (90 200, 90.19 201.95, 90.76 203.83, 91.69 205.56, 92.93 207.07, 94.44 208.31, 96.17 209.24, 98.05 209.81, 100 210, 101.95 209.81, 103.83 209.24, 105.56 208.31, 107.07 207.07, 207.07 107.07, 208.31 105.56, 209.24 103.83, 209.81 101.95, 210 100, 209.81 98.05, 209.24 96.17, 208.31 94.44, 207.07 92.93, 205.56 91.69, 203.83 90.76, 201.95 90.19, 200 90, 100 90, 98.05 90.19, 96.17 90.76, 94.44 91.69, 92.93 92.93, 91.69 94.44, 90.76 96.17, 90.19 98.05, 90 100, 90 200)"
    );
    checkOffsetCurve(
        "POLYGON ((100 200, 200 100, 100 100, 100 200))", -10,
        "LINESTRING (110 175.86, 175.86 110, 110 110, 110 175.86)"
    );
  }
  
  public void testPolygonWithHole() {
    checkOffsetCurve(
        "POLYGON ((20 80, 80 80, 80 20, 20 20, 20 80), (30 70, 70 70, 70 30, 30 30, 30 70))", 10,
        "MULTILINESTRING ((10 80, 10.19 81.95, 10.76 83.83, 11.69 85.56, 12.93 87.07, 14.44 88.31, 16.17 89.24, 18.05 89.81, 20 90, 80 90, 81.95 89.81, 83.83 89.24, 85.56 88.31, 87.07 87.07, 88.31 85.56, 89.24 83.83, 89.81 81.95, 90 80, 90 20, 89.81 18.05, 89.24 16.17, 88.31 14.44, 87.07 12.93, 85.56 11.69, 83.83 10.76, 81.95 10.19, 80 10, 20 10, 18.05 10.19, 16.17 10.76, 14.44 11.69, 12.93 12.93, 11.69 14.44, 10.76 16.17, 10.19 18.05, 10 20, 10 80), (40 60, 40 40, 60 40, 60 60, 40 60))"
    );
    checkOffsetCurve(
        "POLYGON ((20 80, 80 80, 80 20, 20 20, 20 80), (30 70, 70 70, 70 30, 30 30, 30 70))", -10,
        "LINESTRING EMPTY"
    );

  }
  
  //---------------------------------------
  
  public void testQuadSegs() {
    checkOffsetCurve(
        "LINESTRING (20 20, 50 50, 80 20)", 
        10, 2, -1, -1,
        "LINESTRING (12.93 27.07, 42.93 57.07, 50 60, 57.07 57.07, 87.07 27.07)"
    );
  }

  public void testJoinBevel() {
    checkOffsetCurve(
        "LINESTRING (20 20, 50 50, 80 20)", 
        10, -1, BufferParameters.JOIN_BEVEL, -1,
        "LINESTRING (12.93 27.07, 42.93 57.07, 57.07 57.07, 87.07 27.07)"
    );
  }
  
  public void testJoinMitre() {
    checkOffsetCurve(
        "LINESTRING (20 20, 50 50, 80 20)", 
        10, -1, BufferParameters.JOIN_MITRE, -1,
        "LINESTRING (12.93 27.07, 50 64.14, 87.07 27.07)"
    );
  }
  
  //=======================================
  
  private void checkOffsetCurve(String wkt, double distance, String wktExpected) {
    checkOffsetCurve(wkt, distance, wktExpected, 0.05);
  }
  
  private void checkOffsetCurve(String wkt, double distance, 
      int quadSegs, int joinStyle, double mitreLimit,
      String wktExpected) 
  {
    checkOffsetCurve(wkt, distance, quadSegs, joinStyle, mitreLimit, wktExpected, 0.05);
  }
  
  private void checkOffsetCurve(String wkt, double distance, String wktExpected, double tolerance) {
    Geometry geom = read(wkt);
    Geometry result = OffsetCurve.getCurve(geom, distance);
    //System.out.println(result);
    
    if (wktExpected == null)
      return;
    
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, tolerance);
  }
  
  private void checkOffsetCurve(String wkt, double distance, 
      int quadSegs, int joinStyle, double mitreLimit,
      String wktExpected, double tolerance) {
    Geometry geom = read(wkt);
    Geometry result = OffsetCurve.getCurve(geom, distance, quadSegs, joinStyle, mitreLimit);
    //System.out.println(result);
    
    if (wktExpected == null)
      return;
    
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, tolerance);
  }

}
