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

package org.locationtech.jts.operation.overlaysr;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTWriter;

class EdgeKey implements Comparable<EdgeKey> {
  
  public static EdgeKey create(Edge edge) {
    return new EdgeKey(edge);
  }
    
  private Edge edge;
  private Coordinate p0;
  private Coordinate p1;

  EdgeKey(Edge edge) {
    this.edge = edge;
    initPoints(edge);
  }

  private void initPoints(Edge edge) {
    boolean direction = edge.direction();
    if (direction) {
      p0 = edge.getCoordinate(0);
      p1 = edge.getCoordinate(1);
    }
    else {
      int len = edge.size();
      p0 = edge.getCoordinate(len - 1);
      p1 = edge.getCoordinate(len - 2);
    }
  }

  @Override
  public int compareTo(EdgeKey ek) {
    int cmp0 = p0.compareTo(ek.p0);
    if (cmp0 != 0) return cmp0;
    int cmp1 = p1.compareTo(ek.p1);
    return cmp1;
  }
  
  public boolean equals(Object o) {
    if (! (o instanceof EdgeKey)) {
      return false;
    }
    EdgeKey ek = (EdgeKey) o;
    return p0.equals2D(ek.p0) && p1.equals2D(ek.p1);
  }
  
  /**
   * Gets a hashcode for this object.
   * 
   * @return a hashcode for this object
   */
  public int hashCode() {
    //Algorithm from Effective Java by Joshua Bloch
    int result = 17;
    result = 37 * result + hashCode(p0.x);
    result = 37 * result + hashCode(p0.y);
    result = 37 * result + hashCode(p1.x);
    result = 37 * result + hashCode(p1.y);
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
    return "EdgeKey(" + WKTWriter.format(p0) 
      + ", " +  WKTWriter.format(p1) + ")";
  }
}