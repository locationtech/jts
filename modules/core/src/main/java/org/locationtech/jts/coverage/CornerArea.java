package org.locationtech.jts.coverage;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.math.MathUtil;

class CornerArea {
  public static final double DEFAULT_SMOOTH_WEIGHT = 0.0;
  
  private double weightSmooth = DEFAULT_SMOOTH_WEIGHT;
  
  public CornerArea() {
  }

  public CornerArea(double weightSmooth) {
    this.weightSmooth = weightSmooth;
  }

  public double area(Coordinate pp, Coordinate p, Coordinate pn) {
    
    double area = Triangle.area(pp, p, pn);
    double ang = angleNorm(pp, p, pn);
    //-- rescale to [-1 .. 1], with 1 being narrow and -1 being flat
    double angBias = 1.0 - 2.0 * ang;
    //-- reduce area for narrower corners, to make them more likely to be removed
    double areaWeighted = (1 - weightSmooth * angBias) * area;
    return areaWeighted;
  }
  
  private static double angleNorm(Coordinate pp, Coordinate p, Coordinate pn) {
    double angNorm = Angle.angleBetween(pp, p, pn) / 2 / Math.PI;
    return MathUtil.clamp(angNorm, 0, 1);
  }
}
