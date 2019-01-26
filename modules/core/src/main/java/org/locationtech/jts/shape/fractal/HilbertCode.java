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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.shape.GeometricShapeBuilder;

/**
 * Hilbert order encoding and decoding.
 * The Hilbert Curve is a continuous space-filling curve.
 * In the limit the Hilbert curve has infinitely many vertices and fills 
 * the space of the unit square.
 * There are a set of discrete approximations, determined by
 * a level number.
 * The discrete Hilbert curve at level n H<sub>n</sub> has 2<sup>n+1</sup> vertices. 
 * The curve occupies a square of side 2<sup>n</sup>.
 * <p>
 * The discrete Hilbert curve induces an ordering of the 
 * vertices along the curve.
 * The index of a vertex along the curve is called the Hilbert code.
 * This class supports encoding points into the corresponding code,
 * and computing the point for a given code value.
 * Codes are represented as 32-bit integers, which allows levels
 * 0 to 16 to be represented.
 * <p>
 * The Hilbert order has the property that it tends to preserve locality.
 * This means that codes which are near in value will have spatially proximate
 * points.  The converse is not always true, but does occur often 
 * enough that the Hilbert order is an effective way of linearizing space 
 * to support range queries. 
 * 
 * @author Martin Davis
 *
 * @see HilbertCurveBuilder
 * @see MortonCode
 */
public class HilbertCode
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
   * in the Hilbert curve at the given level.
   * 
   * @param level the level of the discrete Hilbert curve
   * @param x the x ordinate of the point
   * @param y the y ordinate of the point
   * @return the index of the point along the Hilbert curve
   */
  public static int encode(int level, int x, int y) {
    // Fast Hilbert curve algorithm by http://threadlocalmutex.com/
    // Ported from C++ https://github.com/rawrunprotected/hilbert_curves (public
    // domain)

    int lvl = levelClamp(level);
    
    x = x << (16 - lvl);
    y = y << (16 - lvl);
    
    long a = x ^ y;
    long b = 0xFFFF ^ a;
    long c = 0xFFFF ^ (x | y);
    long d = x & (y ^ 0xFFFF);

    long A = a | (b >> 1);
    long B = (a >> 1) ^ a;
    long C = ((c >> 1) ^ (b & (d >> 1))) ^ c;
    long D = ((a & (c >> 1)) ^ (d >> 1)) ^ d;

    a = A;
    b = B;
    c = C;
    d = D;
    A = ((a & (a >> 2)) ^ (b & (b >> 2)));
    B = ((a & (b >> 2)) ^ (b & ((a ^ b) >> 2)));
    C ^= ((a & (c >> 2)) ^ (b & (d >> 2)));
    D ^= ((b & (c >> 2)) ^ ((a ^ b) & (d >> 2)));

    a = A;
    b = B;
    c = C;
    d = D;
    A = ((a & (a >> 4)) ^ (b & (b >> 4)));
    B = ((a & (b >> 4)) ^ (b & ((a ^ b) >> 4)));
    C ^= ((a & (c >> 4)) ^ (b & (d >> 4)));
    D ^= ((b & (c >> 4)) ^ ((a ^ b) & (d >> 4)));

    a = A;
    b = B;
    c = C;
    d = D;
    C ^= ((a & (c >> 8)) ^ (b & (d >> 8)));
    D ^= ((b & (c >> 8)) ^ ((a ^ b) & (d >> 8)));

    a = C ^ (C >> 1);
    b = D ^ (D >> 1);

    long i0 = x ^ y;
    long i1 = b | (0xFFFF ^ (i0 | a));

    i0 = (i0 | (i0 << 8)) & 0x00FF00FF;
    i0 = (i0 | (i0 << 4)) & 0x0F0F0F0F;
    i0 = (i0 | (i0 << 2)) & 0x33333333;
    i0 = (i0 | (i0 << 1)) & 0x55555555;

    i1 = (i1 | (i1 << 8)) & 0x00FF00FF;
    i1 = (i1 | (i1 << 4)) & 0x0F0F0F0F;
    i1 = (i1 | (i1 << 2)) & 0x33333333;
    i1 = (i1 | (i1 << 1)) & 0x55555555;

    long index = ((i1 << 1) | i0) >> (32 - 2 * lvl);
    return (int) index;
  }

  /**
   * Clamps a level to the range valid for 
   * the index algorithm used.
   * 
   * @param level the level of a Hilbert curve
   * @return a valid level
   */
  private static int levelClamp(int level) {
    // clamp order to [1, 16]
    int lvl = level < 1 ? 1 : level;
    lvl = lvl > MAX_LEVEL ? MAX_LEVEL : lvl;
    return lvl;
  }
  
  /**
   * Computes the point on a Hilbert curve 
   * of given level for a given code.
   * 
   * @param level the Hilbert curve level
   * @param i the index of the point on the curve
   * @return the point on the Hilbert curve
   */
  public static Coordinate decode(int level, int i) {
    checkLevel(level);
    int lvl = levelClamp(level);
    
    i = i << (32 - 2 * lvl);

    long i0 = deinterleave(i);
    long i1 = deinterleave(i >> 1);

    long t0 = (i0 | i1) ^ 0xFFFF;
    long t1 = i0 & i1;

    long prefixT0 = prefixScan(t0);
    long prefixT1 = prefixScan(t1);

    long a = (((i0 ^ 0xFFFF) & prefixT1) | (i0 & prefixT0));

    long x = (a ^ i1) >> (16 - lvl);
    long y = (a ^ i0 ^ i1) >> (16 - lvl);
    
    return new Coordinate(x, y);
  }

  private static long prefixScan(long x) {
    x = (x >> 8) ^ x;
    x = (x >> 4) ^ x;
    x = (x >> 2) ^ x;
    x = (x >> 1) ^ x;
    return x;
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
