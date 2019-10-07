package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.OrdinateFormat;
import org.locationtech.jts.math.MathUtil;

public class Scale {
  /**
   * A number of digits of precision which leaves some computational "headroom"
   * for floating point operations.
   * 
   * This value should be less than the decimal precision of double-precision values (16).
   */
  public static int MAX_PRECISION_DIGITS = 14;
  
  // TODO: move to PrecisionModel?
  public static double precisionScale(
      double value, int maxPrecisionDigits)
  {
    // the smallest power of 10 greater than the value
    int magnitude = (int) (Math.log(value) / Math.log(10) + 1.0);
    int precDigits = maxPrecisionDigits - magnitude;
    
    double scaleFactor = Math.pow(10.0, precDigits);
    return scaleFactor;
  }
 
  public static double safeScale(double value)
  {
    return precisionScale(value, MAX_PRECISION_DIGITS);
  }
  
  public static double safeScale(Geometry geom)
  {
    return safeScale( maxBound( geom.getEnvelopeInternal() ));
  }
  
  public static double safeScale(Geometry a, Geometry b) {
    double maxBndA = maxBound( a.getEnvelopeInternal());
    double maxBndB = maxBound( b.getEnvelopeInternal());
    double maxBndBoth = Math.max(maxBndA,  maxBndB);
    double scale = Scale.safeScale(maxBndBoth);
    return scale;
  }
  
  private static double maxBound(Envelope env) {
    return MathUtil.max(
        Math.abs(env.getMaxX()), 
        Math.abs(env.getMaxY()), 
        Math.abs(env.getMinX()), 
        Math.abs(env.getMinY())
            );
  }
  
  public static double inherentScale(double value) {
    int numDec = numDecimals(value);
    double scaleFactor = Math.pow(10.0, numDec);
    return scaleFactor;
  }
  
  public static double inherentScale(Geometry geom) { 
    ScaleFilter scaleFilter = new ScaleFilter();
    geom.apply(scaleFilter);
    return scaleFilter.getScale();
  }
  
  /**
   * Computes the maximum inherent scale of two geometries.
   * 
   * @param a a geometry
   * @param b a geometry
   * @return 
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
   * A slightly hacky way of determining the 
   * number of decimal places represented in a double-precision
   * number (as determined by Java).
   * 
   * @param value the value to find the decimal places for
   * @return the number of decimal places
   */
  private static int numDecimals(double value) {
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
  
  public static double autoScale(Geometry a, Geometry b) {
    double scale = inherentScale(a, b);
    double safeScale = safeScale(a, b);
    
    /**
     * Use precision scale if lower, 
     * since it is important to preserve some precision for robustness
     */
    if (safeScale < scale) {
      scale = safeScale;
    }
    //System.out.println("Scale = " + scale);
    return scale;
  }

  private static class ScaleFilter implements CoordinateFilter {
    
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
