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
 * The test cases are chosen so that this has no effect on comparing expected to actual.
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
  
  public void testZeroOffsetLine() {
    checkOffsetCurve(
        "LINESTRING (0 0, 1 0, 1 1)", 0,
        "LINESTRING (0 0, 1 0, 1 1)"
        );
  }

  public void testZeroOffsetPolygon() {
    checkOffsetCurve(
        "POLYGON ((1 9, 9 1, 1 1, 1 9))", 0,
        "LINESTRING (1 9, 1 1, 9 1, 1 9)"
        );
  }

  /**
   * Test bug fix for removing repeated points in input for raw curve.
   * See https://github.com/locationtech/jts/issues/957
   */
  public void testRepeatedPoint() {
    checkOffsetCurve(
        "LINESTRING (4 9, 1 2, 7 5, 7 5, 4 9)", 1,
        "LINESTRING (4.24 7.02, 2.99 4.12, 5.48 5.36, 4.24 7.02)"
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
  
  public void testRightAngle() {
    checkOffsetCurve(
        "LINESTRING (2 8, 8 8, 8 1)", 1,
        "LINESTRING (2 9, 8 9, 8.2 8.98, 8.38 8.92, 8.56 8.83, 8.71 8.71, 8.83 8.56, 8.92 8.38, 8.98 8.2, 9 8, 9 1)"
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

  public void testAsymmetricU() {
    String wkt = "LINESTRING (1 1, 9 1, 9 2, 5 2)";
    checkOffsetCurve( wkt, 1,
        "LINESTRING (1 2, 4 2)"
        );
    checkOffsetCurve( wkt, -1,
        "LINESTRING (1 0, 9 0, 9.2 0.02, 9.38 0.08, 9.56 0.17, 9.71 0.29, 9.83 0.44, 9.92 0.62, 9.98 0.8, 10 1, 10 2, 9.98 2.2, 9.92 2.38, 9.83 2.56, 9.71 2.71, 9.56 2.83, 9.38 2.92, 9.2 2.98, 9 3, 5 3)"
        );
  }
  
  public void testSymmetricU() {
    String wkt = "LINESTRING (1 1, 9 1, 9 2, 1 2)";
    checkOffsetCurve( wkt, 1,
        "LINESTRING EMPTY"
        );
    checkOffsetCurve( wkt, -1,
        "LINESTRING (1 0, 9 0, 9.2 0.02, 9.38 0.08, 9.56 0.17, 9.71 0.29, 9.83 0.44, 9.92 0.62, 9.98 0.8, 10 1, 10 2, 9.98 2.2, 9.92 2.38, 9.83 2.56, 9.71 2.71, 9.56 2.83, 9.38 2.92, 9.2 2.98, 9 3, 1 3)"
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
        "MULTILINESTRING ((60 90, 60 60), (60 40, 60 34.14, 65.85 40, 60 40), (40 40, 10 40))" );
  }

  public void testSelfCrossNeg() {
    checkOffsetCurve(
        "LINESTRING (50 90, 50 10, 90 50, 10 50)", -10,
        "MULTILINESTRING ((40 90, 40 60, 10 60), (40 40, 40 10, 40.19 8.05, 40.76 6.17, 41.69 4.44, 42.93 2.93, 44.44 1.69, 46.17 0.76, 48.05 0.19, 50 0, 51.95 0.19, 53.83 0.76, 55.56 1.69, 57.07 2.93, 97.07 42.93, 98.31 44.44, 99.24 46.17, 99.81 48.05, 100 50, 99.81 51.95, 99.24 53.83, 98.31 55.56, 97.07 57.07, 95.56 58.31, 93.83 59.24, 91.95 59.81, 90 60, 60 60))" );
  }

  public void testSelfCrossCWNeg() {
    checkOffsetCurve(
        "LINESTRING (0 70, 100 70, 40 0, 40 100)", -10,
        "MULTILINESTRING ((0 60, 30 60), (50 60, 50 27.03, 78.25 60, 50 60), (50 80, 50 100))" );
  }

  public void testSelfCrossDartInside() {
    checkOffsetCurve(
        "LINESTRING (60 50, 10 80, 50 10, 90 80, 40 50)", 10,
        "MULTILINESTRING ((54.86 41.43, 50 44.34, 45.14 41.43), (43.9 40.83, 50 30.16, 56.1 40.83))" );
  }

  public void testSelfCrossDartOutside() {
    checkOffsetCurve(
        "LINESTRING (60 50, 10 80, 50 10, 90 80, 40 50)", -10,
        "LINESTRING (50 67.66, 15.14 88.57, 13.32 89.43, 11.35 89.91, 9.33 89.98, 7.34 89.64, 5.46 88.91, 3.76 87.82, 2.32 86.4, 1.19 84.73, 0.42 82.86, 0.04 80.88, 0.07 78.86, 0.5 76.88, 1.32 75.04, 41.32 5.04, 42.42 3.48, 43.8 2.16, 45.4 1.12, 47.17 0.41, 49.05 0.05, 50.95 0.05, 52.83 0.41, 54.6 1.12, 56.2 2.16, 57.58 3.48, 58.68 5.04, 98.68 75.04, 99.5 76.88, 99.93 78.86, 99.96 80.88, 99.58 82.86, 98.81 84.73, 97.68 86.4, 96.24 87.82, 94.54 88.91, 92.66 89.64, 90.67 89.98, 88.65 89.91, 86.68 89.43, 84.86 88.57, 50 67.66)" );
  }

  public void testSelfCrossDart2Inside() {
    checkOffsetCurve(
        "LINESTRING (64 45, 10 80, 50 10, 90 80, 35 45)", 10,
        "LINESTRING (55.00 38.91, 49.58 42.42, 44.74 39.34, 50 30.15, 55.00 38.91)" );
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
  
  public void testOverlapTriangleInside() {
    checkOffsetCurve(
        "LINESTRING (70 80, 10 80, 50 10, 90 80, 40 80)", 10,
        "LINESTRING (70 70, 40 70, 27.23 70, 50 30.15, 72.76 70, 70 70)"
        );
  }
  
  public void testOverlapTriangleOutside() {
    checkOffsetCurve(
        "LINESTRING (70 80, 10 80, 50 10, 90 80, 40 80)", -10,
        "LINESTRING (70 90, 40 90, 10 90, 8.11 89.82, 6.29 89.29, 4.6 88.42, 3.11 87.25, 1.87 85.82, 0.91 84.18, 0.29 82.39, 0.01 80.51, 0.1 78.61, 0.54 76.77, 1.32 75.04, 41.32 5.04, 42.42 3.48, 43.8 2.16, 45.4 1.12, 47.17 0.41, 49.05 0.05, 50.95 0.05, 52.83 0.41, 54.6 1.12, 56.2 2.16, 57.58 3.48, 58.68 5.04, 98.68 75.04, 99.46 76.77, 99.9 78.61, 99.99 80.51, 99.71 82.39, 99.09 84.18, 98.13 85.82, 96.89 87.25, 95.4 88.42, 93.71 89.29, 91.89 89.82, 90 90, 70 90)"
        );
  }
  
  //--------------------------------------------------------
  
  public void testMultiPoint() {
    checkOffsetCurve(
        "MULTIPOINT ((0 0), (1 1))", 1,
        "LINESTRING EMPTY"
        );
  }
  
  public void testMultiLine() {
    checkOffsetCurve(
        "MULTILINESTRING ((20 30, 60 10, 80 60), (40 50, 80 30))", 10,
        "MULTILINESTRING ((24.47 38.94, 54.75 23.8, 70.72 63.71), (44.47 58.94, 84.47 38.94))"
    );
  }
  
  public void testMixedWithPoint() {
    checkOffsetCurve(
        "GEOMETRYCOLLECTION (LINESTRING (20 30, 60 10, 80 60), POINT (0 0))", 10,
        "LINESTRING (24.47 38.94, 54.75 23.8, 70.72 63.71)"
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
  
  //-------------------------------------------------
  
  public void testJoined() {
    String input = "LINESTRING (0 50, 100 50, 50 100, 50 0)";
    checkOffsetCurveJoined( input, 10,
        "LINESTRING (0 60, 75.85 60, 60 75.85, 60 0)"
        );
    checkOffsetCurveJoined( input, -10,
        "LINESTRING (0 40, 100 40, 101.95 40.19, 103.83 40.76, 105.56 41.69, 107.07 42.93, 108.31 44.44, 109.24 46.17, 109.81 48.05, 110 50, 109.81 51.95, 109.24 53.83, 108.31 55.56, 107.07 57.07, 57.07 107.07, 55.56 108.31, 53.83 109.24, 51.95 109.81, 50 110, 48.05 109.81, 46.17 109.24, 44.44 108.31, 42.93 107.07, 41.69 105.56, 40.76 103.83, 40.19 101.95, 40 100, 40 0))"
        );
  }
  
  //-------------------------------------------------
  
  public void testInfiniteLoop() {
    checkOffsetCurve(
        "LINESTRING (21 101, -1 78, 12 43, 50 112, 73 -5, 19 2, 87 85, -7 38, 105 40)", 4,
        null
    );
  }
  
  // see https://github.com/shapely/shapely/issues/820
  public void testOffsetError() {
    checkOffsetCurve(
        "LINESTRING (12 20, 60 68, 111 114, 151 159, 210 218)", 
        3,
        "LINESTRING (9.878679656440358 22.121320343559642, 57.878679656440355 70.12132034355965, 57.99069368916718 70.22770917070595, 108.86775926900314 116.11682714467565, 148.75777204394902 160.99309151648976, 148.87867965644037 161.12132034355963, 207.87867965644037 220.12132034355963)"
    );
  }
  
  //---------------------------------------
  
  public void testQuadSegs() {
    checkOffsetCurve(
        "LINESTRING (20 20, 50 50, 80 20)", 
        10, 10, -1, -1,
        "LINESTRING (12.93 27.07, 42.93 57.07, 44.12 58.09, 45.46 58.91, 46.91 59.51, 48.44 59.88, 50 60, 51.56 59.88, 53.09 59.51, 54.54 58.91, 55.88 58.09, 57.07 57.07, 87.07 27.07)"
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
  
  // See https://github.com/qgis/QGIS/issues/53165
  public void testMinQuadrantSegments() {
    checkOffsetCurve(
        "LINESTRING (553772.0645892698 177770.05079236583, 553780.9235869241 177768.99614978794, 553781.8325485934 177768.41771963477)", 
        -11, 0, BufferParameters.JOIN_MITRE, -1,
        "LINESTRING (553770.76 177759.13, 553777.54 177758.32)"
    );
  }
  
  // See https://github.com/qgis/QGIS/issues/53165#issuecomment-1563214857
  public void testMinQuadrantSegments_QGIS() {
    checkOffsetCurve(
        "LINESTRING (421 622, 446 625, 449 627)", 
        133, 0, BufferParameters.JOIN_MITRE, -1,
        "LINESTRING (405.15 754.05, 416.3 755.39)"
    );
  }
  
  // See https://trac.osgeo.org/postgis/ticket/4072
  public void testJoinMitreError() {
    checkOffsetCurve(
        "LINESTRING(362194.505 5649993.044,362197.451 5649994.125,362194.624 5650001.876,362189.684 5650000.114,362192.542 5649992.324,362194.505 5649993.044)", 
        -0.045, 0, BufferParameters.JOIN_MITRE, -1,
        "LINESTRING (362194.52050157124 5649993.001754275, 362197.5086649931 5649994.098225646, 362194.65096611937 5650001.933395073, 362189.626113625 5650000.141129872, 362192.51525161567 5649992.266257602, 362194.5204958858 5649993.001752188)"
    );
  }
  
  // See https://trac.osgeo.org/postgis/ticket/4072
  public void testJoinMitreErrorSimple() {
    checkOffsetCurve(
        "LINESTRING (4.821 0.72, 7.767 1.801, 4.94 9.552, 0 7.79, 2.858 0, 4.821 0.72)", 
        -0.045, 0, BufferParameters.JOIN_MITRE, -1,
        "LINESTRING (4.83650157122754 0.6777542748970088, 7.824664993161384 1.7742256459460533, 4.966966119329371 9.6093950732796, -0.057886375241824 7.817129871774653, 2.8312516154153906 -0.0577423980712891, 4.836495885800319 0.6777521891305186)"
    );
  }
  
  // See https://trac.osgeo.org/postgis/ticket/3279
  public void testJoinMitreSingleLine() {
    checkOffsetCurve(
        "LINESTRING (0.39 -0.02, 0.4650008997915482 -0.02, 0.4667128891457749 -0.0202500016082272, 0.4683515425280024 -0.0210000000000019, 0.4699159706879993 -0.0222499999999996, 0.4714061701120011 -0.0240000000000018, 0.4929087886040002 -0.0535958153351002, 0.4968358395870001 -0.0507426457862002, 0.4774061701119963 -0.0239999999999952, 0.476353470688 -0.0222500000000011, 0.4761015425280001 -0.0210000000000007, 0.4766503813740676 -0.0202500058185111, 0.4779990890331232 -0.02, 0.6189999999999996 -0.02, 0.619 -0.0700000000000002, 0.634 -0.0700000000000002, 0.6339999999999998 -0.02, 0.65 -0.02)", 
        -0.002, 0, BufferParameters.JOIN_MITRE, -1,
        "LINESTRING (0.39 -0.022, 0.4648556402268155 -0.022, 0.4661407414895839 -0.0221876631893964, 0.4672953866748729 -0.022716134946407, 0.4685176359449585 -0.0236927292232623, 0.4698334593862525 -0.0252379526243584, 0.4924663251198579 -0.0563894198284619, 0.499629444080312 -0.0511851092703384, 0.479075235654203 -0.022894668402962, 0.4785370545613636 -0.022, 0.6169999999999995 -0.022, 0.617 -0.0720000000000002, 0.636 -0.0720000000000002, 0.6359999999999998 -0.022, 0.65 -0.022)"
    );
  }
  
  // See https://github.com/libgeos/geos/issues/1037
  public void testJoinMitreNegDistance() {
    checkOffsetCurve(
        "LINESTRING (0 0, 10 0, 10 10, 0 10, 0 0)", 
        -1, 0, BufferParameters.JOIN_MITRE, 5,
        "LINESTRING (-1 -1, 11 -1, 11 11, -1 11, -1 -1)"
    );
  }
  
  public void testPolygonJoinMitre() {
    checkOffsetCurve(
        "POLYGON ((1 1, 1 7, 5 4, 8 8, 8 1, 1 1))", 
        1, 0, BufferParameters.JOIN_MITRE, 5,
        "LINESTRING (0 0, 0 9, 4.8 5.4, 9 11, 9 0, 0 0)",
        0.0001
    );
    checkOffsetCurve(
        "POLYGON ((1 1, 1 7, 5 4, 8 8, 8 1, 1 1))", 
        -1, 0, BufferParameters.JOIN_MITRE, 5,
        "LINESTRING (2 2, 2 5, 5.2 2.6, 7 5, 7 2, 2 2)",
        0.0001
    );

  }
  
  //---------------------------------------
  
  // see https://github.com/locationtech/jts/issues/1147
  public void testSimplifyFactor() {
    checkOffsetCurveSimplify("LINESTRING (-74.10825983114643 205.2512862651522, -61.59032155710979 183.66416102497575, -56.17516937041343 177.39645645603244, -45.87664216572637 163.28009296700202, -32.80606824944878 140.06145723770865)", 
        -70, 0.0001,
        "LINESTRING (-134.66360133032907 170.13646580544648, -122.14566305629245 148.54934056527003, -114.55900733787851 137.9004382015626, -111.02744383471095 133.81287132602836, -104.85483126202604 125.3519680316891, -93.80502418590495 105.72303306503763)",
        0.00001);
  }


  //=======================================
  
  private static final double EQUALS_TOL = 0.05;

  private void checkOffsetCurve(String wkt, double distance, String wktExpected) {
    checkOffsetCurve(wkt, distance, wktExpected, 0.05);
  }
  
  private void checkOffsetCurve(String wkt, double distance, 
      int quadSegs, int joinStyle, double mitreLimit,
      String wktExpected) 
  {
    checkOffsetCurve(wkt, distance, quadSegs, joinStyle, mitreLimit, wktExpected, EQUALS_TOL);
  }
  
  private void checkOffsetCurveJoined(String wkt, double distance, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry result = OffsetCurve.getCurveJoined(geom, distance);
    //System.out.println(result);
    
    if (wktExpected == null)
      return;
    
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, EQUALS_TOL);
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

  private void checkOffsetCurveSimplify(String wkt, 
      double distance, double simplifyFactor,
      String wktExpected, double tolerance) {
    Geometry geom = read(wkt);
    BufferParameters params = new BufferParameters();
    params.setSimplifyFactor(simplifyFactor);
    OffsetCurve oc = new OffsetCurve(geom, distance, params);
    Geometry result =  oc.getCurve();
    
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, tolerance);
  }
}
