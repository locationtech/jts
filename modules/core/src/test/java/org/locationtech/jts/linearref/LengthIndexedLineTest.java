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

package org.locationtech.jts.linearref;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 * Tests the {@link LengthIndexedLine} class
 */
public class LengthIndexedLineTest
    extends AbstractIndexedLineTest
{

  public static void main(String[] args) {
      junit.textui.TestRunner.run(LengthIndexedLineTest.class);
  }

  public LengthIndexedLineTest(String name) {
    super(name);
  }

  public void testExtractLineBothIndicesAtEndpointXXX()
  {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (20 0, 25 0, 30 0))",
        -10, 10, "LINESTRING (10 0, 10 0)");
  }


  public void testExtractLineBeyondRange()
  {
    checkExtractLine("LINESTRING (0 0, 10 10)", -100, 100, "LINESTRING (0 0, 10 10)");
  }

  public void testExtractLineReverse()
  {
    checkExtractLine("LINESTRING (0 0, 10 0)", 9, 1, "LINESTRING (9 0, 1 0)");
  }

  public void testExtractLineReverseMulti()
  {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (20 0, 25 0, 30 0))",
                     19, 1, "MULTILINESTRING ((29 0, 25 0, 20 0), (10 0, 1 0))");
  }

  public void testExtractLineNegative()
  {
    checkExtractLine("LINESTRING (0 0, 10 0)", -9, -1, "LINESTRING (1 0, 9 0)");
  }

  public void testExtractLineNegativeReverse()
  {
    checkExtractLine("LINESTRING (0 0, 10 0)", -1, -9, "LINESTRING (9 0, 1 0)");
  }

  public void testExtractLineIndexAtEndpoint()
  {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (20 0, 25 0, 30 0))",
                     10, -1, "LINESTRING (20 0, 25 0, 29 0)");
  }

  /**
   * Tests that leading and trailing zero-length sublines are trimmed in the computed result,
   * and that zero-length extracts return the lowest extracted zero-length line
   */
  public void testExtractLineIndexAtEndpointWithZeroLenComponents()
  {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (10 0, 10 0), (20 0, 25 0, 30 0))",
        10, -1, "LINESTRING (20 0, 25 0, 29 0)");
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (10 0, 10 0), (20 0, 25 0, 30 0))",
        5, 10, "LINESTRING (5 0, 10 0)");
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (10 0, 10 0), (10 0, 10 0), (20 0, 25 0, 30 0))",
        10, 10, "LINESTRING (10 0, 10 0)");
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (10 0, 10 0), (10 0, 10 0), (10 0, 10 0), (20 0, 25 0, 30 0))",
        10, -10, "LINESTRING (10 0, 10 0)");
  }

  public void testExtractLineBothIndicesAtEndpoint()
  {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (20 0, 25 0, 30 0))",
                     10, 10, "LINESTRING (10 0, 10 0)");
  }

  public void testExtractLineBothIndicesAtEndpointNegative()
  {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (20 0, 25 0, 30 0))",
                     -10, 10, "LINESTRING (10 0, 10 0)");
  }

  /**
   * From GEOS Ticket #323
   */
  public void testProjectExtractPoint()
  {
    Geometry linearGeom = read("MULTILINESTRING ((0 2, 0 0), (-1 1, 1 1))");
    LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    double index = indexedLine.project(new Coordinate(1, 0));
    Coordinate pt = indexedLine.extractPoint(index);
    assertTrue(pt.equals(new Coordinate(0, 0)));
  }

  public void testExtractPointBeyondRange()
  {
    Geometry linearGeom = read("LINESTRING (0 0, 10 10)");
    LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    Coordinate pt = indexedLine.extractPoint(100);
    assertTrue(pt.equals(new Coordinate(10, 10)));

    Coordinate pt2 = indexedLine.extractPoint(0);
    assertTrue(pt2.equals(new Coordinate(0, 0)));
  }

  public void testProjectPointWithDuplicateCoords()
  {
    Geometry linearGeom = read("LINESTRING (0 0, 10 0, 10 0, 20 0)");
    LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    double projIndex = indexedLine.project(new Coordinate(10, 1));
    assertTrue(projIndex == 10.0);
  }

  /**
   * These tests work for LengthIndexedLine, but not LocationIndexedLine
   *
   */
  public void testOffsetStartPointRepeatedPoint()
  {
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(0 0)", 1.0, "POINT (-0.7071067811865475 0.7071067811865475)");
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(0 0)", -1.0, "POINT (0.7071067811865475 -0.7071067811865475)");
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(10 10)", 5.0, "POINT (6.464466094067262 13.535533905932738)");
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(10 10)", -5.0, "POINT (13.535533905932738 6.464466094067262)");
  }

  /**
   * Tests that z values are interpolated
   *
   */
  public void testComputeZ()
  {
  	Geometry linearGeom = read("LINESTRING (0 0 0, 10 10 10)");
    LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    double projIndex = indexedLine.project(new Coordinate(5, 5));
    Coordinate projPt = indexedLine.extractPoint(projIndex);
//    System.out.println(projPt);
    assertTrue(projPt.equals3D(new Coordinate(5, 5, 5)));  
  }
  
  /**
   * Tests that if the input does not have Z ordinates, neither does the output.
   *
   */
  public void testComputeZNaN()
  {
  	Geometry linearGeom = read("LINESTRING (0 0, 10 10 10)");
    LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    double projIndex = indexedLine.project(new Coordinate(5, 5));
    Coordinate projPt = indexedLine.extractPoint(projIndex);
    assertTrue(Double.isNaN(projPt.z ));  
  }
  
  private void checkExtractLine(String wkt, double start, double end, String expected)
  {
    Geometry linearGeom = read(wkt);
    LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    Geometry result = indexedLine.extractLine(start, end);
    checkExpected(result, expected);
  }

  protected Geometry indicesOfThenExtract(Geometry linearGeom, Geometry subLine)
  {
    LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    double[] loc = indexedLine.indicesOf(subLine);
    Geometry result = indexedLine.extractLine(loc[0], loc[1]);
    return result;
  }

  protected boolean indexOfAfterCheck(Geometry linearGeom, Coordinate testPt)
  {
    LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    
    // check locations are consecutive
    double loc1 = indexedLine.indexOf(testPt);
    double loc2 = indexedLine.indexOfAfter(testPt, loc1);
    if (loc2 <= loc1) return false;
    
    // check extracted points are the same as the input
    Coordinate pt1 = indexedLine.extractPoint(loc1);
    Coordinate pt2 = indexedLine.extractPoint(loc2);
    if (! pt1.equals2D(testPt)) return false;
    if (! pt2.equals2D(testPt)) return false;
    
    return true;
  }

  protected boolean indexOfAfterCheck(Geometry linearGeom, Coordinate testPt, Coordinate checkPt)
  {
    LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    
    // check that computed location is after check location
    double checkLoc = indexedLine.indexOf(checkPt);
    double testLoc = indexedLine.indexOfAfter(testPt, checkLoc);
    if (testLoc < checkLoc) return false;
    
    return true;
  }

  protected Coordinate extractOffsetAt(Geometry linearGeom, Coordinate testPt, double offsetDistance)
  {
    LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    double index = indexedLine.indexOf(testPt);
    return indexedLine.extractPoint(index, offsetDistance);
  }

}