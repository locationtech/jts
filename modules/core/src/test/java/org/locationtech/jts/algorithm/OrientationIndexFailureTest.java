/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests failure cases of CGAlgorithms.computeOrientation
 * @version 1.7
 */
public class OrientationIndexFailureTest
    extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(OrientationIndexFailureTest.class);
  }

  public OrientationIndexFailureTest(String name) { super(name); }

  /**
   * This is included to confirm this test is operating correctly
   * @throws Exception
   */
  public void testSanity() throws Exception
  {
    assertTrue(OrientationIndexTest.isAllOrientationsEqual(
        OrientationIndexTest.getCoordinates("LINESTRING ( 0 0, 0 1, 1 1)")));
  }

  public void testBadCCW() throws Exception
  {
    // this case fails because subtraction of small from large loses precision
    Coordinate[] pts = {
      new Coordinate(1.4540766091864998, -7.989685402102996),
      new Coordinate(23.131039116367354, -7.004368924503866),
      new Coordinate(1.4540766091865, -7.989685402102996),
    };
    checkOrientation(pts);
  }

  public void testBadCCW2() throws Exception
  {
    // this case fails because subtraction of small from large loses precision
    Coordinate[] pts = {
      new Coordinate(219.3649559090992, 140.84159161824724),
      new Coordinate(168.9018919682399, -5.713787599646864),
      new Coordinate(186.80814046338352, 46.28973405831556),
    };
    checkOrientation(pts);
  }

  public void testBadCCW3() throws Exception
  {
    // this case fails because subtraction of small from large loses precision
    Coordinate[] pts = {
        new Coordinate(279.56857838488514, -186.3790522565901),
        new Coordinate(-20.43142161511487, 13.620947743409914),
        new Coordinate(0, 0)
    };
    checkOrientation(pts);
  }

  public void testBadCCW4() throws Exception
  {
    // from JTS list - 5/15/2012  strange case for the GeometryNoder
    Coordinate[] pts = {
        new Coordinate(-26.2, 188.7),
        new Coordinate(37.0, 290.7),
        new Coordinate(21.2, 265.2)
    };
    checkOrientation(pts);
  }

  public void testBadCCW5() throws Exception
  {
    // from JTS list - 6/15/2012  another case from Tomas Fa
    Coordinate[] pts = {
        new Coordinate(-5.9, 163.1),
        new Coordinate(76.1, 250.7),
        new Coordinate(14.6, 185)
        //new Coordinate(96.6, 272.6)
    };
    checkOrientation(pts);
  }

  public void testBadCCW7() throws Exception
  {
    // from JTS list - 6/26/2012  another case from Tomas Fa
    Coordinate[] pts = {
        new Coordinate(-0.9575, 0.4511),
        new Coordinate(-0.9295, 0.3291),
        new Coordinate(-0.8945, 0.1766)
    };
    checkDD(pts, true);
    checkShewchuk(pts, false);
    checkOriginalJTS(pts, false);
  }

  public void testBadCCW7_2() throws Exception
  {
    // from JTS list - 6/26/2012  another case from Tomas Fa
    // scale to integers - all methods work on this
    Coordinate[] pts = {
        new Coordinate(-9575, 4511),
        new Coordinate(-9295, 3291),
        new Coordinate(-8945, 1766)
    };
    checkDD(pts, true);
    checkShewchuk(pts, true);
    checkOriginalJTS(pts, true);
  }


  public void testBadCCW6() throws Exception
  {
    // from JTS Convex Hull "Almost collinear" unit test
    Coordinate[] pts = {
        new Coordinate(-140.8859438214298, 140.88594382142983),
        new Coordinate(-57.309236848216706, 57.30923684821671),
        new Coordinate(-190.9188309203678, 190.91883092036784)
    };
    checkOrientation(pts);
  }
  
  /**
   * Shorthand method for most common case,
   * where the high-precision methods work but JTS Robust algorithm fails.
   * @param pts
   */
  void checkOrientation(Coordinate[] pts)
  {
    // this should succeed
    checkDD(pts, true);
    checkShewchuk(pts, true);
    
    // this is expected to fail
    checkOriginalJTS(pts, false);
  }

  private void checkShewchuk(Coordinate[] pts, boolean expected)
  {
    assertTrue("Shewchuk", expected == isAllOrientationsEqualSD(pts));
  }

  private void checkOriginalJTS(Coordinate[] pts, boolean expected)
  {
    assertTrue("JTS Robust FAIL", expected == OrientationIndexTest.isAllOrientationsEqual(pts));
  }

  private void checkDD(Coordinate[] pts, boolean expected)
  {
    assertTrue("DD", expected == isAllOrientationsEqualDD(pts));
  }
  
  public static boolean isAllOrientationsEqual(
      double p0x, double p0y,
      double p1x, double p1y,
      double p2x, double p2y)
  {
    Coordinate[] pts = {
        new Coordinate(p0x, p0y),
        new Coordinate(p1x, p1y),
        new Coordinate(p2x, p2y)
    };
    if (! isAllOrientationsEqualDD(pts))
      throw new IllegalStateException("High-precision orientation computation FAILED");
    return OrientationIndexTest.isAllOrientationsEqual(pts);
  }

  public static boolean isAllOrientationsEqualDD(Coordinate[] pts)
  {
    int orient0 = CGAlgorithmsDD.orientationIndex(pts[0], pts[1], pts[2]);
    int orient1 = CGAlgorithmsDD.orientationIndex(pts[1], pts[2], pts[0]);
    int orient2 = CGAlgorithmsDD.orientationIndex(pts[2], pts[0], pts[1]);
    return orient0 == orient1 && orient0 == orient2;
  }
  
  public static boolean isAllOrientationsEqualSD(Coordinate[] pts)
  {
    int orient0 = ShewchuksDeterminant.orientationIndex(pts[0], pts[1], pts[2]);
    int orient1 = ShewchuksDeterminant.orientationIndex(pts[1], pts[2], pts[0]);
    int orient2 = ShewchuksDeterminant.orientationIndex(pts[2], pts[0], pts[1]);
    return orient0 == orient1 && orient0 == orient2;
  }
  
}