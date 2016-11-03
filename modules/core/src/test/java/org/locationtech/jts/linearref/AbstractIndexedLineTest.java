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
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;


/**
 * Base class for linear referencing class unit tests.
 */
public abstract class AbstractIndexedLineTest extends TestCase {

  private WKTReader reader = new WKTReader();

  public AbstractIndexedLineTest(String name) {
    super(name);
  }

  public void testFirst()
  {
    runOffsetTest("LINESTRING (0 0, 20 20)", "POINT(20 20)", 0.0, "POINT (20 20)");
  }

  public void testML()
  {
    runIndicesOfThenExtract("MULTILINESTRING ((0 0, 10 10), (20 20, 30 30))",
            "MULTILINESTRING ((1 1, 10 10), (20 20, 25 25))");
  }

  public void testPartOfSegmentNoVertex()
  {
    runIndicesOfThenExtract("LINESTRING (0 0, 10 10, 20 20)",
            "LINESTRING (1 1, 9 9)");
  }

  public void testPartOfSegmentContainingVertex()
  {
    runIndicesOfThenExtract("LINESTRING (0 0, 10 10, 20 20)",
            "LINESTRING (5 5, 10 10, 15 15)");
  }

  /**
   * Tests that duplicate coordinates are handled correctly.
   */
  public void testPartOfSegmentContainingDuplicateCoords()
  {
    runIndicesOfThenExtract("LINESTRING (0 0, 10 10, 10 10, 20 20)",
            "LINESTRING (5 5, 10 10, 10 10, 15 15)");
  }

  /**
   * Following tests check that correct portion of loop is identified.
   * This requires that the correct vertex for (0,0) is selected.
   */

  public void testLoopWithStartSubLine()
  {
    runIndicesOfThenExtract("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)",
            "LINESTRING (0 0, 0 10, 10 10)");
  }

  public void testLoopWithEndingSubLine()
  {
    runIndicesOfThenExtract("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)",
            "LINESTRING (10 10, 10 0, 0 0)");
  }

  // test a subline equal to the parent loop
  public void testLoopWithIdenticalSubLine()
  {
    runIndicesOfThenExtract("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)",
            "LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)");
  }

  // test a zero-length subline equal to the start point
  public void testZeroLenSubLineAtStart()
  {
    runIndicesOfThenExtract("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)",
            "LINESTRING (0 0, 0 0)");
  }

  // test a zero-length subline equal to a mid point
  public void testZeroLenSubLineAtMidVertex()
  {
    runIndicesOfThenExtract("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)",
            "LINESTRING (10 10, 10 10)");
  }

  public void testIndexOfAfterSquare()
  {
  	runIndexOfAfterTest("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)", 
  			"POINT (0 0)");
  }
  
  public void testIndexOfAfterRibbon()
  {
    runIndexOfAfterTest("LINESTRING (0 0, 0 60, 50 60, 50 20, -20 20)", 
    "POINT (0 20)");
    runIndexOfAfterTest("LINESTRING (0 0, 0 60, 50 60, 50 20, -20 20)", 
        "POINT (0 20)", "POINT (30 60)");
  }
  
  public void testIndexOfAfterBeyondEndRibbon()
  {
    runIndexOfAfterTest("LINESTRING (0 0, 0 60, 50 60, 50 20, -20 20)", 
        "POINT (-30 20)", "POINT (-20 20)");
  }
  
  public void testOffsetStartPoint()
  {
    runOffsetTest("LINESTRING (0 0, 10 10, 20 20)", "POINT(0 0)", 1.0, "POINT (-0.7071067811865475 0.7071067811865475)");
    runOffsetTest("LINESTRING (0 0, 10 10, 20 20)", "POINT(0 0)", -1.0, "POINT (0.7071067811865475 -0.7071067811865475)");
    runOffsetTest("LINESTRING (0 0, 10 10, 20 20)", "POINT(10 10)", 5.0, "POINT (6.464466094067262 13.535533905932738)");
    runOffsetTest("LINESTRING (0 0, 10 10, 20 20)", "POINT(10 10)", -5.0, "POINT (13.535533905932738 6.464466094067262)");
  }
  
  public void testOffsetStartPointRepeatedPoint()
  {
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(0 0)", 1.0, "POINT (-0.7071067811865475 0.7071067811865475)");
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(0 0)", -1.0, "POINT (0.7071067811865475 -0.7071067811865475)");
    // These tests work for LengthIndexedLine, but not LocationIndexedLine
    //runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(10 10)", 5.0, "POINT (6.464466094067262 13.535533905932738)");
    //runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(10 10)", -5.0, "POINT (13.535533905932738 6.464466094067262)");
  }

