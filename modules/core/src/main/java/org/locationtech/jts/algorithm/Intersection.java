/*
 * Copyright (c) 2019 martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;

/**
 * Contains functions to compute intersections between lines.
 * 
 * @author Martin Davis
 *
 */
public class Intersection {
  
  /**
   * Computes the intersection point of two lines.
   * If the lines are parallel or collinear this case is detected 
   * and <code>null</code> is returned.
   * <p>
   * In general it is not possible to accurately compute
   * the intersection point of two lines, due to 
   * numerical roundoff.
   * This is particularly true when the input lines are nearly parallel.
   * This routine uses numerical conditioning on the input values
   * to ensure that the computed value should be very close to the correct value.
   * 
   * @param p1 an endpoint of line 1
   * @param p2 an endpoint of line 1
   * @param q1 an endpoint of line 2
   * @param q2 an endpoint of line 2
   * @return the intersection point between the lines, if there is one,
   * or null if the lines are parallel or collinear
   * 
   * @see CGAlgorithmsDD#intersection(Coordinate, Coordinate, Coordinate, Coordinate)
   */
  public static Coordinate intersection(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
    // compute midpoint of "kernel envelope"
    double minX0 = p1.x < p2.x ? p1.x : p2.x;
    double minY0 = p1.y < p2.y ? p1.y : p2.y;
    double maxX0 = p1.x > p2.x ? p1.x : p2.x;
    double maxY0 = p1.y > p2.y ? p1.y : p2.y;

    double minX1 = q1.x < q2.x ? q1.x : q2.x;
    double minY1 = q1.y < q2.y ? q1.y : q2.y;
    double maxX1 = q1.x > q2.x ? q1.x : q2.x;
    double maxY1 = q1.y > q2.y ? q1.y : q2.y;

    double intMinX = minX0 > minX1 ? minX0 : minX1;
    double intMaxX = maxX0 < maxX1 ? maxX0 : maxX1;
    double intMinY = minY0 > minY1 ? minY0 : minY1;
    double intMaxY = maxY0 < maxY1 ? maxY0 : maxY1;

    double midx = (intMinX + intMaxX) / 2.0;
    double midy = (intMinY + intMaxY) / 2.0;
    
    // condition ordinate values by subtracting midpoint
    double p1x = p1.x - midx;
    double p1y = p1.y - midy;
    double p2x = p2.x - midx;
    double p2y = p2.y - midy;
    double q1x = q1.x - midx;
    double q1y = q1.y - midy;
    double q2x = q2.x - midx;
    double q2y = q2.y - midy;
     
    // unrolled computation using homogeneous coordinates eqn
    double px = p1y - p2y;
    double py = p2x - p1x;
    double pw = p1x * p2y - p2x * p1y;
    
    double qx = q1y - q2y;
    double qy = q2x - q1x;
    double qw = q1x * q2y - q2x * q1y;
    
    double x = py * qw - qy * pw;
    double y = qx * pw - px * qw;
    double w = px * qy - qx * py;
    
    double xInt = x/w;
    double yInt = y/w;
    
    // check for parallel lines
    if ((Double.isNaN(xInt)) || (Double.isInfinite(xInt)
        || Double.isNaN(yInt)) || (Double.isInfinite(yInt))) {
      return null;
    }
    // de-condition intersection point
    return new Coordinate(xInt + midx, yInt + midy);
  }

}


