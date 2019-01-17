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
 * Generates Hilbert Curves.
 * 
 * @author Martin Davis
 *
 */
public class HilbertCurveBuilder
extends GeometricShapeBuilder
{
  private int order = -1;

  /**
   * Creates a new instance using the provided {@link GeometryFactory}.
   * 
   * @param geomFactory the geometry factory to use
   */
  public HilbertCurveBuilder(GeometryFactory geomFactory)
  {
    super(geomFactory);
  }

  /**
   * Sets the order of curve to generate.
   * The order should be in the range [0 - 1].
   * 
   * @param order the order of the curve
   */
  public void setOrder(int order) {
    this.order = orderClamp(order);
  }
  
  @Override
  public Geometry getGeometry() {
    int numOnSide = 1;
    if (order < 0) {
     //TODO:  compute order from numPts
    }
    else {
      numOnSide = (int) Math.pow(2, order);
      numPts = numOnSide * numOnSide;
    }
    LineSegment baseLine = getSquareBaseLine();
    double width = baseLine.getLength();
    double scale = width / (numOnSide - 1);
    
    Coordinate[] pts = new Coordinate[numPts];
    for (int i = 0; i < numPts; i++) {
       Coordinate pt = hilbertPoint(order, i);
       double x = scale(pt.getX(), scale, baseLine.minX());
       double y = scale(pt.getY(), scale, baseLine.minY());
       pts[i] = new Coordinate(x, y);
    }
    return geomFactory.createLineString(pts);
  }
  
  private double scale(double val, double scale, double base) {
    return val * scale + base;
  }
  
  /**
   * Computes the index of the point (x,y)
   * in the Hilbert curve of the given order.
   * 
   * @param order the Hilbert curve order
   * @param x the x ordinate of the point
   * @param y the y ordinate of the point
   * @return the index of the point along the Hilbert curve
   */
  public static int hilbertIndex(int order, int x, int y) {
    // Fast Hilbert curve algorithm by http://threadlocalmutex.com/
    // Ported from C++ https://github.com/rawrunprotected/hilbert_curves (public
    // domain)

    int ord = orderClamp(order);
    
    x = x << (16 - ord);
    y = y << (16 - ord);
    
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

    long index = ((i1 << 1) | i0) >> (32 - 2 * ord);
    return (int) index;
  }

  /**
   * Clamps an order to the range valid for 
   * the index algorithm used.
   * 
   * @param order the order of a Hilbert curve
   * @return a valid order
   */
  private static int orderClamp(int order) {
    // clamp order to [1, 16]
    int ord = order < 1 ? 1 : order;
    ord = ord > 16 ? 16 : ord;
    return ord;
  }
  
  /**
   * Computes the point on a Hilbert curve 
   * of given order for a given index.
   * 
   * @param order the Hilbert curve order
   * @param i the index of the point on the curve
   * @return the point on the Hilbert curve
   */
  public static Coordinate hilbertPoint(int order, int i) {
    // Fast Hilbert curve algorithm by http://threadlocalmutex.com/
    // Ported from C++ https://github.com/rawrunprotected/hilbert_curves (public
    // domain)
    
    int ord = orderClamp(order);
    
    i = i << (32 - 2 * ord);

    long i0 = deinterleave(i);
    long i1 = deinterleave(i >> 1);

    long t0 = (i0 | i1) ^ 0xFFFF;
    long t1 = i0 & i1;

    long prefixT0 = prefixScan(t0);
    long prefixT1 = prefixScan(t1);

    long a = (((i0 ^ 0xFFFF) & prefixT1) | (i0 & prefixT0));

    long x = (a ^ i1) >> (16 - ord);
    long y = (a ^ i0 ^ i1) >> (16 - ord);
    
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