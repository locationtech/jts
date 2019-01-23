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
 * Morton (Z) Curve encoding.
 * 
 * @author Martin Davis
 *
 */
public class MortonCurve
{
  public static final int MAX_ORDER = 16;
  
  public static int size(int order) {
    checkOrder(order);
    return (int) Math.pow(2, 2 *order);
  }
  
  public static int maxOrdinate(int order) {
    checkOrder(order);
    return (int) Math.pow(2, order) - 1;
  }
  
  private static void checkOrder(int order) {
    if (order > MAX_ORDER) {
      throw new IllegalArgumentException("Order must be in range 0 to " + MAX_ORDER);
    }
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
