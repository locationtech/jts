/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.math;

/**
 * Various utility functions for mathematical and numerical operations.
 * 
 * @author mbdavis
 *
 */
public class MathUtil 
{
	/**
	 * Clamps a value to a given range.
	 * @param x the value to clamp
	 * @param min the minimum value of the range
	 * @param max the maximum value of the range
	 * @return the clamped value
	 */
  public static double clamp(double x, double min, double max)
  {
  	if (x < min) return min;
  	if (x > max) return max;
  	return x;
  }
  
  private static final double LOG_10 = Math.log(10);
  
  /**
   * Computes the base-10 logarithm of a <tt>double</tt> value.
   * <ul>
   * <li>If the argument is NaN or less than zero, then the result is NaN.
   * <li>If the argument is positive infinity, then the result is positive infinity.
   * <li>If the argument is positive zero or negative zero, then the result is negative infinity.
   * </ul>
   *   
   * @param x a positive number
   * @return the value log a, the base-10 logarithm of the input value
   */
  public static double log10(double x)
  {
    double ln = Math.log(x);
    if (Double.isInfinite(ln)) return ln;
    if (Double.isNaN(ln)) return ln;
    return ln / LOG_10;
  }
  

}
