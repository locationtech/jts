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
package org.locationtech.jts.operation.relateng;

import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests {@link NodeSection#equals(Object)} and {@link NodeSection#hashCode()}
 * for consistency with {@link NodeSection#compareTo(NodeSection)} (JTS #1184).
 */
public class NodeSectionTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(NodeSectionTest.class);
  }

  public NodeSectionTest(String name) { super(name); }

  private static NodeSection section(int id, Coordinate v0, Coordinate v1) {
    Coordinate nodePt = new Coordinate(5, 5);
    return new NodeSection(true, Dimension.A, id, 0, null, false, v0, nodePt, v1);
  }

  public void testEqualsValuesConsistentWithCompareTo() {
    NodeSection ns = section(1, new Coordinate(0, 0), new Coordinate(10, 10));
    NodeSection nsSame = section(1, new Coordinate(0, 0), new Coordinate(10, 10));
    assertTrue(ns.compareTo(nsSame) == 0);
    assertEquals(ns, nsSame);
  }

  public void testEqualsHashCodeContract() {
    NodeSection ns = section(1, new Coordinate(0, 0), new Coordinate(10, 10));
    NodeSection nsSame = section(1, new Coordinate(0, 0), new Coordinate(10, 10));
    assertEquals(ns.hashCode(), nsSame.hashCode());
    Set<NodeSection> set = new HashSet<NodeSection>();
    set.add(ns);
    set.add(nsSame);
    assertEquals(1, set.size());
  }
}
