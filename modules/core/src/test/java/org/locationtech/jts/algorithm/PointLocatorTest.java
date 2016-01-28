/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package org.locationtech.jts.algorithm;

import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests PointInRing algorithms
 *
 * @version 1.7
 */
public class PointLocatorTest extends TestCase {

  private WKTReader reader = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(PointLocatorTest.class);
  }

  public PointLocatorTest(String name) { super(name); }

  public void testBox() throws Exception
  {
    runPtLocator(Location.INTERIOR, new Coordinate(10, 10),
"POLYGON ((0 0, 0 20, 20 20, 20 0, 0 0))");
  }

  public void testComplexRing() throws Exception
  {
    runPtLocator(Location.INTERIOR, new Coordinate(0, 0),
"POLYGON ((-40 80, -40 -80, 20 0, 20 -100, 40 40, 80 -80, 100 80, 140 -20, 120 140, 40 180,     60 40, 0 120, -20 -20, -40 80))");
  }

  public void testPointLocatorLinearRingLineString() throws Exception
  {
    runPtLocator(Location.BOUNDARY, new Coordinate(0, 0),
                 "GEOMETRYCOLLECTION( LINESTRING(0 0, 10 10), LINEARRING(10 10, 10 20, 20 10, 10 10))");
  }

  public void testPointLocatorPointInsideLinearRing() throws Exception
  {
    runPtLocator(Location.EXTERIOR, new Coordinate(11, 11),
                 "LINEARRING(10 10, 10 20, 20 10, 10 10)");
  }

   private void runPtLocator(int expected, Coordinate pt, String wkt)
      throws Exception
  {
    Geometry geom = reader.read(wkt);
    PointLocator pointLocator = new PointLocator();
    int loc = pointLocator.locate(pt, geom);
    assertEquals(expected, loc);
  }

}