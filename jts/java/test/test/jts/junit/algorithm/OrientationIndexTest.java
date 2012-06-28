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
 * Tests CGAlgorithms.computeOrientation
 * @version 1.7
 */
public class OrientationIndexTest
    extends TestCase
{

  private static WKTReader reader = new WKTReader();
  //private CGAlgorithms rcga = new CGAlgorithms();

  public static void main(String args[]) {
    TestRunner.run(OrientationIndexTest.class);
  }

  public OrientationIndexTest(String name) { super(name); }

  public void testCCW() throws Exception
  {
    assertTrue(isAllOrientationsEqual(getCoordinates("LINESTRING ( 0 0, 0 1, 1 1)")));
  }
  
  public void testCCW2() throws Exception
  {
    // experimental case - can't make it fail
    Coordinate[] pts2 = {
      new Coordinate(1.0000000000004998, -7.989685402102996),
      new Coordinate(10.0, -7.004368924503866),
      new Coordinate(1.0000000000005, -7.989685402102996),
    };
    assertTrue(isAllOrientationsEqual(pts2));
  }
  
  /**
   * Tests whether the orientations around a triangle of points
   * are all equal (as is expected if the orientation predicate is correct)
   * 
   * @param pts an array of three points
   * @return true if all the orientations around the triangle are equal
   */
  public static boolean isAllOrientationsEqual(Coordinate[] pts)
  {
    int[] orient = new int[3];
    orient[0] = RobustDeterminant.orientationIndex(pts[0], pts[1], pts[2]);
    orient[1] = RobustDeterminant.orientationIndex(pts[1], pts[2], pts[0]);
    orient[2] = RobustDeterminant.orientationIndex(pts[2], pts[0], pts[1]);
    return orient[0] == orient[1] && orient[0] == orient[2];
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
    return isAllOrientationsEqual(pts);
  }
  
  public static Coordinate[] getCoordinates(String wkt)
      throws ParseException
  {
    Geometry geom = reader.read(wkt);
    return geom.getCoordinates();
  }
  

}