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
  //private CGAlgorithms rcga = new CGAlgorithms();

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
    // this should succeed
    assertTrue(isAllOrientationsEqualDD(pts));
    // this is expected to fail
    assertTrue(! OrientationIndexTest.isAllOrientationsEqual(pts));
  }

  public void testBadCCW2() throws Exception
  {
    // this case fails because subtraction of small from large loses precision
    Coordinate[] pts = {
      new Coordinate(219.3649559090992, 140.84159161824724),
      new Coordinate(168.9018919682399, -5.713787599646864),
      new Coordinate(186.80814046338352, 46.28973405831556),
    };
    // this should succeed
    assertTrue(isAllOrientationsEqualDD(pts));
    // this is expected to fail
    assertTrue(! OrientationIndexTest.isAllOrientationsEqual(pts));
  }

  public void testBadCCW3() throws Exception
  {
    // this case fails because subtraction of small from large loses precision
    Coordinate[] pts = {
        new Coordinate(279.56857838488514, -186.3790522565901),
        new Coordinate(-20.43142161511487, 13.620947743409914),
        new Coordinate(0, 0)
    };
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
    int[] orient = new int[3];
    orient[0] = orientationIndexDD(pts[0], pts[1], pts[2]);
    orient[1] = orientationIndexDD(pts[1], pts[2], pts[0]);
    orient[2] = orientationIndexDD(pts[2], pts[0], pts[1]);
    return orient[0] == orient[1] && orient[0] == orient[2];
  }
  
  private static int orientationIndexDD(Coordinate p1, Coordinate p2, Coordinate q)
  {
    DD dx1 = DD.valueOf(p2.x).selfSubtract(p1.x);
    DD dy1 = DD.valueOf(p2.y).selfSubtract(p1.y);
    DD dx2 = DD.valueOf(q.x).selfSubtract(p2.x);
    DD dy2 = DD.valueOf(q.y).selfSubtract(p2.y);

    return signOfDet2x2DD(dx1, dy1, dx2, dy2);
  }
  
  private static int signOfDet2x2DD(DD x1, DD y1, DD x2, DD y2)
  {
    DD det = x1.multiply(y2).subtract(y1.multiply(x2));
    if (det.isZero())
      return 0;
    if (det.isNegative())
      return -1;
    return 1;

  }
}