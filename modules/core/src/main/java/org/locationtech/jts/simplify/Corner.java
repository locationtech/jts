/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.simplify;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Triangle;

public class Corner implements Comparable<Corner> {
  private int index;
  private int prev;
  private int next;
  private double area;

  public Corner(int i, int prev, int next, double area) {
    this.index = i;
    this.prev = prev;
    this.next = next;
    this.area = area;
  }

  public boolean isVertex(int index) {
    return index == this.index
        || index == prev
        || index == next;
  }

  public int getIndex() {
    return index;
  }
  
  public double getArea() {
    return area;
  }
  
  /**
   * Orders corners by increasing area
   */
  @Override
  public int compareTo(Corner o) {
    return Double.compare(area, o.area);
  }
  
  public Envelope envelope(LinkedRing ring) {
    Coordinate pp = ring.getCoordinate(prev);
    Coordinate p = ring.getCoordinate(index);
    Coordinate pn = ring.getCoordinate(next);
    Envelope env = new Envelope(pp, pn);
    env.expandToInclude(p);
    return env;
  }
  
  public boolean intersects(Coordinate v, LinkedLine edge) {
    Coordinate pp = edge.getCoordinate(prev);
    Coordinate p = edge.getCoordinate(index);
    Coordinate pn = edge.getCoordinate(next);
    return Triangle.intersects(pp, p, pn, v);
  }
  
  public boolean isRemoved(LinkedLine edge) {
    return edge.prev(index) != prev || edge.next(index) != next;
  }
  
  public LineString toLineString(LinkedLine edge) {
    Coordinate pp = edge.getCoordinate(prev);
    Coordinate p = edge.getCoordinate(index);
    Coordinate pn = edge.getCoordinate(next);
    return (new GeometryFactory()).createLineString(
        new Coordinate[] { safeCoord(pp), safeCoord(p), safeCoord(pn) });
  }

  private static Coordinate safeCoord(Coordinate p) {
    if (p == null) return new Coordinate(Double.NaN, Double.NaN);
    return p;
  }
}

