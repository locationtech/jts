/*
 * Copyright (c) 2026 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geomgraph;

import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests {@link EdgeIntersection#equals(Object)} and {@link EdgeIntersection#hashCode()}
 * for consistency with {@link EdgeIntersection#compareTo(Object)} (JTS #1184).
 */
public class EdgeIntersectionTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(EdgeIntersectionTest.class);
  }

  public EdgeIntersectionTest(String name) { super(name); }

  public void testEqualsValuesConsistentWithCompareTo() {
    EdgeIntersection ei = new EdgeIntersection(new Coordinate(1, 2), 3, 0.5);
    EdgeIntersection eiSame = new EdgeIntersection(new Coordinate(1, 2), 3, 0.5);
    assertTrue(ei.compareTo(eiSame) == 0);
    assertEquals(ei, eiSame);
  }

  public void testEqualsHashCodeContract() {
    EdgeIntersection ei = new EdgeIntersection(new Coordinate(1, 2), 3, 0.5);
    EdgeIntersection eiSame = new EdgeIntersection(new Coordinate(1, 2), 3, 0.5);
    assertEquals(ei.hashCode(), eiSame.hashCode());
    Set<EdgeIntersection> set = new HashSet<EdgeIntersection>();
    set.add(ei);
    set.add(eiSame);
    assertEquals(1, set.size());
  }

  public void testEqualsHashCodeConsistentWithNaN() {
    EdgeIntersection zero = new EdgeIntersection(new Coordinate(1, 2), 0, 0.0);
    EdgeIntersection nan = new EdgeIntersection(new Coordinate(1, 2), 0, Double.NaN);
    EdgeIntersection half = new EdgeIntersection(new Coordinate(1, 2), 0, 0.5);

    // NaN must only be equal to itself, not to arbitrary other values (transitivity)
    assertFalse(zero.equals(nan));
    assertFalse(nan.equals(half));
    assertFalse(zero.equals(half));
    assertEquals(nan, new EdgeIntersection(new Coordinate(1, 2), 0, Double.NaN));

    // equals/hashCode contract must hold even when NaN is involved
    assertEquals(nan.hashCode(), new EdgeIntersection(new Coordinate(1, 2), 0, Double.NaN).hashCode());
  }
}
