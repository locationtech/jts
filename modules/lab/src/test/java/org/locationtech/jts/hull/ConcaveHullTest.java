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
package org.locationtech.jts.hull;

import junit.textui.TestRunner;

import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

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
    //checkEqual(expected, actual);
  }
}
