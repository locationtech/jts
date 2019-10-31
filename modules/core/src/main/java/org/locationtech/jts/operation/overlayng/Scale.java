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
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.OrdinateFormat;
import org.locationtech.jts.math.MathUtil;

/**
 * Functions for computing scale factors
 * for geometry operations, in particular operations 
 * using limited-precision noding (such as {@link OverlayNG}).
 * These functions can be used to automatically determine
 * appropriate scale factors for use in geometric operations.
 * 
 * @author mdavis
 *
 */
public class Scale 
{  
  /**
   * A number of digits of precision which leaves some computational "headroom"
   * for floating point operations.
   * 
   * This value should be less than the decimal precision of double-precision values (16).
   */
  public static int MAX_PRECISION_DIGITS = 14;
  
  /**
   * Computes a scale factor which maximizes 
   * the digits of precision but which is 
   * still safe to use for overlay operations.
   * If possible the inherent scale of the geometries
   * is returned, but if this 
   * 
   * @param a a geometry 
   * @param b a geometry
   * @return a scale factor for use in overlay operations
   */
  public static double autoScale(Geometry a, Geometry b) {
    double inherentScale = inherentScale(a, b);
    double safeScale = safeScale(a, b);
    
    /**
     * Use safe scale if lower, 
     * since it is important to preserve some precision for robustness
     */
    if (inherentScale <= safeScale ) {
      return inherentScale;
    }
    //System.out.println("Scale = " + scale);
    return safeScale;
  }
  
  /**
   * Computes a safe scale factor for a numeric value.
   * A safe scale factor ensures that rounded 
   * number has no more than {@link MAX_PRECISION_DIGITS} 
   * digits of precision.
   * 
   * @param value a numeric value
   * @return a safe scale factor for the value
   */
  public static double safeScale(double value)
  {
    return precisionScale(value, MAX_PRECISION_DIGITS);
  }
  
  /**
   * Computes a safe scale factor for a geometry.
   * A safe scale factor ensures that the rounded 
   * ordinates have no more than {@link MAX_PRECISION_DIGITS} 
   * digits of precision.
   * 
   * @param geom a geometry
   * @return a safe scale factor for the geometry ordinates
   */
  public static double safeScale(Geometry geom)
  {
    return safeScale( maxAbsBound( geom.getEnvelopeInternal() ));
  }
  
  /**
   * Computes a safe scale factor for two geometries.
   * A safe scale factor ensures that the rounded 
   * ordinates have no more than {@link MAX_PRECISION_DIGITS} 
   * digits of precision.
   * 
   * @param a a geometry
   * @param b a geometry
   * @return a safe scale factor for the geometry ordinates
   */
  public static double safeScale(Geometry a, Geometry b) {
    double maxBnd = maxAbsBound( a.getEnvelopeInternal());
    if (b != null) {
      double maxBndB = maxAbsBound( b.getEnvelopeInternal());
      maxBnd = Math.max(maxBnd,  maxBndB);
    }
    double scale = Scale.safeScale(maxBnd);
    return scale;
  }
  
  /**
   * Determines the maximum bound in absolute value
   * of an envelope.
   * This indicates the largest ordinate value
   * which must be accommodated by a scale factor.
   * 
   * @param env an envelope
   * @return the maximum bound in absolute value
   */
  private static double maxAbsBound(Envelope env) {
    return MathUtil.max(
        Math.abs(env.getMaxX()), 
        Math.abs(env.getMaxY()), 
        Math.abs(env.getMinX()), 
        Math.abs(env.getMinY())
        );
  }
  
  // TODO: move to PrecisionModel?
  /**
   * Computes the scale factor which will
   * produce a given number of digits of precision (significant digits)
   * when used to round the given number.
   * <p>
   * For example: to provide 5 decimal digits of precision
   * for the number 123.456 the precision scale factor is 100;
   * for 3 digits of precision the scale factor is 1;
   * for 2 digits of precision the scale factor is 0.1. 
   * <p>
   * Rounding to the scale factor can be performed with {@link PrecisionModel#round}
   * 
   * @param value a number to be rounded
   * @param precisionDigits the number of digits of precision required
   * @return scale factor which provides the required number of digits of precision 
   * 
   * @see PrecisionModel.round
   */
  private static double precisionScale(
      double value, int precisionDigits)
  {
    // the smallest power of 10 greater than the value
    int magnitude = (int) (Math.log(value) / Math.log(10) + 1.0);
    int precDigits = precisionDigits - magnitude;
    
    double scaleFactor = Math.pow(10.0, precDigits);
    return scaleFactor;
  }
 
