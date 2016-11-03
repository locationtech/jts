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
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.PrecisionModel;

import junit.framework.TestCase;


/**
 * Test IntersectionSegment#compareNodePosition using an exhaustive set
 * of test cases
 *
 * @version 1.7
 */
public class SegmentPointComparatorFullTest
 extends TestCase
{

  private PrecisionModel pm = new PrecisionModel(1.0);

  public SegmentPointComparatorFullTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(SegmentPointComparatorFullTest.class);
  }

  public void testQuadrant0()
  {
    checkSegment(100, 0);
    checkSegment(100, 50);
    checkSegment(100, 100);
    checkSegment(100, 150);
    checkSegment(0, 100);
  }

  public void testQuadrant4()
  {
    checkSegment(100, -50);
    checkSegment(100, -100);
    checkSegment(100, -150);
    checkSegment(0, -100);
  }

  public void testQuadrant1()
  {
    checkSegment(-100, 0);
    checkSegment(-100, 50);
    checkSegment(-100, 100);
    checkSegment(-100, 150);
  }

  public void testQuadrant2()
  {
    checkSegment(-100, 0);
    checkSegment(-100, -50);
    checkSegment(-100, -100);
    checkSegment(-100, -150);
  }

  private void checkSegment(double x, double y)
  {
    Coordinate seg0 = new Coordinate(0, 0);
    Coordinate seg1 = new Coordinate(x, y);
    LineSegment seg = new LineSegment(seg0, seg1);

    for (int i = 0; i < 4; i++) {
      double dist = i;

      double gridSize = 1 / pm.getScale();

      checkPointsAtDistance(seg, dist, dist + 1.0 * gridSize);
      checkPointsAtDistance(seg, dist, dist + 2.0 * gridSize);
      checkPointsAtDistance(seg, dist, dist + 3.0 * gridSize);
      checkPointsAtDistance(seg, dist, dist + 4.0 * gridSize);
    }
  }

  private Coordinate computePoint(LineSegment seg, double dist)
  {
    double dx = seg.p1.x - seg.p0.x;
    double dy = seg.p1.y - seg.p0.y;
    double len = seg.getLength();
    Coordinate pt = new Coordinate(dist * dx / len, dist * dy / len);
    pm.makePrecise(pt);
    return pt;
  }

  private void checkPointsAtDistance(LineSegment seg, double dist0, double dist1)
  {
    Coordinate p0 = computePoint(seg, dist0);
    Coordinate p1 = computePoint(seg, dist1);
    if (p0.equals(p1)) {
      checkNodePosition(seg, p0, p1, 0);
    }
    else {
      checkNodePosition(seg, p0, p1, -1);
      checkNodePosition(seg, p1, p0, 1);
    }
  }

  private void checkNodePosition(LineSegment seg, Coordinate p0, Coordinate p1, int expectedPositionValue)
  {
    int octant = Octant.octant(seg.p0, seg.p1);
    int posValue = SegmentPointComparator.compare(octant, p0, p1);
    //System.out.println(octant + " " + p0 + " " + p1 + " " + posValue);
    assertTrue(posValue == expectedPositionValue);
  }

}
