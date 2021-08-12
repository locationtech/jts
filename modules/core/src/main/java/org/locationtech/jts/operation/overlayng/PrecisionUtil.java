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
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.OrdinateFormat;
import org.locationtech.jts.math.MathUtil;

/**
 * Functions for computing precision model scale factors
 * that ensure robust geometry operations.
 * In particular, these can be used to
 * automatically determine appropriate scale factors for operations 
 * using limited-precision noding (such as {@link OverlayNG}).
 * <p>
 * WARNING: the <code>inherentScale</code> and <code>robustScale</code> 
 * functions can be very slow, due to the method used to determine
 * number of decimal places of a number.  
 * These are not recommended for production use.
 * 
 * @author Martin Davis
 *
 */
public class PrecisionUtil 
{  
  /**
   * A number of digits of precision which leaves some computational "headroom"
   * to ensure robust evaluation of certain double-precision floating point geometric operations.
   * 
   * This value should be less than the maximum decimal precision of double-precision values (16).
   */
  public static int MAX_ROBUST_DP_DIGITS = 14;
  
  /**
   * Determines a precision model to 
   * use for robust overlay operations.
   * The precision scale factor is chosen to maximize 
   * output precision while avoiding round-off issues.
   * <p>
   * NOTE: this is a heuristic determination, so is not guaranteed to 
   * eliminate precision issues.
   * <p>
   * WARNING: this is very slow.
   * 
   * @param a a geometry
   * @param b a geometry
   * @return a suitable precision model for overlay
   */
  public static PrecisionModel robustPM(Geometry a, Geometry b) {
    double scale = PrecisionUtil.robustScale(a, b);
    return new PrecisionModel( scale );
  }
  
 
  /**
   * Computes a safe scale factor for a numeric value.
   * A safe scale factor ensures that rounded 
   * number has no more than {@link #MAX_ROBUST_DP_DIGITS}
   * digits of precision.
   * 
   * @param value a numeric value
   * @return a safe scale factor for the value
   */
  public static double safeScale(double value)
  {
    return precisionScale(value, MAX_ROBUST_DP_DIGITS);
  }
  
  /**
   * Computes a safe scale factor for a geometry.
   * A safe scale factor ensures that the rounded 
   * ordinates have no more than {@link #MAX_ROBUST_DP_DIGITS}
   * digits of precision.
   * 
   * @param geom a geometry
   * @return a safe scale factor for the geometry ordinates
   */
  public static double safeScale(Geometry geom)
  {
    return safeScale( maxBoundMagnitude( geom.getEnvelopeInternal() ));
  }
  
  /**
   * Computes a safe scale factor for two geometries.
   * A safe scale factor ensures that the rounded 
   * ordinates have no more than {@link #MAX_ROBUST_DP_DIGITS}
   * digits of precision.
   * 
   * @param a a geometry
   * @param b a geometry (which may be null)
   * @return a safe scale factor for the geometry ordinates
   */
  public static double safeScale(Geometry a, Geometry b) {
    double maxBnd = maxBoundMagnitude( a.getEnvelopeInternal());
    if (b != null) {
      double maxBndB = maxBoundMagnitude( b.getEnvelopeInternal());
      maxBnd = Math.max(maxBnd,  maxBndB);
    }
    double scale = PrecisionUtil.safeScale(maxBnd);
    return scale;
  }
  
  /**
   * Determines the maximum magnitude (absolute value) of the bounds of an
   * of an envelope.
   * This is equal to the largest ordinate value
   * which must be accommodated by a scale factor.
   * 
   * @param env an envelope
   * @return the value of the maximum bound magnitude
   */
  private static double maxBoundMagnitude(Envelope env) {
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
   * <p>
   * WARNING: this is very slow.
   *  
   * @param geom geometry
   * @return inherent scale of a geometry
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
   * <p>
   * WARNING: this is very slow.
   * 
   * @param a a geometry
   * @param b a geometry
   * @return the inherent scale factor of the two geometries
   */
  public static double inherentScale(Geometry a, Geometry b) {
    double scale = PrecisionUtil.inherentScale(a);
    if (b != null) {
      double scaleB = PrecisionUtil.inherentScale(b);
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
   * <p>
   * WARNING: this is very slow.
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
      double scaleVal = PrecisionUtil.inherentScale( value );
      if (scaleVal > scale) {
        //System.out.println("Value " + value + " has scale: " + scaleVal);
        scale = scaleVal;
      }
    }
  }
  
  /**
   * Determines a precision model to 
   * use for robust overlay operations for one geometry.
   * The precision scale factor is chosen to maximize 
   * output precision while avoiding round-off issues.
   * <p>
   * NOTE: this is a heuristic determination, so is not guaranteed to 
   * eliminate precision issues.
   * <p>
   * WARNING: this is very slow.
   * 
   * @param a a geometry
   * @return a suitable precision model for overlay
   */
  public static PrecisionModel robustPM(Geometry a) {
    double scale = PrecisionUtil.robustScale(a);
    return new PrecisionModel( scale );
  }
  
  /**
   * Determines a scale factor which maximizes 
   * the digits of precision and is 
   * safe to use for overlay operations.
   * The robust scale is the minimum of the 
   * inherent scale and the safe scale factors.
   * <p>
   * WARNING: this is very slow.
   * 
   * @param a a geometry 
   * @param b a geometry
   * @return a scale factor for use in overlay operations
   */
  public static double robustScale(Geometry a, Geometry b) {
    double inherentScale = inherentScale(a, b);
    double safeScale = safeScale(a, b);
    return robustScale(inherentScale, safeScale);
  }
  
  /**
   * Determines a scale factor which maximizes 
   * the digits of precision and is 
   * safe to use for overlay operations.
   * The robust scale is the minimum of the 
   * inherent scale and the safe scale factors.
   * 
   * @param a a geometry 
   * @return a scale factor for use in overlay operations
   */
  public static double robustScale(Geometry a) {
    double inherentScale = inherentScale(a);
    double safeScale = safeScale(a);
    return robustScale(inherentScale, safeScale);
  }
  
  private static double robustScale(double inherentScale, double safeScale) {
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
 
}