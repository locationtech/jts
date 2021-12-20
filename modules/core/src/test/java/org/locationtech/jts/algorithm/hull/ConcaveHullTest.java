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
  
  public void testLengthSimple() {
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

  public void testLengthCShape() {
    checkHullByLength("MULTIPOINT ((70 80), (80 90), (90 70), (50 80), (30 70), (20 40), (30 20), (50 10), (90 20), (40 50), (40 30), (41 67))", 
       50, "POLYGON ((30 70, 50 80, 80 90, 90 70, 70 80, 40 50, 40 30, 90 20, 50 10, 30 20, 20 40, 30 70))" );
  }

  //------------------------------------------------
  
  public void testAreaSimple() {
    checkHullByArea("MULTIPOINT ((10 10), (90 10), (30 70), (70 70), (50 60))", 
       .5, "POLYGON ((30 70, 70 70, 90 10, 50 60, 10 10, 30 70))" );
  }

  public void testAreaZero() {
    checkHullByArea("MULTIPOINT ((10 10), (90 10), (70 70), (50 60), (50 90), (40 70), (30 30))", 
       0, "POLYGON ((10 10, 40 70, 50 90, 70 70, 90 10, 50 60, 30 30, 10 10))" );
  }

  public void testAreaConvex() {
    checkHullByArea("MULTIPOINT ((10 10), (90 10), (70 70), (50 60), (50 90), (40 70), (30 30))", 
       1, "POLYGON ((10 10, 40 70, 50 90, 70 70, 90 10, 10 10))" );
  }

  //==========================================================================
  
  private void checkHullByLength(String wkt, double threshold, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHull.concaveHullByLength(geom, threshold);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  private void checkHullByArea(String wkt, double threshold, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ConcaveHull.concaveHullByArea(geom, threshold);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
}
