package com.vividsolutions.jts.util;

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
