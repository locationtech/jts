/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.noding;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;

/**
 * Test IntersectionSegment#compareNodePosition
 *
 * @version 1.7
 */
public class SegmentPointComparatorTest
 extends TestCase
{

  public SegmentPointComparatorTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(SegmentPointComparatorTest.class);
  }

  public void testOctant0()
  {
    checkNodePosition(0, 1, 1, 2, 2, -1);
    checkNodePosition(0, 1, 0, 1, 1, -1);
  }

  private void checkNodePosition(int octant,
      double x0, double y0,
    double x1, double y1,
    int expectedPositionValue
    )
  {
    int posValue = SegmentPointComparator.compare(octant,
        new Coordinate(x0, y0),
        new Coordinate(x1, y1)
        );
    assertTrue(posValue == expectedPositionValue);
  }
}
