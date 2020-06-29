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

package org.locationtech.jts.shape.fractal;

import org.locationtech.jts.geom.Coordinate;

/**
 * Encodes points as the index along the planar Morton (Z-order) curve.
 * <p>
 * The planar Morton (Z-order) curve is a continuous space-filling curve.
 * The Morton curve defines an ordering of the 
 * points in the positive quadrant of the plane.
 * The index of a point along the Morton curve is called the Morton code.
 * <p>
 * A sequence of subsets of the Morton curve can be defined by a level number.
 * Each level subset occupies a square range.
 * The curve at level n M<sub>n</sub> contains 2<sup>n + 1</sup> points. 
 * It fills the range square of side 2<sup>level</sup>. 
 * Curve points have ordinates in the range [0, 2<sup>level</sup> - 1].
 * The code for a given point is identical at all levels.
 * The level simply determines the number of points in the curve subset
 * and the size of the range square.
 * <p>
 * This implementation represents codes using 32-bit integers.  
 * This allows levels 0 to 16 to be handled.
 * The class supports encoding points
 * and decoding the point for a given code value.
 * <p>
 * The Morton order has the property that it tends to preserve locality.
 * This means that codes which are near in value will have spatially proximate
 * points.  The converse is not always true - the delta between 
 * codes for nearby points is not always small.  But the average delta 
 * is small enough that the Morton order is an effective way of linearizing space 
 * to support range queries. 
 * 
 * @author Martin Davis
 *
 * @see MortonCurveBuilder
 * @see HilbertCode
 */
public class MortonCode
{
  /**
   * The maximum curve level that can be represented.
   */
  public static final int MAX_LEVEL = 16;
  
  /**
   * The number of points in the curve for the given level.
   * The number of points is 2<sup>2 * level</sup>.
   * 
   * @param level the level of the curve
   * @return the number of points
   */
  public static int size(int level) {
    checkLevel(level);
    return (int) Math.pow(2, 2 *level);
  }
  
  /**
   * The maximum ordinate value for points 
   * in the curve for the given level.
   * The maximum ordinate is 2<sup>level</sup> - 1.
   * 
   * @param level the level of the curve
   * @return the maximum ordinate value
   */
  public static int maxOrdinate(int level) {
    checkLevel(level);
    return (int) Math.pow(2, level) - 1;
  }
  
  /**
   * The level of the finite Morton curve which contains at least 
   * the given number of points.
   * 
   * @param numPoints the number of points required
   * @return the level of the curve
   */
  public static int level(int numPoints) {
    int pow2 = (int) ( (Math.log(numPoints)/Math.log(2)));
    int level = pow2 / 2;
    int size = size(level);
    if (size < numPoints) level += 1;
    return level;
  }
  
  private static void checkLevel(int level) {
    if (level > MAX_LEVEL) {
      throw new IllegalArgumentException("Level must be in range 0 to " + MAX_LEVEL);
    }
  }
  /**
   * Computes the index of the point (x,y)
   * in the Morton curve ordering.
   * 
   * @param x the x ordinate of the point
   * @param y the y ordinate of the point
   * @return the index of the point along the Morton curve
   */
  public static int encode(int x, int y) {
    return (interleave(y) << 1) + interleave(x);
  }
  
  private static int interleave(int x) {
    x &= 0x0000ffff;                  // x = ---- ---- ---- ---- fedc ba98 7654 3210
    x = (x ^ (x << 8)) & 0x00ff00ff; // x = ---- ---- fedc ba98 ---- ---- 7654 3210
    x = (x ^ (x << 4)) & 0x0f0f0f0f; // x = ---- fedc ---- ba98 ---- 7654 ---- 3210
    x = (x ^ (x << 2)) & 0x33333333; // x = --fe --dc --ba --98 --76 --54 --32 --10
    x = (x ^ (x << 1)) & 0x55555555; // x = -f-e -d-c -b-a -9-8 -7-6 -5-4 -3-2 -1-0
    return x;
  }

  /**
   * Computes the point on the Morton curve 
   * for a given index.
   * 
   * @param index the index of the point on the curve
   * @return the point on the curve
   */
  public static Coordinate decode(int index) {
    long x = deinterleave(index);
    long y = deinterleave(index >> 1);
    return new Coordinate(x, y);
  }

  private static long deinterleave(int x) {
    x = x & 0x55555555;
    x = (x | (x >> 1)) & 0x33333333;
    x = (x | (x >> 2)) & 0x0F0F0F0F;
    x = (x | (x >> 4)) & 0x00FF00FF;
    x = (x | (x >> 8)) & 0x0000FFFF;
    return x;
  }
}
