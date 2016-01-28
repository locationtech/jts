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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests CGAlgorithms.isCCW
 * @version 1.7
 */
public class IsCCWTest extends TestCase {

  private WKTReader reader = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(IsCCWTest.class);
  }

  public IsCCWTest(String name) { super(name); }

  public void testCCW() throws Exception
  {
    Coordinate[] pts = getCoordinates("POLYGON ((60 180, 140 240, 140 240, 140 240, 200 180, 120 120, 60 180))");
    assertEquals(CGAlgorithms.isCCW(pts), false);

    Coordinate[] pts2 = getCoordinates("POLYGON ((60 180, 140 120, 100 180, 140 240, 60 180))");
    assertEquals(CGAlgorithms.isCCW(pts2), true);
    // same pts list with duplicate top point - check that isCCW still works
    Coordinate[] pts2x = getCoordinates("POLYGON ((60 180, 140 120, 100 180, 140 240, 140 240, 60 180))");
    assertEquals(CGAlgorithms.isCCW(pts2x), true);
  }

  private Coordinate[] getCoordinates(String wkt)
      throws ParseException
  {
    Geometry geom = reader.read(wkt);
    return geom.getCoordinates();
  }
}