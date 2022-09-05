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
package org.locationtech.jts.coverage;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.io.WKTWriter;

class CoverageEdge {

  public static CoverageEdge createEdge(LinearRing ring, int start, int end) {
    int size = start < end 
                  ? end - start + 1 
                  : ring.getNumPoints() - start + end;
    Coordinate[] pts = new Coordinate[size];
    int iring = start;
    for (int i = 0; i < size; i++) {
      pts[i] = ring.getCoordinateN(iring).copy();
      iring += 1;
      if (iring >= ring.getNumPoints()) iring = 1;
    }
    normalize(pts);
    return new CoverageEdge(pts);
  }

  /**
   * Normalize a coordinate list so that 
   * the first point is less than the last point, 
   * or if they are equal, 
   * then the second point is less than the next-to-last point.
   * Assumes that only the first and last points may be identical
   * (as is the case for an edge in a coverage).
   * 
   * @param pts
   */
  private static void normalize(Coordinate[] pts) {
    //TODO: handle rings (use adjacent points to decide on orientation
    if (0 < pts[0].compareTo(pts[pts.length - 1])) {
      CoordinateArrays.reverse(pts);
    }
  }

  private Coordinate[] pts;
  
  public CoverageEdge(Coordinate[] pts) {
    this.pts = pts;
  }

  public LineSegment getKey() {
    return new LineSegment(pts[0], pts[1]);
  }

  public Coordinate[] getCoordinates() {
    return pts;
  }

  public String toString() {
    return WKTWriter.toLineString(pts);
  }

  public Coordinate getEndCoordinate() {
    return pts[pts.length - 1];
  }
  
  public Coordinate getStartCoordinate() {
    return pts[0];
  }
}
