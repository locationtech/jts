/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;

/**
 * Represents a homogeneous coordinate in a 2-D coordinate space.
 * In JTS {@link HCoordinate}s are used as a clean way
 * of computing intersections between line segments.
 *
 * @author David Skea
 * @version 1.7
 */
public class HCoordinate
{

  /**
   * Computes the (approximate) intersection point between two line segments
   * using homogeneous coordinates.
   * <p>
   * Note that this algorithm is
   * not numerically stable; i.e. it can produce intersection points which
   * lie outside the envelope of the line segments themselves.  In order
   * to increase the precision of the calculation input points should be normalized
   * before passing them to this routine.
   */
  public static Coordinate intersection(
      Coordinate p1, Coordinate p2,
      Coordinate q1, Coordinate q2)
      throws NotRepresentableException
  {
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
    
    double xInt = x/w;
    double yInt = y/w;
    
    if ((Double.isNaN(xInt)) || (Double.isInfinite(xInt)
    		|| Double.isNaN(yInt)) || (Double.isInfinite(yInt))) {
      throw new NotRepresentableException();
    }
    
    return new Coordinate(xInt, yInt);
  }

  /*
  public static Coordinate OLDintersection(
      Coordinate p1, Coordinate p2,
      Coordinate q1, Coordinate q2)
      throws NotRepresentableException
  {
    HCoordinate l1 = new HCoordinate(p1, p2);
    HCoordinate l2 = new HCoordinate(q1, q2);
    HCoordinate intHCoord = new HCoordinate(l1, l2);
    Coordinate intPt = intHCoord.getCoordinate();
    return intPt;
  }
  */

  public double x,y,w;

  public HCoordinate() {
    x = 0.0;
    y = 0.0;
    w = 1.0;
  }

  public HCoordinate(double _x, double _y, double _w) {
    x = _x;
    y = _y;
    w = _w;
  }

  public HCoordinate(double _x, double _y) {
    x = _x;
    y = _y;
    w = 1.0;
  }

  public HCoordinate(Coordinate p) {
    x = p.x;
    y = p.y;
    w = 1.0;
  }

  public HCoordinate(HCoordinate p1, HCoordinate p2) 
  {
    x = p1.y * p2.w - p2.y * p1.w;
    y = p2.x * p1.w - p1.x * p2.w;
    w = p1.x * p2.y - p2.x * p1.y;
  }

  /**
   * Constructs a homogeneous coordinate which is the intersection of the lines
   * define by the homogenous coordinates represented by two
   * {@link Coordinate}s.
   * 
   * @param p1
   * @param p2
   */
  public HCoordinate(Coordinate p1, Coordinate p2) 
  {
  	// optimization when it is known that w = 1
    x = p1.y - p2.y;
    y = p2.x - p1.x;
    w = p1.x * p2.y - p2.x * p1.y;
  }
  
  public HCoordinate(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) 
  {
  	// unrolled computation
    double px = p1.y - p2.y;
    double py = p2.x - p1.x;
    double pw = p1.x * p2.y - p2.x * p1.y;
    
    double qx = q1.y - q2.y;
    double qy = q2.x - q1.x;
    double qw = q1.x * q2.y - q2.x * q1.y;
    
    x = py * qw - qy * pw;
    y = qx * pw - px * qw;
    w = px * qy - qx * py;
  }
  
  public double getX() throws NotRepresentableException {
    double a = x/w;
    if ((Double.isNaN(a)) || (Double.isInfinite(a))) {
      throw new NotRepresentableException();
    }
    return a;
  }

  public double getY() throws NotRepresentableException {
    double a = y/w;
    if  ((Double.isNaN(a)) || (Double.isInfinite(a))) {
      throw new NotRepresentableException();
    }
    return a;
  }

  public Coordinate getCoordinate() throws NotRepresentableException {
    Coordinate p = new Coordinate();
    p.x = getX();
    p.y = getY();
    return p;
  }
}