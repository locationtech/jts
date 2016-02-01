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
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;

/**
 * Tests PointInRing algorithms
 *
 * @version 1.7
 */
public class LocatePointInRingTest extends AbstractPointInRingTest {

  private WKTReader reader = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(LocatePointInRingTest.class);
  }

  public LocatePointInRingTest(String name) { super(name); }

  protected void runPtInRing(int expectedLoc, Coordinate pt, String wkt)
      throws Exception
  {
    Geometry geom = reader.read(wkt);
    assertEquals(expectedLoc, CGAlgorithms.locatePointInRing(pt, geom.getCoordinates()));
  }

}