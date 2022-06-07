/*
 * Copyright (c) 2022 Martin Davis.
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

public class ConcaveHullOfPolygonsTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(ConcaveHullOfPolygonsTest.class);
  }

  public ConcaveHullOfPolygonsTest(String name) { super(name); }
  
  public void testEmpty() {
    String wkt = "MULTIPOLYGON EMPTY";
    checkHullTight(wkt, 1000, 
        "POLYGON EMPTY" );
  }

  public void testPolygon() {
    String wkt = "POLYGON ((1 9, 5 8, 9 9, 4 4, 7 1, 2 1, 1 9))";
    checkHullTight(wkt, 1000, 
        "POLYGON ((1 9, 5 8, 9 9, 4 4, 7 1, 2 1, 1 9))" );
    checkHull(wkt, 1000, 
        "POLYGON ((1 9, 9 9, 7 1, 2 1, 1 9))" );
  }

  public void testSimple() {
    String wkt = "MULTIPOLYGON (((100 200, 100 300, 150 250, 200 300, 200 200, 100 200)), ((100 100, 200 100, 150 50, 100 100)))";
    checkHullTight(wkt, 1000, 
        "POLYGON ((100 100, 100 200, 100 300, 150 250, 200 300, 200 200, 200 100, 150 50, 100 100))" );
    checkHull(wkt, 1000, 
        "POLYGON ((100 100, 100 200, 100 300, 200 300, 200 200, 200 100, 150 50, 100 100))" );
  }

  public void testSimpleNeck() {
    String wkt = "MULTIPOLYGON (((1 9, 5 8, 9 9, 9 6, 6 4, 4 4, 1 6, 1 9)), ((1 1, 4 3, 6 3, 9 1, 1 1)))";
    checkHullTight(wkt, 0, wkt );
    checkHullTight(wkt, 2, 
        "POLYGON ((6 3, 9 1, 1 1, 4 3, 4 4, 1 6, 1 9, 5 8, 9 9, 9 6, 6 4, 6 3))" );
    checkHullTight(wkt, 6, 
        "POLYGON ((1 1, 1 6, 1 9, 5 8, 9 9, 9 6, 9 1, 1 1))" );
  }

  public void testPoly3Concave1() {
    checkHullTight("MULTIPOLYGON (((1 5, 5 8, 5 5, 1 5)), ((5 1, 1 4, 5 4, 5 1)), ((6 8, 9 6, 7 5, 9 4, 6 1, 6 8)))", 
       100, "POLYGON ((6 8, 9 6, 7 5, 9 4, 6 1, 5 1, 1 4, 1 5, 5 8, 6 8))" );
  }

  public void testPoly3Concave3() {
    String wkt = "MULTIPOLYGON (((0 7, 4 10, 3 7, 5 6, 4 5, 0 7)), ((4 0, 0 2, 3 4, 5 3, 4 0)), ((9 10, 8 8, 10 9, 8 5, 10 3, 7 0, 6 3, 7 4, 7 6, 5 9, 9 10)))";

    checkHullTight( wkt, 0, wkt );
    checkHullTight( wkt, 2, 
        "POLYGON ((5 3, 4 0, 0 2, 3 4, 4 5, 0 7, 4 10, 5 9, 9 10, 8 8, 10 9, 8 5, 10 3, 7 0, 6 3, 5 3))" );
    checkHullTight( wkt, 4, 
        "POLYGON ((4 0, 0 2, 3 4, 4 5, 0 7, 4 10, 5 9, 9 10, 8 8, 10 9, 8 5, 10 3, 7 0, 4 0))" );
    checkHullTight( wkt, 100, 
        "POLYGON ((0 7, 4 10, 9 10, 8 8, 10 9, 8 5, 10 3, 7 0, 4 0, 0 2, 0 7))" );

    checkHullByLenRatio( wkt, 0, wkt);
    checkHullByLenRatio( wkt, 0.2, 
        "POLYGON ((5 9, 9 10, 10 9, 8 5, 10 3, 7 0, 6 3, 5 3, 4 0, 0 2, 3 4, 4 5, 0 7, 4 10, 5 9))" );
    checkHullByLenRatio( wkt, 0.5, 
        "POLYGON ((5 9, 9 10, 10 9, 8 5, 10 3, 7 0, 4 0, 0 2, 3 4, 4 5, 0 7, 4 10, 5 9))" );
    checkHullByLenRatio( wkt, 1, 
        "POLYGON ((9 10, 10 9, 10 3, 7 0, 4 0, 0 2, 0 7, 4 10, 9 10))" );
  }

  public void testPoly3WithHole() {
    String wkt = "MULTIPOLYGON (((1 9, 5 9, 5 7, 3 7, 3 5, 1 5, 1 9)), ((1 4, 3 4, 3 2, 5 2, 5 0, 1 0, 1 4)), ((6 9, 8 9, 9 5, 8 0, 6 0, 6 2, 8 5, 6 7, 6 9)))";
    checkHullWithHoles( wkt, .9, wkt);
    checkHullWithHoles( wkt, 1, 
        "POLYGON ((1 0, 1 4, 1 5, 1 9, 5 9, 6 9, 8 9, 9 5, 8 0, 6 0, 5 0, 1 0), (3 2, 5 2, 6 2, 8 5, 6 7, 5 7, 3 7, 3 5, 3 4, 3 2))");
    checkHullWithHoles( wkt, 2.5, 
        "POLYGON ((1 5, 1 9, 5 9, 6 9, 8 9, 9 5, 8 0, 6 0, 5 0, 1 0, 1 4, 1 5), (3 4, 3 2, 5 2, 6 2, 8 5, 6 7, 5 7, 3 7, 3 5, 3 4))");
    checkHullWithHoles( wkt, 4, 
        "POLYGON ((1 5, 1 9, 5 9, 6 9, 8 9, 9 5, 8 0, 6 0, 5 0, 1 0, 1 4, 1 5), (5 2, 6 2, 8 5, 6 7, 5 7, 3 5, 5 2))");
    checkHullWithHoles( wkt, 9, 
        "POLYGON ((6 9, 8 9, 9 5, 8 0, 6 0, 5 0, 1 0, 1 4, 1 5, 1 9, 5 9, 6 9))");
  }
  
  private void checkHull(String wkt, double maxLen, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHullOfPolygons.concaveHullByLength(geom, maxLen);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  private void checkHullByLenRatio(String wkt, double lenRatio, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHullOfPolygons.concaveHullByLengthRatio(geom, lenRatio);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  private void checkHullTight(String wkt, double maxLen, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHullOfPolygons.concaveHullByLength(geom, maxLen, true, false);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  private void checkHullWithHoles(String wkt, double maxLen, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHullOfPolygons.concaveHullByLength(geom, maxLen, false, true);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
}
