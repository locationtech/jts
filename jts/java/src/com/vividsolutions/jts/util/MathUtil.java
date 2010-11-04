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
  
  public static double log10(double x)
  {
    return Math.log(x)/ LOG_10;
  }
  

}
