/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.LineStringExtracter;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class LinearBoundaryTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(LinearBoundaryTest.class);
  }
  
  public LinearBoundaryTest(String name) {
    super(name);
  }
  
  public void testLineMod2() {
    checkLinearBoundary("LINESTRING (0 0, 9 9)", 
        BoundaryNodeRule.MOD2_BOUNDARY_RULE,
        "MULTIPOINT((0 0), (9 9))");   
  }

  public void testLines2Mod2() {
    checkLinearBoundary("MULTILINESTRING ((0 0, 9 9), (9 9, 5 1))", 
        BoundaryNodeRule.MOD2_BOUNDARY_RULE,
        "MULTIPOINT((0 0), (5 1))");   
  }

  public void testLines3Mod2() {
    checkLinearBoundary("MULTILINESTRING ((0 0, 9 9), (9 9, 5 1), (9 9, 1 5))", 
        BoundaryNodeRule.MOD2_BOUNDARY_RULE,
        "MULTIPOINT((0 0), (5 1), (1 5), (9 9))");   
  }

  public void testLines3Monvalent() {
    checkLinearBoundary("MULTILINESTRING ((0 0, 9 9), (9 9, 5 1), (9 9, 1 5))", 
        BoundaryNodeRule.MONOVALENT_ENDPOINT_BOUNDARY_RULE,
        "MULTIPOINT((0 0), (5 1), (1 5))");   
  }

  private void checkLinearBoundary(String wkt, BoundaryNodeRule bnr, String wktBdyExpected) {
    Geometry geom = read(wkt);
    LinearBoundary lb = new LinearBoundary(extractLines(geom), bnr);
    boolean hasBoundaryExpected = wktBdyExpected == null ? false : true;
    assertEquals("HasBoundary", hasBoundaryExpected, lb.hasBoundary());
    
    checkBoundaryPoints(lb, geom, wktBdyExpected);
  }

  private void checkBoundaryPoints(LinearBoundary lb, Geometry geom, String wktBdyExpected) {
    Set<Coordinate> bdySet = extractPoints(wktBdyExpected);
    
    for (Coordinate p : bdySet) {
      assertTrue(lb.isBoundary(p));
    }
    
    Coordinate[] allPts = geom.getCoordinates();
    for (Coordinate p : allPts) {
      if (! bdySet.contains(p)) {
        assertFalse(lb.isBoundary(p));
      }
    }
  }

  private Set<Coordinate> extractPoints(String wkt) {
    Set<Coordinate> ptSet = new HashSet<Coordinate>();
    if (wkt == null) return ptSet;
    Coordinate[] pts = read(wkt).getCoordinates();
    for (Coordinate p : pts) {
      ptSet.add(p);
    }
    return ptSet;
  }

  private List<LineString> extractLines(Geometry geom) {
    return LineStringExtracter.getLines(geom);
  }
}
