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
package org.locationtech.jts.noding;

import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests {@link OrientedCoordinateArray#equals(Object)} and
 * {@link OrientedCoordinateArray#hashCode()} for consistency with
 * {@link OrientedCoordinateArray#compareTo(Object)} (JTS #1184), including
 * the orientation-independence the class exists to provide.
 */
public class OrientedCoordinateArrayTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(OrientedCoordinateArrayTest.class);
  }

  public OrientedCoordinateArrayTest(String name) { super(name); }

  private static Coordinate[] coords(double... xy) {
    Coordinate[] pts = new Coordinate[xy.length / 2];
    for (int i = 0; i < pts.length; i++)
      pts[i] = new Coordinate(xy[2 * i], xy[2 * i + 1]);
    return pts;
  }

  private static Coordinate[] reverse(Coordinate[] pts) {
    Coordinate[] r = new Coordinate[pts.length];
    for (int i = 0; i < pts.length; i++)
      r[i] = pts[pts.length - 1 - i];
    return r;
  }

  public void testEqualsValuesConsistentWithCompareTo() {
    Coordinate[] pts = coords(0, 0, 1, 1, 2, 0);
    OrientedCoordinateArray a = new OrientedCoordinateArray(pts);
    OrientedCoordinateArray b = new OrientedCoordinateArray(coords(0, 0, 1, 1, 2, 0));
    assertTrue(a.compareTo(b) == 0);
    assertEquals(a, b);
  }

  public void testEqualsHashCodeContract() {
    OrientedCoordinateArray a = new OrientedCoordinateArray(coords(0, 0, 1, 1, 2, 0));
    OrientedCoordinateArray b = new OrientedCoordinateArray(coords(0, 0, 1, 1, 2, 0));
    assertEquals(a.hashCode(), b.hashCode());
    Set<OrientedCoordinateArray> set = new HashSet<OrientedCoordinateArray>();
    set.add(a);
    set.add(b);
    assertEquals(1, set.size());
  }

  public void testEqualsHashCodeOrientationIndependent() {
    // an asymmetric array, so a stored-order hash would differ from its reverse
    Coordinate[] pts = coords(0, 0, 5, 1, 2, 7, 9, 3);
    OrientedCoordinateArray fwd = new OrientedCoordinateArray(pts);
    OrientedCoordinateArray rev = new OrientedCoordinateArray(reverse(pts));
    // the class compares orientation-independently, so these are equal ...
    assertTrue(fwd.compareTo(rev) == 0);
    assertEquals(fwd, rev);
    // ... therefore their hash codes must agree, and they must dedup
    assertEquals(fwd.hashCode(), rev.hashCode());
    Set<OrientedCoordinateArray> set = new HashSet<OrientedCoordinateArray>();
    set.add(fwd);
    set.add(rev);
    assertEquals(1, set.size());
  }
}
