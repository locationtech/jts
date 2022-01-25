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
package org.locationtech.jts.shape;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests {@link CubicBezierCurve}.
 * 
 * Because of the inherent approximation and variability of Bezier Curves, 
 * this class is not intended to check exact results, only that results are reasonable.
 * 
 * @author mdavis
 *
 */
public class CubicBezierCurveTest 
extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(CubicBezierCurveTest.class);
  }

  public CubicBezierCurveTest(String name)
  {
    super(name);
  }
  
  public void testAlphaSquare() {
    checkCurve("POLYGON ((40 60, 60 60, 60 40, 40 40, 40 60))", 1,
        "POLYGON ((40 60, 41.1 61, 42.3 61.8, 43.6 62.5, 45 63.1, 46.4 63.5, 47.8 63.8, 49.3 64, 50.7 64, 52.2 63.8, 53.6 63.5, 55 63.1, 56.4 62.5, 57.7 61.8, 58.9 61, 60 60, 61 58.9, 61.8 57.7, 62.5 56.4, 63.1 55, 63.5 53.6, 63.8 52.2, 64 50.7, 64 49.3, 63.8 47.8, 63.5 46.4, 63.1 45, 62.5 43.6, 61.8 42.3, 61 41.1, 60 40, 58.9 39, 57.7 38.2, 56.4 37.5, 55 36.9, 53.6 36.5, 52.2 36.2, 50.7 36, 49.3 36, 47.8 36.2, 46.4 36.5, 45 36.9, 43.6 37.5, 42.3 38.2, 41.1 39, 40 40, 39 41.1, 38.2 42.3, 37.5 43.6, 36.9 45, 36.5 46.4, 36.2 47.8, 36 49.3, 36 50.7, 36.2 52.2, 36.5 53.6, 36.9 55, 37.5 56.4, 38.2 57.7, 39 58.9, 40 60))");
  }

  public void testAlphaRightAngle() {
    checkCurve("LINESTRING (30 40, 40 50, 50 40)", 1,
        "LINESTRING (30 40, 30.1 41.1, 30.2 42.1, 30.5 43.1, 30.9 44, 31.4 44.9, 32 45.8, 32.7 46.6, 33.4 47.3, 34.2 48, 35.1 48.6, 36 49.1, 36.9 49.5, 37.9 49.8, 38.9 49.9, 40 50, 41.1 49.9, 42.1 49.8, 43.1 49.5, 44 49.1, 44.9 48.6, 45.8 48, 46.6 47.3, 47.3 46.6, 48 45.8, 48.6 44.9, 49.1 44, 49.5 43.1, 49.8 42.1, 49.9 41.1, 50 40)");
  }

  public void testAlphaRightZigzag() {
    checkCurve("LINESTRING (10 10, 20 19, 30 10, 40 20)", 1,
        "LINESTRING (10 10, 10.2 11, 10.4 11.9, 10.8 12.9, 11.2 13.7, 11.7 14.6, 12.3 15.3, 13 16, 13.7 16.7, 14.5 17.3, 15.3 17.8, 16.2 18.2, 17.1 18.5, 18 18.8, 19 18.9, 20 19, 20.9 18.9, 21.8 18.6, 22.5 18.1, 23.1 17.4, 23.7 16.6, 24.2 15.8, 24.8 14.9, 25.2 14, 25.8 13.1, 26.3 12.3, 26.9 11.5, 27.5 10.9, 28.2 10.4, 29.1 10.1, 30 10, 31 10.1, 32 10.3, 33 10.6, 33.9 11, 34.8 11.5, 35.7 12.1, 36.5 12.8, 37.2 13.5, 37.9 14.3, 38.5 15.2, 39 16.1, 39.4 17, 39.7 18, 39.9 19, 40 20)");
  }

  public void testCtrlRightZigzag() {
    checkCurve("LINESTRING (10 10, 20 20, 30 10, 40 20)", 
        "LINESTRING (10 15, 15 20, 25 20, 28 10, 32 10, 40 25)",
        "LINESTRING (10 10, 10.1 11, 10.3 12, 10.6 13, 11 13.9, 11.5 14.8, 12.1 15.7, 12.8 16.5, 13.5 17.2, 14.3 17.9, 15.2 18.5, 16.1 19, 17 19.4, 18 19.7, 19 19.9, 20 20, 21 19.9, 21.9 19.5, 22.8 19, 23.6 18.2, 24.4 17.4, 25.1 16.5, 25.8 15.5, 26.4 14.5, 27.1 13.5, 27.6 12.6, 28.2 11.8, 28.7 11, 29.1 10.5, 29.6 10.1, 30 10, 30.5 10.2, 31.1 10.7, 31.8 11.5, 32.6 12.5, 33.5 13.7, 34.4 15, 35.3 16.2, 36.2 17.5, 37.1 18.6, 37.9 19.6, 38.6 20.4, 39.2 20.9, 39.6 21, 39.9 20.7, 40 20)");
  }

  private void checkCurve(String wkt, double alpha, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = CubicBezierCurve.bezierCurve(geom, alpha);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual, 0.5);
  }
  
  private void checkCurve(String wkt, String wktCtrl, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry ctrl = read(wktCtrl);
    Geometry actual = CubicBezierCurve.bezierCurve(geom, ctrl);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual, 0.5);
  }
}
