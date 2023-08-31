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
package org.locationtech.jts.algorithm.hull;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class ConcaveHullTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(ConcaveHullTest.class);
  }

  public ConcaveHullTest(String name) { super(name); }
  
  public void testLengthEmpty() {
    checkHullByLength("MULTIPOINT EMPTY", 
       70, "POLYGON EMPTY" );
  }

  public void testLengthPoint() {
    checkHullByLength("MULTIPOINT ((10 10), (10 10))", 
       70, "POINT (10 10)" );
  }

  public void testLengthCollinear() {
    checkHullByLength("LINESTRING (10 10, 20 20, 30 30))", 
       70, "LINESTRING (10 10, 30 30)" );
  }

  public void testLengthTriangle() {
    checkHullByLength("MULTIPOINT ((10 10), (90 10), (30 70))", 
       70, "POLYGON ((10 10, 30 70, 90 10, 10 10))" );
  }

  public void testLengthChevron() {
    checkHullByLength("MULTIPOINT ((10 10), (90 10), (30 70), (70 70), (50 60))", 
       70, "POLYGON ((30 70, 70 70, 90 10, 50 60, 10 10, 30 70))" );
  }

  public void testLengthZero() {
    checkHullByLength("MULTIPOINT ((10 10), (90 10), (70 70), (50 60), (50 90), (40 70), (30 30))", 
       0, "POLYGON ((10 10, 40 70, 50 90, 70 70, 90 10, 50 60, 30 30, 10 10))" );
  }

  public void testLengthConvex() {
    checkHullByLength("MULTIPOINT ((10 10), (90 10), (70 70), (50 60), (50 90), (40 70), (30 30))", 
       100, "POLYGON ((10 10, 40 70, 50 90, 70 70, 90 10, 10 10))" );
  }

  //------------------------------------------------

  public void testLenRatioCShape() {
    checkHullByLengthRatio("MULTIPOINT ((70 80), (80 90), (90 70), (50 80), (30 70), (20 40), (30 20), (50 10), (90 20), (40 50), (40 30), (41 67))", 
       0.2, "POLYGON ((20 40, 30 70, 50 80, 80 90, 90 70, 70 80, 41 67, 40 50, 40 30, 90 20, 50 10, 30 20, 20 40))" );
  }

  public void testLenRatioSShape() {
    checkHullByLengthRatio("MULTIPOINT ((0 81), (65 86), (70 71), (80 59), (92 49), (107 44), (122 41), (137 40), (152 41), (167 42), (182 47), (195 55), (203 68), (201 83), (188 92), (173 97), (158 100), (143 103), (128 106), (113 109), (98 112), (83 115), (68 120), (53 125), (40 133), (28 143), (18 155), (13 170), (12 185), (16 200), (26 213), (38 223), (51 231), (66 236), (81 240), (96 243), (111 245), (126 245), (141 245), (156 245), (171 244), (186 241), (201 238), (216 233), (229 225), (242 216), (252 204), (259 190), (262 175), (194 171), (189 186), (178 197), (164 203), (149 205), (134 206), (119 205), (104 203), (89 198), (77 188), (80 173), (93 165), (108 160), (123 157), (138 154), (153 151), (168 149), (183 146), (198 142), (213 138), (227 132), (241 126), (253 116), (263 104), (269 90), (271 75), (270 60), (264 46), (254 34), (243 23), (229 16), (215 10), (200 6), (185 3), (170 1), (155 0), (139 0), (123 0), (108 1), (93 3), (78 5), (63 10), (49 16), (35 23), (23 33), (13 45), (6 59), (16 82), (32 83), (48 84), (245 174), (228 173), (211 172), (131 128), (63 148), (222 207), (127 230), (154 131), (240 82), (72 220), (210 32), (90 22), (206 208), (57 202), (195 117), (55 166), (246 55), (201 101), (224 73), (211 192), (42 176), (152 228), (172 113), (24 61), (76 33), (92 216), (46 69), (118 138), (169 23), (213 118), (221 56), (44 192), (118 22), (224 40), (56 57), (192 32), (179 220), (34 44), (145 18), (239 194), (40 155), (92 136), (231 106), (40 207), (108 228), (256 81), (28 185), (54 33), (74 205), (172 132), (221 93), (249 96), (69 47), (78 146), (155 115), (202 223))", 
       0.1, "POLYGON ((16 200, 26 213, 38 223, 51 231, 66 236, 81 240, 96 243, 111 245, 126 245, 141 245, 156 245, 171 244, 186 241, 201 238, 216 233, 229 225, 242 216, 252 204, 259 190, 262 175, 245 174, 228 173, 211 172, 194 171, 189 186, 178 197, 164 203, 149 205, 134 206, 119 205, 104 203, 89 198, 77 188, 80 173, 93 165, 108 160, 123 157, 138 154, 153 151, 168 149, 183 146, 198 142, 213 138, 227 132, 241 126, 253 116, 263 104, 269 90, 271 75, 270 60, 264 46, 254 34, 243 23, 229 16, 215 10, 200 6, 185 3, 170 1, 155 0, 139 0, 123 0, 108 1, 93 3, 78 5, 63 10, 49 16, 35 23, 23 33, 13 45, 6 59, 0 81, 16 82, 32 83, 48 84, 65 86, 70 71, 80 59, 92 49, 107 44, 122 41, 137 40, 152 41, 167 42, 182 47, 195 55, 203 68, 201 83, 188 92, 173 97, 158 100, 143 103, 128 106, 113 109, 98 112, 83 115, 68 120, 53 125, 40 133, 28 143, 18 155, 13 170, 12 185, 16 200))" );
  }

  public void testLenRatioZero() {
    checkHullByLengthRatio("MULTIPOINT ((10 90), (10 10), (90 10), (90 90), (40 40), (60 30), (30 70), (40 60), (60 50), (60 72), (47 66), (90 60))", 
       0, "POLYGON ((30 70, 10 90, 60 72, 90 90, 90 60, 90 10, 60 30, 10 10, 40 40, 60 50, 47 66, 40 60, 30 70))" );
  }

  public void testLenRatioP5() {
    checkHullByLengthRatio("MULTIPOINT ((10 90), (10 10), (90 10), (90 90), (40 40), (60 30), (30 70), (40 60), (60 50), (60 72), (47 66), (90 60))", 
       0.5, "POLYGON ((30 70, 10 90, 60 72, 90 90, 90 60, 90 10, 60 30, 10 10, 40 40, 30 70))" );
  }

  public void testLenRatioOne() {
    checkHullByLengthRatio("MULTIPOINT ((10 90), (10 10), (90 10), (90 90), (40 40), (60 30), (30 70), (40 60), (60 50), (60 72), (47 66), (90 60))", 
       1, "POLYGON ((10 10, 10 90, 90 90, 90 60, 90 10, 10 10))" );
  }

  public void testLenRatioXYZChevronP5() {
    checkHullByLengthRatioXYZ("MULTIPOINT Z ((10 10 1), (90 10 2), (30 70 3), (70 70 4), (50 60 5))", 
       0.5, "POLYGON Z ((30 70 3, 70 70 4, 90 10 2, 50 60 5, 10 10 1, 30 70 3))" );
  }
  
  //------------------------------------------------
  
  public void testLengthHolesCircle() {
    checkHullWithHolesByLength(WKT_CIRCLE, 40, 
        "POLYGON ((20 90, 40 96, 56 95, 80 90, 90 70, 95 45, 90 20, 80 10, 45 5, 20 10, 10 20, 5 40, 11 60, 20 90), (20 70, 15 45, 40 20, 70 20, 80 40, 80 60, 70 80, 40 80, 20 70))" );
  }

  public void testLengthHolesCircle0() {
    checkHullWithHolesByLength(WKT_CIRCLE, 0,
        "POLYGON ((20 90, 40 96, 56 95, 70 80, 80 90, 90 70, 80 60, 95 45, 80 40, 70 20, 90 20, 80 10, 60 15, 45 5, 40 20, 40 80, 15 45, 21 30, 20 10, 10 20, 5 40, 11 60, 20 70, 20 90))" );
  }
  
  //------------------------------------------------
  
  private static String WKT_SIMPLE = "MULTIPOINT ((14 18), (18 14), (15 6), (15 2), (5 5), (3 13), (8 14), (8 10), (16 8))";
  private static String WKT_CIRCLE = "MULTIPOINT ((90 20), (80 10), (45 5), (10 20), (20 10), (21 30), (40 20), (11 60), (20 70), (20 90), (40 80), (70 80), (80 60), (90 70), (80 90), (56 95), (95 45), (80 40), (70 20), (15 45), (5 40), (40 96), (60 15))";
  
  public void testLengthSimple() {
    checkHullByLength(WKT_SIMPLE, 8,
        "POLYGON ((8 10, 5 5, 3 13, 8 14, 14 18, 18 14, 16 8, 15 2, 15 6, 8 10))" );
  }

  public void testAlphaSimple() {
    checkAlphaShape(WKT_SIMPLE, 4,
        "POLYGON ((5 5, 3 13, 8 14, 14 18, 18 14, 16 8, 8 10, 15 6, 15 2, 5 5))" );
  }

  public void testAlphaCircle() {
    checkAlphaShape(WKT_CIRCLE, 20,
        "POLYGON ((20 70, 20 90, 40 96, 56 95, 80 90, 90 70, 95 45, 90 20, 80 10, 60 15, 45 5, 20 10, 10 20, 5 40, 11 60, 20 70))" );
  }

  public void testAlphaWithHolesCircle() {
    checkAlphaShape(WKT_CIRCLE, 20, true,
        "POLYGON ((20 90, 40 96, 56 95, 80 90, 90 70, 95 45, 90 20, 80 10, 60 15, 45 5, 20 10, 10 20, 5 40, 11 60, 20 70, 20 90), (40 80, 15 45, 21 30, 40 20, 70 20, 80 40, 80 60, 70 80, 40 80))" );
  }

  //------------------------------------------------
  
  // These tests test that the computed Delaunay triangulation is correct
  // See https://github.com/locationtech/jts/pull/1004
  
  public void testRobust_GEOS946() {
    checkHullByLengthRatio("MULTIPOINT ((113.56577197798602 22.80081530883069),(113.565723279387 22.800815316487014),(113.56571548761124 22.80081531771092),(113.56571548780202 22.800815317674463),(113.56577197817877 22.8008153088047),(113.56577197798602 22.80081530883069))", 
       0.75, "POLYGON ((113.56571548761124 22.80081531771092, 113.565723279387 22.800815316487014, 113.56577197798602 22.80081530883069, 113.56577197817877 22.8008153088047, 113.56571548780202 22.800815317674463, 113.56571548761124 22.80081531771092))" );
  }
  
  public void testRobust_GEOS946_2() {
    checkHullByLengthRatio("MULTIPOINT ((584245.72096874 7549593.72686167), (584251.71398371 7549594.01629478), (584242.72446125 7549593.58214511), (584230.73978847 7549592.9760418), (584233.73581213 7549593.13045099), (584236.7318358 7549593.28486019), (584239.72795377 7549593.43742855), (584227.74314188 7549592.83423486))", 
       0.75, "POLYGON ((584227.74314188 7549592.83423486, 584239.72795377 7549593.43742855, 584242.72446125 7549593.58214511, 584245.72096874 7549593.72686167, 584251.71398371 7549594.01629478, 584230.73978847 7549592.9760418, 584227.74314188 7549592.83423486))" );
  }
  
  //==========================================================================
  
  private void checkHullByLengthRatio(String wkt, double threshold, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHull.concaveHullByLengthRatio(geom, threshold);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  private void checkHullByLengthRatioXYZ(String wkt, double threshold, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHull.concaveHullByLengthRatio(geom, threshold);
    Geometry expected = read(wktExpected);
    checkEqualXYZ(expected, actual);
  }
  
  private void checkHullByLength(String wkt, double threshold, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHull.concaveHullByLength(geom, threshold);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  private void checkHullWithHolesByLength(String wkt, double threshold, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHull.concaveHullByLength(geom, threshold, true);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }

  private void checkAlphaShape(String wkt, double alpha, String wktExpected) {
    checkAlphaShape(wkt, alpha, false, wktExpected);
  }
  
  private void checkAlphaShape(String wkt, double alpha, boolean isHolesAllowed, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHull.alphaShape(geom, alpha, isHolesAllowed);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  

}
