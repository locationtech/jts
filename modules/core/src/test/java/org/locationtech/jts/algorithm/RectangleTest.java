/*
 * Copyright (c) 2023 Martin Davis.
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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class RectangleTest extends GeometryTestCase 
{
  private static final double TOL = 1e-10;
  
  public static void main(String args[]) {
    TestRunner.run(RectangleTest.class);
  }

  public RectangleTest(String name) { super(name); }
  
  public void testOrthogonal() {
    checkRectangle("LINESTRING (9 1, 1 1, 0 5, 7 10, 10 6)",
        "POLYGON ((0 1, 0 10, 10 10, 10 1, 0 1))");
  }
  
  public void test45() {
    checkRectangle("LINESTRING (10 5, 5 0, 2 1, 2 7, 9 9)",
        "POLYGON ((-1 4, 6.5 11.5, 11.5 6.5, 4 -1, -1 4))");
  }
  
  public void testCoincidentBaseSides() {
    checkRectangle("LINESTRING (10 5, 7 0, 7 0, 2 7, 10 5)",
        "POLYGON ((0.2352941176470591 4.0588235294117645, 3.2352941176470598 9.058823529411764, 10 5, 7 0, 0.2352941176470591 4.0588235294117645))");
  }
  
  private void checkRectangle(String wkt, String wktExpected) {
    LineString line = (LineString) read(wkt);
    Coordinate baseRightPt = line.getCoordinateN(0);
    Coordinate baseLeftPt = line.getCoordinateN(1);
    Coordinate leftSidePt = line.getCoordinateN(2);
    Coordinate oppositePt = line.getCoordinateN(3);
    Coordinate rightSidePt = line.getCoordinateN(4);
    Geometry actual = Rectangle.createFromSidePts(baseRightPt, baseLeftPt,
        oppositePt, leftSidePt, rightSidePt, line.getFactory());
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual, TOL);
  }
}
