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
import org.locationtech.jts.util.Assert;

/**
 * Implements a robust method of comparing the relative position of two
 * points along the same segment.
 * The coordinates are assumed to lie "near" the segment.
 * This means that this algorithm will only return correct results
 * if the input coordinates
 * have the same precision and correspond to rounded values
 * of exact coordinates lying on the segment.
 *
 * @version 1.7
 */
public class SegmentPointComparator {

  /**
   * Compares two {@link Coordinate}s for their relative position along a segment
   * lying in the specified {@link Octant}.
   *
   * @return -1 node0 occurs first;
   * 0 the two nodes are equal;
   * 1 node1 occurs first
   */
  public static int compare(int octant, Coordinate p0, Coordinate p1)
  {
    // nodes can only be equal if their coordinates are equal
    if (p0.equals2D(p1)) return 0;

    int xSign = relativeSign(p0.x, p1.x);
    int ySign = relativeSign(p0.y, p1.y);

    switch (octant) {
      case 0: return compareValue(xSign, ySign);
      case 1: return compareValue(ySign, xSign);
      case 2: return compareValue(ySign, -xSign);
      case 3: return compareValue(-xSign, ySign);
      case 4: return compareValue(-xSign, -ySign);
      case 5: return compareValue(-ySign, -xSign);
      case 6: return compareValue(-ySign, xSign);
      case 7: return compareValue(xSign, -ySign);
    }
    Assert.shouldNeverReachHere("invalid octant value");
    return 0;
  }

  public static int relativeSign(double x0, double x1)
  {
    if (x0 < x1) return -1;
    if (x0 > x1) return 1;
    return 0;
  }

  private static int compareValue(int compareSign0, int compareSign1)
  {
    if (compareSign0 < 0) return -1;
    if (compareSign0 > 0) return 1;
    if (compareSign1 < 0) return -1;
    if (compareSign1 > 0) return 1;
    return 0;

  }
}
