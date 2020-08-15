/*
 * Copyright (c) 2019 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom;

import org.locationtech.jts.io.WKTWriter;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class TriangleCircumcentreTest extends GeometryTestCase {
  public static void main(String args[])
  {
    TestRunner.run(TriangleCircumcentreTest.class);
  }

  public TriangleCircumcentreTest(String name)
  {
    super(name);
  }
  
  // This test fails due to round-off error
  /*
  public void testSquareDiagonal() {
    Coordinate cc1 = circumcentre(193600.80333333334, 469345.355, 193600.80333333334, 469345.0175, 193601.10666666666, 469345.0175);
    Coordinate cc2 = circumcentre(193600.80333333334, 469345.355, 193601.10666666666, 469345.0175, 193601.10666666666, 469345.355);
    checkCCEqual(cc1,  cc2);
  }
  */

  public void testSquareDiagonalDD() {
    Coordinate cc1 = circumcentreDD(193600.80333333334, 469345.355, 193600.80333333334, 469345.0175, 193601.10666666666, 469345.0175);
    Coordinate cc2 = circumcentreDD(193600.80333333334, 469345.355, 193601.10666666666, 469345.0175, 193601.10666666666, 469345.355);
    checkCCEqual(cc1,  cc2);
  }

  private static Coordinate circumcentre(double ax, double ay, double bx, double by, double cx, double cy) {
    Coordinate a = new Coordinate(ax, ay);
    Coordinate b = new Coordinate(bx, by);
    Coordinate c = new Coordinate(cx, cy);
    return Triangle.circumcentre(a, b, c);
  }
  private static Coordinate circumcentreDD(double ax, double ay, double bx, double by, double cx, double cy) {
    Coordinate a = new Coordinate(ax, ay);
    Coordinate b = new Coordinate(bx, by);
    Coordinate c = new Coordinate(cx, cy);
    return Triangle.circumcentreDD(a, b, c);
  }
  
  private void checkCCEqual(Coordinate cc1, Coordinate cc2) {
    boolean isEqual = cc1.equals2D(cc2);
    if (! isEqual) {
      System.out.println("Triangle circumcentres are not equal!");
      System.out.println(WKTWriter.toPoint(cc1));
      System.out.println(WKTWriter.toPoint(cc2));
    }
    assertTrue(isEqual);
  }


}
