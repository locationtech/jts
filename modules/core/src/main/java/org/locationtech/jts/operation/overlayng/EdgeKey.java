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
import org.locationtech.jts.io.OrdinateFormat;

/**
 * A key for sorting and comparing edges in a noded arrangement.
 * Relies on the fact that in a correctly noded arrangement
 * edges are identical (up to direction) 
 * if they have their first segment in common. 
 * 
 * @author mdavis
 *
 */
class EdgeKey implements Comparable<EdgeKey> {
  
  public static EdgeKey create(Edge edge) {
    return new EdgeKey(edge);
  }
    
  private double p0x;
  private double p0y;
  private double p1x;
  private double p1y;

  EdgeKey(Edge edge) {
    initPoints(edge);
  }

  private void initPoints(Edge edge) {
    boolean direction = edge.direction();
    if (direction) {
      init(edge.getCoordinate(0), 
          edge.getCoordinate(1));
    }
    else {
      int len = edge.size();
      init( edge.getCoordinate(len - 1), 
          edge.getCoordinate(len - 2) );
    }
  }

  private void init(Coordinate p0, Coordinate p1) {
    p0x = p0.getX();
    p0y = p0.getY();
    p1x = p1.getX();
    p1y = p1.getY();
  }

  @Override
  public int compareTo(EdgeKey ek) {
    if (p0x < ek.p0x) return -1;
    if (p0x > ek.p0x) return 1;
    if (p0y < ek.p0y) return -1;
    if (p0y > ek.p0y) return 1;
    // first points are equal, compare second
    if (p1x < ek.p1x) return -1;
    if (p1x > ek.p1x) return 1;
    if (p1y < ek.p1y) return -1;
    if (p1y > ek.p1y) return 1;
    return 0;
  }
  
  public boolean equals(Object o) {
    if (! (o instanceof EdgeKey)) {
      return false;
    }
    EdgeKey ek = (EdgeKey) o;
    return p0x == ek.p0x 
        && p0y == ek.p0y
        && p1x == ek.p1x
        && p1y == ek.p1y;
  }
  
  /**
   * Gets a hashcode for this object.
   * 
   * @return a hashcode for this object
   */
  public int hashCode() {
    //Algorithm from Effective Java by Joshua Bloch
    int result = 17;
    result = 37 * result + hashCode(p0x);
    result = 37 * result + hashCode(p0y);
    result = 37 * result + hashCode(p1x);
    result = 37 * result + hashCode(p1y);
    return result;
  }
  
  /**
   * Computes a hash code for a double value, using the algorithm from
   * Joshua Bloch's book <i>Effective Java"</i>
   * 
   * @param x the value to compute for
   * @return a hashcode for x
   */
  public static int hashCode(double x) {
    long f = Double.doubleToLongBits(x);
    return (int)(f^(f>>>32));
  }
  
  public String toString() {
    return "EdgeKey(" + format(p0x, p0y) 
      + ", " +  format(p1x, p1y) + ")";
  }
  
  private String format(double x, double y) {
    return OrdinateFormat.DEFAULT.format(x) + " " + OrdinateFormat.DEFAULT.format(y);
  }
}