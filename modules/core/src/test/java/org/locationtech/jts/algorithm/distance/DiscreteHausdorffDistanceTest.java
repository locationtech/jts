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
package org.locationtech.jts.algorithm.distance;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class DiscreteHausdorffDistanceTest 
extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(DiscreteHausdorffDistanceTest.class);
  }

  public DiscreteHausdorffDistanceTest(String name) { super(name); }

  public void testLineSegments()
  {
    runTest("LINESTRING (0 0, 2 1)", "LINESTRING (0 0, 2 0)", "LINESTRING (2 0, 2 1)");
  }
  
  public void testLineSegments2()
  {
    runTest("LINESTRING (0 0, 2 0)", "LINESTRING (0 1, 1 2, 2 1)", "LINESTRING (1 0, 1 2)");
  }
  
  public void testLinePoints()
  {
    runTest("LINESTRING (0 0, 2 0)", "MULTIPOINT (0 2, 1 0, 2 1)", "LINESTRING (0 0, 0 2)");
  }
  
  /**
   * Shows effects of limiting HD to vertices,
   * which in this case does not produce the true Hausdorff distance.
   */
  public void testLinesShowingDiscretenessEffect()
  {
    String wkt1 = "LINESTRING (130 0, 0 0, 0 150)";
    String wkt2 = "LINESTRING (10 10, 10 150, 130 10)";
    runTest(wkt1, wkt2, "LINESTRING (10 10, 0 0)");
    // densifying provides accurate HD
    runTest(wkt1, wkt2, 0.5, "LINESTRING (0 80, 70 80)");
    
    //-- oriented mode
    runOriented(wkt1, wkt2, "LINESTRING (10 10, 0 0)");
    // densifying provides accurate HD
    runOriented(wkt1, wkt2, 0.1, "LINESTRING (107.41176470588235 36.352941176470594, 65 0)");
  }
  
  public void testOrientedLines() throws Exception
  {
    String wkt1 = "LINESTRING (1 6, 3 5, 1 4)";
    String wkt2 = "LINESTRING (1 9, 9 5, 1 1)";
    runOriented(wkt1, wkt2, "LINESTRING (2.2 8.4, 1 6)");
    runOriented(wkt2, wkt1, "LINESTRING (3 5, 9 5)");
  }

  public void testOrientedLines2() throws Exception
  {
    String wkt1 = "LINESTRING (1 6, 3 5, 1 4)";
    String wkt2 = "LINESTRING (1 3, 1 9, 9 5, 1 1)";
    runOriented(wkt1, wkt2, "LINESTRING (1 5, 3 5)");
    runOriented(wkt2, wkt1, "LINESTRING (3 5, 9 5)");
  }

  private static final double TOLERANCE = 0.00001;
  
  private void runTest(String wkt1, String wkt2, String wktExpected) 
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    Geometry result = DiscreteHausdorffDistance.distanceLine(g1, g2);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);
    
    double resultDistance = DiscreteHausdorffDistance.distance(g1, g2);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }
  
  private void runTest(String wkt1, String wkt2, double densifyFrac, String wktExpected) 
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    Geometry result = DiscreteHausdorffDistance.distanceLine(g1, g2, densifyFrac);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);
    
    double resultDistance = DiscreteHausdorffDistance.distance(g1, g2, densifyFrac);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }
  
  private void runOriented(String wkt1, String wkt2, String wktExpected) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    Geometry result = DiscreteHausdorffDistance.orientedDistanceLine(g1, g2);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);
    
    double resultDistance = DiscreteHausdorffDistance.orientedDistance(g1, g2);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }

  private void runOriented(String wkt1, String wkt2, double densifyFrac, String wktExpected) {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    
    Geometry result = DiscreteHausdorffDistance.orientedDistanceLine(g1, g2, densifyFrac);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result, TOLERANCE);
    
    double resultDistance = DiscreteHausdorffDistance.orientedDistance(g1, g2, densifyFrac);
    double expectedDistance = expected.getLength();
    assertEquals(expectedDistance, resultDistance, TOLERANCE);
  }

}
