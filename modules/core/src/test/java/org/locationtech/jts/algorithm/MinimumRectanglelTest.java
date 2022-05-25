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
    checkMinRectangle("LINESTRING (1 2, 3 8, 9 6)", "POLYGON ((9 6, 7 10, -1 6, 1 2, 9 6))");
  }
  
  /**
   * Failure case from https://trac.osgeo.org/postgis/ticket/5163
   * @throws Exception
   */
  public void testFlatDiagonal() throws Exception {
    checkMinRectangle("LINESTRING(-99.48710639268086 34.79029839231914,-99.48370699999998 34.78689899963806,-99.48152167568102 34.784713675318976)", 
        "LINESTRING (-99.48710639268086 34.79029839231914, -99.48152167568102 34.784713675318976)");
  }  

  private void checkMinRectangle(String wkt, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = MinimumDiameter.getMinimumRectangle(geom);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual, TOL);
  }
  

}
