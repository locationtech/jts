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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;


/**
 * @version 1.7
 */
public class MinimumRectanglelTest extends GeometryTestCase {

  private static final double TOL = 1e-10;

  public static void main(String args[]) {
    TestRunner.run(MinimumRectanglelTest.class);
  }

  public MinimumRectanglelTest(String name) { super(name); }

  public void testLengthZero() {
    checkMinRectangle("LINESTRING (1 1, 1 1)", "POINT (1 1)");
  }
  
  public void testHorizontal() {
    checkMinRectangle("LINESTRING (1 1, 3 1, 5 1, 7 1)", "LINESTRING (1 1, 7 1)");
  }
  
  public void testVertical() {
    checkMinRectangle("LINESTRING (1 1, 1 4, 1 7, 1 9)", "LINESTRING (1 1, 1 9)");
  }
  
  public void testBentLine() {
    checkMinRectangle("LINESTRING (1 2, 3 8, 9 8)", 
        "POLYGON ((6.84 10.88, 9 8, 1 2, -1.16 4.88, 6.84 10.88))");
  }
  
  public void testNotMinDiamter() {
    checkMinRectangle("POLYGON ((150 300, 200 300, 300 300, 300 250, 280 120, 210 100, 100 100, 100 240, 150 300))", 
        "POLYGON ((100 100, 100 300, 300 300, 300 100, 100 100))");
  }
  
  public void testConvex() {
    checkMinRectangle("POLYGON ((3 8, 6 8, 9 5, 7 3, 3 1, 2 4, 3 8))", 
        "POLYGON ((0.2 6.6, 6.6 9.8, 9.4 4.2, 3 1, 0.2 6.6))");
  }
  
  /**
   * Failure case from https://trac.osgeo.org/postgis/ticket/5163
   * @throws Exception
   */
  public void testFlatDiagonal() throws Exception {
    checkMinRectangle("LINESTRING(-99.48710639268086 34.79029839231914,-99.48370699999998 34.78689899963806,-99.48152167568102 34.784713675318976)", 
        "LINESTRING (-99.48710639268086 34.79029839231914, -99.48152167568102 34.784713675318976)");
  } 
  
  public void testBadRectl() throws Exception {
    checkMinRectangle("POLYGON ((-5.21175 49.944633, -5.77435 50.021367, -5.7997 50.0306, -5.81815 50.0513, -5.82625 50.073567, -5.83085 50.1173, -6.2741 56.758767, -5.93245 57.909, -5.1158 58.644533, -5.07915 58.661733, -3.42575 58.686633, -3.1392 58.6685, -3.12495 58.666233, -1.88745 57.6444, 1.68845 52.715133, 1.7057 52.6829, 1.70915 52.6522, 1.7034 52.585433, 1.3867 51.214033, 1.36595 51.190267, 1.30485 51.121967, 0.96365 50.928567, 0.93025 50.912433, 0.1925 50.7436, -5.21175 49.944633))", 
        "POLYGON ((1.8583607388069103 50.41649058582797, -5.816631979932251 49.904263313964535, -6.395241388167441 58.57389735949991, 1.2797513305717105 59.08612463136336, 1.8583607388069103 50.41649058582797))");
  }  

  private void checkMinRectangle(String wkt, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = MinimumRectangle.getMinimumRectangle(geom);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual, TOL);
  }
  

}
