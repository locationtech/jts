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
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;

/**
 * Tests CGAlgorithms.computeOrientation
 * @version 1.7
 */
public class ComputeOrientationTest
    extends TestCase
{

  private WKTReader reader = new WKTReader();
  //private CGAlgorithms rcga = new CGAlgorithms();

  public static void main(String args[]) {
    TestRunner.run(ComputeOrientationTest.class);
  }

  public ComputeOrientationTest(String name) { super(name); }

  public void testCCW() throws Exception
  {
    assertTrue(isAllOrientationsEqual(getCoordinates("LINESTRING ( 0 0, 0 1, 1 1)")));

    // experimental case - can't make it fail
    Coordinate[] pts2 = {
      new Coordinate(1.0000000000004998, -7.989685402102996),
      new Coordinate(10.0, -7.004368924503866),
      new Coordinate(1.0000000000005, -7.989685402102996),
    };
    assertTrue(isAllOrientationsEqual(pts2));
  }
  
  // MD - deliberately disabled
  public void XtestBadCCW() throws Exception
  {
    // this case fails because subtraction of small from large loses precision
    Coordinate[] pts1 = {
      new Coordinate(1.4540766091864998, -7.989685402102996),
      new Coordinate(23.131039116367354, -7.004368924503866),
      new Coordinate(1.4540766091865, -7.989685402102996),
    };
    assertTrue(isAllOrientationsEqual(pts1));
  }

  private boolean isAllOrientationsEqual(Coordinate[] pts)
  {
    int[] orient = new int[3];
    orient[0] = CGAlgorithms.computeOrientation(pts[0], pts[1], pts[2]);
    orient[1] = CGAlgorithms.computeOrientation(pts[1], pts[2], pts[0]);
    orient[2] = CGAlgorithms.computeOrientation(pts[2], pts[0], pts[1]);
    return orient[0] == orient[1] && orient[0] == orient[2];
  }
  private Coordinate[] getCoordinates(String wkt)
      throws ParseException
  {
    Geometry geom = reader.read(wkt);
    return geom.getCoordinates();
  }
}