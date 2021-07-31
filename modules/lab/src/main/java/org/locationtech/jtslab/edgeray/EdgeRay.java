/*
 * Copyright (c) 2018 Martin Davis, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtslab.edgeray;

import org.locationtech.jts.geom.Coordinate;

class EdgeRay {

  /**
   * Computes the area term for the edge rays in both directions along an edge.
   * 
   * @param x0
   * @param y0
   * @param x1
   * @param y1
   * @return
   */
  public static double areaTermBoth(double x0, double y0, double x1, double y1) {

    double dx = x1 - x0;
    double dy = y1 - y0;
    double len = Math.sqrt(dx*dx + dy*dy);
    
    double u0x = dx / len;
    double u0y = dy / len;
    // normal vector pointing to R of unit
    double n0x = u0y;
    double n0y = -u0x;
    
    double u1x = -u0x;
    double u1y = -u0y;
    // normal vector pointing to L of back unit vector
    double n1x = -u1y;
    double n1y = u1x;
    
    double areaTerm0 = 0.5 * (x0*u0x + y0*u0y) * (x0*n0x + y0*n0y); 
    double areaTerm1 = 0.5 * (x1*u1x + y1*u1y) * (x1*n1x + y1*n1y); 
    
    return areaTerm0 + areaTerm1;
  }
  
  public static double areaTerm(
      double x0, double y0, double x1, double y1, boolean isNormalToRight) {
    return areaTerm(x0, y0, x0, y0, x1, y1, isNormalToRight);
  }

    
  public static double areaTerm(
      double vx, double vy, double x0, double y0, double x1, double y1, boolean isNormalToRight) {

    double dx = x1 - x0;
    double dy = y1 - y0;
    double len = Math.sqrt(dx*dx + dy*dy);
    
    if (len <= 0) return 0;
    
    double ux = dx / len;
    double uy = dy / len;
    // normal vector pointing to R of unit
    // (assumes CW ring)
    double nx, ny;
    if (isNormalToRight) {
      nx = uy;
      ny = -ux;
    }
    else {
      nx = -uy;
      ny = ux;
    }
    
    double areaTerm = 0.5 * (vx*ux + vy*uy) * (vx*nx + vy*ny); 
    //System.out.println(areaTerm);
    return areaTerm;
  }

  public static double areaTerm(Coordinate p0, Coordinate p1, boolean isNormalToRight) {
    return areaTerm(p0.x, p0.y, p1.x, p1.y, isNormalToRight);
  }
  public static double areaTerm(Coordinate v, Coordinate p0, Coordinate p1, boolean isNormalToRight) {
    return areaTerm(v.x, v.y, p0.x, p0.y, p1.x, p1.y, isNormalToRight);
  }
}
