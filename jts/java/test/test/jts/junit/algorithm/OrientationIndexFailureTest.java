/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package test.jts.junit.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.math.DD;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;

/**
 * Tests failure cases of CGAlgorithms.computeOrientation
 * @version 1.7
 */
public class OrientationIndexFailureTest
    extends TestCase
{

  private WKTReader reader = new WKTReader();

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
    // from JTS list - 6/15/2012  another strange case from Tomas Fa
    Coordinate[] pts = {
        new Coordinate(-5.9, 163.1),
        new Coordinate(76.1, 250.7),
        new Coordinate(14.6, 185)
        //new Coordinate(96.6, 272.6)
    };
    checkOrientation(pts);
  }

  void checkOrientation(Coordinate[] pts)
  {
    // this should succeed
    assertTrue(isAllOrientationsEqualDD(pts));
    // this is expected to fail
    assertTrue(! OrientationIndexTest.isAllOrientationsEqual(pts));
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
  
}