  /**
   * Computes the inherent scale of a number.
   * The inherent scale is the scale factor for rounding
   * which preserves <b>all</b> digits of precision 
   * (significant digits)
   * present in the numeric value.
   * In other words, it is the scale factor which does not
   * change the numeric value when rounded:
   * <pre>
   *   num = round( num, inherentScale(num) )
   * </pre>
   * 
   * @param value a number
   * @return the inherent scale factor of the number
   */
  public static double inherentScale(double value) {
    int numDec = numberOfDecimals(value);
    double scaleFactor = Math.pow(10.0, numDec);
    return scaleFactor;
  }
  
  /**
   * Computes the inherent scale of a geometry.
   * The inherent scale is the scale factor for rounding
   * which preserves <b>all</b> digits of precision 
   * (significant digits)
   * present in the geometry ordinates.
   * <p>
   * This is the maximum inherent scale
   * of all ordinate values in the geometry.
   *  
   * @param value a number
   * @return the inherent scale factor of the number
   */
  public static double inherentScale(Geometry geom) { 
    InherentScaleFilter scaleFilter = new InherentScaleFilter();
    geom.apply(scaleFilter);
    return scaleFilter.getScale();
  }
  
  /**
   * Computes the inherent scale of two geometries.
   * The inherent scale is the scale factor for rounding
   * which preserves <b>all</b> digits of precision 
   * (significant digits)
   * present in the geometry ordinates.
   * <p>
   * This is the maximum inherent scale
   * of all ordinate values in the geometries.
   * 
   * @param a a geometry
   * @param b a geometry
   * @return the inherent scale factor of the two geometries
   */
  public static double inherentScale(Geometry a, Geometry b) {
    double scale = Scale.inherentScale(a);
    if (b != null) {
      double scaleB = Scale.inherentScale(b);
      scale = Math.max(scale, scaleB);
    }
    return scale;
  }
  
  /*
  // this doesn't work
  private static int BADnumDecimals(double value) {
    double val = Math.abs(value);
    double frac = val - Math.floor(val);
    int numDec = 0;
    while (frac > 0 && numDec < MAX_PRECISION_DIGITS) {
      double mul10 = 10 * frac;
      frac = mul10 - Math.floor(mul10);
      numDec ++;
    }
    return numDec;
  }
  */
  
  /**
   * Determines the 
   * number of decimal places represented in a double-precision
   * number (as determined by Java).
   * This uses the Java double-precision print routine 
   * to determine the number of decimal places,
   * This is likely not optimal for performance, 
   * but should be accurate and portable. 
   * 
   * @param value a numeric value
   * @return the number of decimal places in the value
   */
  private static int numberOfDecimals(double value) {
    /**
     * Ensure that scientific notation is NOT used
     * (it would skew the number of fraction digits)
     */
    String s = OrdinateFormat.DEFAULT.format(value);
    if (s.endsWith(".0")) 
      return 0;
    int len = s.length();
    int decIndex = s.indexOf('.');
    if (decIndex <= 0) 
      return 0;
    return len - decIndex - 1;
  }

  /**
   * Applies the inherent scale calculation 
   * to every ordinate in a geometry.
   * 
   * @author Martin Davis
   *
   */
  private static class InherentScaleFilter implements CoordinateFilter {
    
    private double scale  = 0;

    public double getScale() {
      return scale;
    }
    
    @Override
    public void filter(Coordinate coord) {
      updateScaleMax(coord.getX());
      updateScaleMax(coord.getY());
    }
    
    private void updateScaleMax(double value) {
      double scaleVal = Scale.inherentScale( value );
      if (scaleVal > scale) {
        //System.out.println("Value " + value + " has scale: " + scaleVal);
        scale = scaleVal;
      }
    }
  }
}
