/*
 * Copyright (c) 2019 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.algorithm;

import org.locationtech.jts.algorithm.CGAlgorithmsDD;
import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.precision.CommonBits;

/**
 * Alternate implementations of line intersection algorithms.
 * Used for test purposes only.
 * 
 * @author Martin Davis
 *
 */
public class IntersectionAlgorithms {

  public static Coordinate intersectionBasic(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
    double px = p1.y - p2.y;
    double py = p2.x - p1.x;
    double pw = p1.x * p2.y - p2.x * p1.y;

    double qx = q1.y - q2.y;
    double qy = q2.x - q1.x;
    double qw = q1.x * q2.y - q2.x * q1.y;

    double x = py * qw - qy * pw;
    double y = qx * pw - px * qw;
    double w = px * qy - qx * py;

    double xInt = x / w;
    double yInt = y / w;

    if ((Double.isNaN(xInt)) || (Double.isInfinite(xInt) || Double.isNaN(yInt)) || (Double.isInfinite(yInt))) {
      return null;
    }
    return new Coordinate(xInt, yInt);
  }
  
  public static Coordinate intersectionDDWithFilter(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
    Coordinate intPt = intersectionDDFilter(p1, p2, q1, q2);
    if (intPt != null)
      return intPt;
    return CGAlgorithmsDD.intersection(p1, p2, q1, q2);
  }

  private static final double FILTER_TOL = 1.0E-6;

  private static Coordinate intersectionDDFilter(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
    // Compute using DP math
    Coordinate intPt = intersectionBasic(p1, p2, q1, q2);
    if (intPt == null)
      return null;
    if (Distance.pointToLinePerpendicular(intPt, p1, p2) > FILTER_TOL)
      return null;
    if (Distance.pointToLinePerpendicular(intPt, q1, q2) > FILTER_TOL)
      return null;
    return intPt;
  }

  public static Coordinate intersectionCB(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2)
  {
    Coordinate common = computeCommonCoord(p1, p2, q1, q2);
    p1 = subtractCoord(p1, common);
    p2 = subtractCoord(p2, common);
    q1 = subtractCoord(q1, common);
    q2 = subtractCoord(q2, common);

    // unrolled computation
    double px = p1.y - p2.y;
    double py = p2.x - p1.x;
    double pw = p1.x * p2.y - p2.x * p1.y;

    double qx = q1.y - q2.y;
    double qy = q2.x - q1.x;
    double qw = q1.x * q2.y - q2.x * q1.y;

    double x = py * qw - qy * pw;
    double y = qx * pw - px * qw;
    double w = px * qy - qx * py;

    double xInt = x / w;
    double yInt = y / w;

    if ((Double.isNaN(xInt)) || (Double.isInfinite(xInt) || Double.isNaN(yInt)) || (Double.isInfinite(yInt))) {
      return null;
    }
    return new Coordinate(xInt + common.x, yInt + common.y);
  }

  private static Coordinate subtractCoord(Coordinate c0, Coordinate c1) {
    Coordinate res = c0.copy();
    res.x -= c1.x;
    res.y -= c1.y;
    return res;
  }

  private static Coordinate computeCommonCoord(Coordinate c0, Coordinate c1, Coordinate c2, Coordinate c3) {
    return new Coordinate(getCommonBits(c0.x, c1.x, c2.x, c3.x), getCommonBits(c0.y, c1.y, c2.y, c3.y));
  }

  private static double getCommonBits(double v0, double v1, double v2, double v3) {
    CommonBits cb = new CommonBits();
    cb.add(v0);
    cb.add(v1);
    cb.add(v2);
    cb.add(v3);
    return cb.getCommon();
  }

}
