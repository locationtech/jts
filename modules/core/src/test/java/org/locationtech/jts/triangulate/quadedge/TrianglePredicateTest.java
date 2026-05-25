/*
 * Copyright (c) 2026 Darafei Praliaskouski.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.triangulate.quadedge;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.geom.Coordinate;

/**
 * Tests for triangle predicates.
 */
public class TrianglePredicateTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(TrianglePredicateTest.class);
  }

  public TrianglePredicateTest(String name) {
    super(name);
  }

  public void testAdaptiveInCircleClearCases() {
    Coordinate a = new Coordinate(0, 0);
    Coordinate b = new Coordinate(10, 0);
    Coordinate c = new Coordinate(0, 10);

    assertTrue(TrianglePredicate.isInCircleRobust(a, b, c, new Coordinate(1, 1)));
    assertFalse(TrianglePredicate.isInCircleRobust(a, b, c, new Coordinate(20, 20)));
  }

  public void testAdaptiveInCircleFallbackUsesDDComputation() {
    Coordinate a = new Coordinate(100000000000000.19, 100000000000000.53);
    Coordinate b = new Coordinate(100000000000000.19, 100000000000000.47);
    Coordinate c = new Coordinate(100000000000000.22, 100000000000000.38);
    Coordinate p = new Coordinate(100000000000000.40, 100000000000000.75);

    assertTrue(TrianglePredicate.isInCircleDDFast(a, b, c, p));
    assertTrue(TrianglePredicate.isInCircleRobust(a, b, c, p));
  }
}
