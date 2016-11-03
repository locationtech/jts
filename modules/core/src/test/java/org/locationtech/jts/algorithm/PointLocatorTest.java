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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.io.WKTReader;

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