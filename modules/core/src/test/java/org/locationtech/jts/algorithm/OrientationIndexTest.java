/*
 * Copyright (c) 2016 Vivid Solutions.
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
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests Orientation.index
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
  
  public void testOrientationIndexRobust() throws Exception 
  { 
    Coordinate p0 = new Coordinate(219.3649559090992, 140.84159161824724); 
    Coordinate p1 = new Coordinate(168.9018919682399, -5.713787599646864); 
    Coordinate p = new Coordinate(186.80814046338352, 46.28973405831556); 
    int orient = Orientation.index(p0, p1, p); 
    int orientInv = Orientation.index(p1, p0, p); 
    assert(orient != orientInv); 
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
    orient[0] = Orientation.index(pts[0], pts[1], pts[2]);
    orient[1] = Orientation.index(pts[1], pts[2], pts[0]);
    orient[2] = Orientation.index(pts[2], pts[0], pts[1]);
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
