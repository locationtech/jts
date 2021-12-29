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

public class PolygonConcaveHullTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(PolygonConcaveHullTest.class);
  }

  public PolygonConcaveHullTest(String name) { super(name); }
  
  public void testSimple() {
    checkHull("POLYGON ((30 90, 10 40, 40 10, 70 10, 90 30, 80 80, 70 40, 30 40, 50 50, 60 70, 30 90))", 
        0, "POLYGON ((10 40, 30 90, 80 80, 90 30, 70 10, 40 10, 10 40))");
  }

  public void testZGore() {
    checkHull("POLYGON ((10 90, 40 60, 20 40, 40 20, 70 50, 40 30, 30 40, 60 70, 50 90, 90 90, 90 10, 10 10, 10 90))", 
        0.5, "POLYGON ((10 10, 10 90, 40 60, 50 90, 90 90, 90 10, 10 10))");
  }

  public void testFlat() {
    checkHull("POLYGON ((10 10, 10 90, 90 90, 90 50, 90 10, 50 10, 10 10))", 
        0.6, "POLYGON ((10 10, 10 90, 90 90, 90 10, 10 10))");
  }

  public void testInnerHull() {
    checkHull("POLYGON ((11 14, 2 31, 18 29, 25 17, 38 16, 29 5, 19 11, 11 0, 0 10, 11 14))", 
        -0.4, "POLYGON ((2 31, 18 29, 29 5, 19 11, 2 31))");
  }

  public void testOuterWithHole() {
    checkHull("POLYGON ((50 100, 30 70, 0 50, 30 30, 50 0, 70 30, 100 50, 70 70, 50 100), (50 75, 40 50, 10 50, 36 35, 50 5, 65 35, 90 50, 60 60, 50 75))", 
        0.1, "POLYGON ((50 100, 100 50, 50 0, 0 50, 50 100), (36 35, 50 5, 60 60, 36 35))");
  }

  private void checkHull(String wkt, double vertexCountFraction, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = PolygonConcaveHull.hull(geom, vertexCountFraction);
    //System.out.println(actual);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
}
