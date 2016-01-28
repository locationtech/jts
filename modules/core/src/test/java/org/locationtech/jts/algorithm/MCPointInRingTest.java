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

import junit.textui.TestRunner;

/**
 * Tests PointInRing algorithms
 *
 * @version 1.7
 */
public class MCPointInRingTest extends AbstractPointInRingTest {

  private WKTReader reader = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(PointInRingTest.class);
  }

  public MCPointInRingTest(String name) { super(name); }


   protected void runPtInRing(int expectedLoc, Coordinate pt, String wkt)
      throws Exception
  {
  	 // isPointInRing is not defined for pts on boundary
  	 if (expectedLoc == Location.BOUNDARY)
  		 return;
  	 
    Geometry geom = reader.read(wkt);
    if (! (geom instanceof Polygon))
    	return;
    
    LinearRing ring = (LinearRing) ((Polygon) geom).getExteriorRing();
    boolean expected = expectedLoc == Location.INTERIOR;
    MCPointInRing pir = new MCPointInRing(ring);
    boolean result = pir.isInside(pt);
    assertEquals(expected, result);
  }

}