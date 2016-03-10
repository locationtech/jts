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

package org.locationtech.jts.dissolve;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import test.jts.junit.GeometryUtils;


public class LineDissolverTest  extends TestCase {
  public static void main(String args[]) {
    TestRunner.run(LineDissolverTest.class);
  }

  public LineDissolverTest(String name) { super(name); }

  public void testDebug() throws ParseException
  {
    //testSingleLine();
    testIsolatedRing();
  }
  
  public void testSingleSegmentLine() throws ParseException
  {
    checkDissolve("LINESTRING (0 0, 1 1)", "LINESTRING (0 0, 1 1)");
  }

  public void testTwoSegmentLine() throws ParseException
  {
    checkDissolve("LINESTRING (0 0, 1 1, 2 2)", "LINESTRING (0 0, 1 1, 2 2)");
  }

  public void testOverlappingTwoSegmentLines() throws ParseException
  {
    checkDissolve(
        new String[] {"LINESTRING (0 0, 1 1, 2 2)", "LINESTRING (1 1, 2 2, 3 3)"}, 
        "LINESTRING (0 0, 1 1, 2 2, 3 3)");
  }

  public void testOverlappingLines3() throws ParseException
  {
    checkDissolve(
        new String[] {"LINESTRING (0 0, 1 1, 2 2)", 
            "LINESTRING (1 1, 2 2, 3 3)",
            "LINESTRING (1 1, 2 2, 2 0)" }, 
        "MULTILINESTRING ((0 0, 1 1, 2 2), (2 0, 2 2), (2 2, 3 3))");
  }

  public void testDivergingLines() throws ParseException
  {
    checkDissolve(
        "MULTILINESTRING ((0 0, 1 0, 2 1), (0 0, 1 0, 2 0), (1 0, 2 1, 2 0, 3 0))",  
        "MULTILINESTRING ((0 0, 1 0), (1 0, 2 0), (1 0, 2 1, 2 0), (2 0, 3 0))");
  }

  public void testLollipop() throws ParseException
  {
    checkDissolve(
        "LINESTRING (0 0, 1 0, 2 0, 2 1, 1 0, 0 0)",  
        "MULTILINESTRING ((0 0, 1 0), (1 0, 2 0, 2 1, 1 0))");
  }

  public void testDisjointLines() throws ParseException
  {
    checkDissolve(
        "MULTILINESTRING ((0 0, 1 0, 2 1), (10 0, 11 0, 12 0))",  
        "MULTILINESTRING ((0 0, 1 0, 2 1), (10 0, 11 0, 12 0))");
  }

  public void testSingleLine() throws ParseException
  {
    checkDissolve(
        "MULTILINESTRING ((0 0, 1 0, 2 1))",  
        "LINESTRING (0 0, 1 0, 2 1)");
  }

  public void testOneSegmentY() throws ParseException
  {
    checkDissolve("MULTILINESTRING ((0 0, 1 1, 2 2), (1 1, 1 2))", "MULTILINESTRING ((0 0, 1 1), (1 1, 2 2), (1 1, 1 2))");
  }

  public void testTwoSegmentY() throws ParseException
  {
    checkDissolve("MULTILINESTRING ((0 0, 9 9, 10 10, 11 11, 20 20), (10 10, 10 20))", 
        "MULTILINESTRING ((10 20, 10 10), (10 10, 9 9, 0 0), (10 10, 11 11, 20 20))");
  }

  public void testIsolatedRing() throws ParseException
  {
    checkDissolve("LINESTRING (0 0, 1 1, 1 0, 0 0)", "LINESTRING (0 0, 1 1, 1 0, 0 0)");
  }
  
  public void testIsolateRingFromMultipleLineStrings() throws ParseException
  {
    checkDissolve("MULTILINESTRING ((0 0, 1 0, 1 1), (0 0, 0 1, 1 1))", "LINESTRING (0 0, 0 1, 1 1, 1 0, 0 0)");
  }
  
  /**
   * Shows that rings with incident lines are created with the correct node point.
   * 
   * @throws ParseException
   */
  public void testRingWithTail() throws ParseException
  {
    checkDissolve("MULTILINESTRING ((0 0, 1 0, 1 1), (0 0, 0 1, 1 1), (1 0, 2 0))", "MULTILINESTRING ((1 0, 0 0, 0 1, 1 1, 1 0), (1 0, 2 0))");
  }
  
  public void testZeroLengthStartSegment() throws ParseException
  {
    checkDissolve(
        "MULTILINESTRING ((0 0, 0 0, 2 1))",  
        "LINESTRING (0 0, 2 1)");
  }


  private void checkDissolve(String wkt, String expectedWKT) throws ParseException {
    checkDissolve(new String[] { wkt }, expectedWKT);
  }

  private void checkDissolve(String[] wkt, String expectedWKT) throws ParseException {
    List geoms = GeometryUtils.readWKT(wkt);
    Geometry expected = GeometryUtils.readWKT(expectedWKT);
    checkDissolve(geoms, expected);
  }


  private void checkDissolve(List geoms, Geometry expected) {
    LineDissolver d = new LineDissolver();
    d.add(geoms);
    Geometry result = d.getResult();
    boolean equal = result.norm().equalsExact(expected.norm());
    if (! equal) {
      //System.out.println("Expected = " + expected
      //    + " actual = " + result.norm());
    }
    assertTrue(equal);
  }

}
