/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import java.util.BitSet;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiLineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class TPVWSimplifierTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(TPVWSimplifierTest.class);
  }
  
  public TPVWSimplifierTest(String name) {
    super(name);
  }
  
  public void testSimpleNoop() {
    checkNoop("MULTILINESTRING ((9 9, 3 9, 1 4, 4 1, 9 1), (9 1, 2 4, 9 9))", 
        2);
  }
    
  public void testSimple() {
    checkSimplify("MULTILINESTRING ((9 9, 3 9, 1 4, 4 1, 9 1), (9 1, 6 3, 2 4, 5 7, 9 9))", 
        2, 
        "MULTILINESTRING ((9 9, 3 9, 1 4, 4 1, 9 1), (9 1, 2 4, 9 9))");
  }
    
  public void testFreeRing() {
    checkSimplify("MULTILINESTRING ((1 9, 9 9, 9 1), (1 9, 1 1, 9 1), (7 5, 8 8, 2 8, 2 2, 8 2, 7 5))", 
        new int[] { 2 },
        2, 
        "MULTILINESTRING ((1 9, 1 1, 9 1), (1 9, 9 9, 9 1), (8 8, 2 8, 2 2, 8 2, 8 8))");
  }
    
  public void testNoFreeRing() {
    checkSimplify("MULTILINESTRING ((1 9, 9 9, 9 1), (1 9, 1 1, 9 1), (5 5, 4 8, 2 8, 2 2, 4 2, 5 5), (5 5, 6 8, 8 8, 8 2, 6 2, 5 5))", 
        new int[] {  },
        2, 
        "MULTILINESTRING ((1 9, 1 1, 9 1), (1 9, 9 9, 9 1), (5 5, 2 2, 2 8, 5 5), (5 5, 8 2, 8 8, 5 5))");
  }
    
  public void testConstraint() {
    checkSimplify("MULTILINESTRING ((6 8, 2 8, 2.1 5, 2 2, 6 2, 5.9 5, 6 8))", 
        new int[] {  },
        "MULTILINESTRING ((1 9, 9 9, 6 5, 9 1), (1 9, 1 1, 9 1))",
        1, 
        "MULTILINESTRING ((6 8, 2 8, 2 2, 6 2, 5.9 5, 6 8))");
  }
    
  private void checkNoop(String wkt, double tolerance) {
    MultiLineString geom = (MultiLineString) read(wkt);
    Geometry actual = TPVWSimplifier.simplify(geom, tolerance);
    checkEqual(geom, actual);
  }
  
  private void checkSimplify(String wkt, double tolerance, String wktExpected) {
    MultiLineString geom = (MultiLineString) read(wkt);
    Geometry actual = TPVWSimplifier.simplify(geom, tolerance);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
  private void checkSimplify(String wkt, int[] freeRingIndex, 
      double tolerance, String wktExpected) {
    checkSimplify(wkt, freeRingIndex, null, tolerance, wktExpected);
  }
  
  private void checkSimplify(String wkt, int[] freeRingIndex, 
      String wktConstraints,
      double tolerance, String wktExpected) {
    MultiLineString lines = (MultiLineString) read(wkt);
    BitSet freeRings = new BitSet();
    for (int index : freeRingIndex) {
      freeRings.set(index);
    }
    MultiLineString constraints = wktConstraints == null ? null
      : (MultiLineString) read(wktConstraints);
    Geometry actual = TPVWSimplifier.simplify(lines, freeRings, constraints, tolerance);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
}
