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

package com.vividsolutions.jts.hull;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

import com.vividsolutions.jts.geom.Geometry;

public class ConcaveHullTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(ConcaveHullTest.class);
  }
  
  public ConcaveHullTest(String name) {
    super(name);
  }

  public void testSimple() {
    checkHull(
        "POLYGON ((100 200, 200 180, 300 200, 200 190, 100 200))",
        150,
        "POLYGON ((100 200, 200 180, 300 200, 200 190, 100 200))"
        );
  }
  
  private void checkHull(String inputWKT, double tolerance, String expectedWKT) {
    Geometry input = read(inputWKT);
    Geometry expected = read(expectedWKT);
    ConcaveHull hull = new ConcaveHull(input, tolerance);
    Geometry actual = hull.getResult();
    checkEqual(expected, actual);
  }
}
