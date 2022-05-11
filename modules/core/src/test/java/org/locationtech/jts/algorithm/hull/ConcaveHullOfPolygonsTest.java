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

  private void checkHull(String wkt, double threshold, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHullOfPolygons.concaveHullByLength(geom, threshold);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  private void checkHullTight(String wkt, double threshold, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHullOfPolygons.concaveHullByLength(geom, threshold, true, false);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
}