  public void testOffsetEndPoint()
  {
    runOffsetTest("LINESTRING (0 0, 20 20)", "POINT(20 20)", 0.0, "POINT (20 20)");
    runOffsetTest("LINESTRING (0 0, 13 13, 20 20)", "POINT(20 20)", 0.0, "POINT (20 20)");
    runOffsetTest("LINESTRING (0 0, 10 0, 20 0)", "POINT(20 0)", 1.0, "POINT (20 1)");
    runOffsetTest("LINESTRING (0 0, 20 0)", "POINT(10 0)", 1.0, "POINT (10 1)"); // point on last segment
    runOffsetTest("MULTILINESTRING ((0 0, 10 0), (10 0, 20 0))", "POINT(10 0)", -1.0, "POINT (10 -1)");
    runOffsetTest("MULTILINESTRING ((0 0, 10 0), (10 0, 20 0))", "POINT(20 0)", 1.0, "POINT (20 1)");
  }
  
  protected Geometry read(String wkt)
  {
    try {
      return reader.read(wkt);
    }
    catch (ParseException ex) {
      throw new RuntimeException(ex);
    }
  }

  protected void runIndicesOfThenExtract(String inputStr,
    String subLineStr)
//      throws Exception
  {
    Geometry input = read(inputStr);
    Geometry subLine = read(subLineStr);
    Geometry result = indicesOfThenExtract(input, subLine);
    checkExpected(result, subLineStr);
  }

  protected void checkExpected(Geometry result, String expected)
  {
    Geometry subLine = read(expected);
    boolean isEqual = result.equalsExact(subLine, 1.0e-5);
    if (! isEqual) {
      System.out.println("Computed result is: " + result);
    }
    assertTrue(isEqual);
  }

  protected abstract Geometry indicesOfThenExtract(Geometry input, Geometry subLine);

/*
  // example of indicesOfThenLocate method
  private Geometry indicesOfThenLocate(LineString input, LineString subLine)
  {
    LocationIndexedLine indexedLine = new LocationIndexedLine(input);
    LineStringLocation[] loc = indexedLine.indicesOf(subLine);
    Geometry result = indexedLine.locate(loc[0], loc[1]);
    return result;
  }
*/

  protected void runIndexOfAfterTest(String inputStr,
      String testPtWKT)
//        throws Exception
    {
      Geometry input = read(inputStr);
      Geometry testPoint = read(testPtWKT);
      Coordinate testPt = testPoint.getCoordinate();
      boolean resultOK = indexOfAfterCheck(input, testPt);
      assertTrue(resultOK);
    }
  
  protected void runIndexOfAfterTest(String inputStr,
      String testPtWKT, String afterPtWKT)
//        throws Exception
    {
      Geometry input = read(inputStr);
      Geometry testPoint = read(testPtWKT);
      Coordinate testPt = testPoint.getCoordinate();
      Geometry afterPoint = read(afterPtWKT);
      Coordinate afterPt = afterPoint.getCoordinate();
      boolean resultOK = indexOfAfterCheck(input, testPt, afterPt);
      assertTrue(resultOK);
    }
  
  /**
   * Checks that the point computed by <tt>indexOfAfter</tt>
   * is the same as the input point.
   * (This should be the case for all except pathological cases, 
   * such as the input test point being beyond the end of the line). 
   * 
   * @param input
   * @param testPt
   * @return true if the result of indexOfAfter is the same as the input point
   */
  protected abstract boolean indexOfAfterCheck(Geometry input, Coordinate testPt);
  
  protected abstract boolean indexOfAfterCheck(Geometry input, Coordinate testPt, Coordinate afterPt);

  static final double TOLERANCE_DIST = 0.001;
  
  protected void runOffsetTest(String inputWKT,
      String testPtWKT, double offsetDistance, String expectedPtWKT)
//        throws Exception
    {
      Geometry input = read(inputWKT);
      Geometry testPoint = read(testPtWKT);
      Geometry expectedPoint = read(expectedPtWKT);
      Coordinate testPt = testPoint.getCoordinate();
      Coordinate expectedPt = expectedPoint.getCoordinate();
      Coordinate offsetPt = extractOffsetAt(input, testPt, offsetDistance);
      
      boolean isOk = offsetPt.distance(expectedPt) < TOLERANCE_DIST;
      if (! isOk)
        System.out.println("Expected = " + expectedPoint + "  Actual = " + offsetPt);
      assertTrue(isOk);
    }
  
  protected abstract Coordinate extractOffsetAt(Geometry input, Coordinate testPt, double offsetDistance);

}