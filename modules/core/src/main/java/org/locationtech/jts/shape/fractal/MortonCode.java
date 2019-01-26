/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.shape.fractal;

import org.locationtech.jts.geom.Coordinate;

/**
 * Morton (Z) order encoding and decoding.
 * 
 * @author Martin Davis
 *
 * @see MortonCurveBuilder
 * @see HilbertCode
 */
public class MortonCode
{
  public static final int MAX_LEVEL = 16;
  
  public static int size(int level) {
    checkLevel(level);
    return (int) Math.pow(2, 2 *level);
  }
  
  public static int maxOrdinate(int level) {
    checkLevel(level);
    return (int) Math.pow(2, level) - 1;
  }
  
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
   * Computes the point on a Morton curve 
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
