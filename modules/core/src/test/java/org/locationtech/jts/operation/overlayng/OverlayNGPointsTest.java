/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlayNGPointsTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(OverlayNGPointsTest.class);
  }

  public OverlayNGPointsTest(String name) { super(name); }
  
  public void testSimpleIntersection() {
    Geometry a = read("MULTIPOINT ((1 1), (2 1))");
    Geometry b = read("POINT (2 1)");
    Geometry expected = read("POINT (2 1)");
    checkEqual(expected, OverlayNGTest.intersection(a, b, 1));
  }
  
  public void testSimpleMergeIntersection() {
    Geometry a = read("MULTIPOINT ((1 1), (1.5 1.1), (2 1), (2.1 1.1))");
    Geometry b = read("POINT (2 1)");
    Geometry expected = read("POINT (2 1)");
    checkEqual(expected, OverlayNGTest.intersection(a, b, 1));
  }
  
  public void testSimpleUnion() {
    Geometry a = read("MULTIPOINT ((1 1), (2 1))");
    Geometry b = read("POINT (2 1)");
    Geometry expected = read("MULTIPOINT ((1 1), (2 1))");
    checkEqual(expected, OverlayNGTest.union(a, b, 1));
  }
  
  public void testSimpleDifference() {
    Geometry a = read("MULTIPOINT ((1 1), (2 1))");
    Geometry b = read("POINT (2 1)");
    Geometry expected = read("POINT (1 1)");
    checkEqual(expected, OverlayNGTest.difference(a, b, 1));
  }
  
  public void testSimpleSymDifference() {
    Geometry a = read("MULTIPOINT ((1 2), (1 1), (2 2), (2 1))");
    Geometry b = read("MULTIPOINT ((2 2), (2 1), (3 2), (3 1))");
    Geometry expected = read("MULTIPOINT ((1 2), (1 1), (3 2), (3 1))");
    checkEqual(expected, OverlayNGTest.symDifference(a, b, 1));
  }
  
  public void testSimpleFloatUnion() {
    Geometry a = read("MULTIPOINT ((1 1), (1.5 1.1), (2 1), (2.1 1.1))");
    Geometry b = read("MULTIPOINT ((1.5 1.1), (2 1), (2 1.2))");
    Geometry expected = read("MULTIPOINT ((1 1), (1.5 1.1), (2 1), (2 1.2), (2.1 1.1))");
    checkEqual(expected, OverlayNGTest.union(a, b));
  }
  
  public void testDisjointPointsRoundedIntersection() {
    Geometry a = read("POINT (10.1 10)");
    Geometry b = read("POINT (10 10.1)");
    Geometry expected = read("POINT (10 10)");
    checkEqual(expected, OverlayNGTest.intersection(a, b, 1));
  }
  
  public void testEmptyIntersection() {
    Geometry a = read("MULTIPOINT ((1 1), (3 1))");
    Geometry b = read("POINT (2 1)");
    Geometry expected = read("POINT EMPTY");
    checkEqual(expected, OverlayNGTest.intersection(a, b, 1));
  }
  
  public void testEmptyInputIntersection() {
    Geometry a = read("MULTIPOINT ((1 1), (3 1))");
    Geometry b = read("POINT EMPTY");
    Geometry expected = read("POINT EMPTY");
    checkEqual(expected, OverlayNGTest.intersection(a, b, 1));
  }
  
  public void testEmptyInputUUnion() {
    Geometry a = read("MULTIPOINT ((1 1), (3 1))");
    Geometry b = read("POINT EMPTY");
    Geometry expected = read("MULTIPOINT ((1 1), (3 1))");
    checkEqual(expected, OverlayNGTest.union(a, b, 1));
  }
  
  public void testEmptyDifference() {
    Geometry a = read("MULTIPOINT ((1 1), (3 1))");
    Geometry b = read("MULTIPOINT ((1 1), (2 1), (3 1))");
    Geometry expected = read("POINT EMPTY");
    checkEqual(expected, OverlayNGTest.difference(a, b, 1));
  }
}
