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
    Coordinate[] pts = extractEdgePoints(ring, start, end);
    normalize(pts);
    return new CoverageEdge(pts);
  }

  private static Coordinate[] extractEdgePoints(LinearRing ring, int start, int end) {
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
    return pts;
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
    //-- normalize a line edge, based on smallest endpoint 
    if (0 < pts[0].compareTo(pts[pts.length - 1])) {
      CoordinateArrays.reverse(pts);
    }
  }

  public static LineSegment computeKey(Coordinate[] pts) {
    if (CoordinateArrays.isRing(pts))
      return computeRingKey(pts);
    return computeLineKey(pts);
  }
  
  /**
   * Computes a key for a ring.
   * The key is the segment starting at the lowest vertex,
   * towards the lowest adjacent distinct vertex.
   * 
   * @param pts
   * @return
   */
  private static LineSegment computeRingKey(Coordinate[] pts) {
    // find lowest vertex index
    int indexLow = 0;
    for (int i = 1; i < pts.length - 1; i++) {
      if (pts[indexLow].compareTo(pts[i]) < 0)
        indexLow = i;
    }
    Coordinate key0 = pts[indexLow];
    // find distinct adjacent vertices
    Coordinate adj0 = findDistinctPoint(pts, indexLow, true, key0);
    Coordinate adj1 = findDistinctPoint(pts, indexLow, false, key0);
    Coordinate key1 = adj0.compareTo(adj1) < 0 ? adj0 : adj1;
    return new LineSegment(key0, key1);
  }

  /**
   * Compute a key for a line.
   * The key is the segment starting at the lowest endpoint.
   * 
   * @param pts
   * @return the edge key value
   */
  private static LineSegment computeLineKey(Coordinate[] pts) {
    //-- endpoints are distinct in a line edge
    Coordinate end0 = pts[0];
    Coordinate end1 = pts[pts.length - 1];
    boolean isForward = 0 > end0.compareTo(end1);
    Coordinate key0, key1;
    if (isForward) {
      key0 = end0;
      key1 = findDistinctPoint(pts, 1, true, key0);
    }
    else {
      key0 = end1;
      key1 = findDistinctPoint(pts, pts.length - 2, false, key0);
    }
    return new LineSegment(key0, key1);
  }

  private static Coordinate findDistinctPoint(Coordinate[] pts, int index, boolean isForward, Coordinate pt) {
    int inc = isForward ? 1 : -1;
    int i = index;
    do {
      if (! pts[i].equals2D(pt)) {
        return pts[i];
      }
      // increment index with wrapping
      i += inc;
      if (i < 0) {
        i = pts.length - 1;
      }
      else if (i > pts.length - 1) {
        i = 0;
      }
    } while (i != index);
    throw new IllegalStateException("Edge does not contain distinct points");
  }

  private Coordinate[] pts;
  
  public CoverageEdge(Coordinate[] pts) {
    this.pts = pts;
  }

  public void setCoordinates(Coordinate[] pts) {
    this.pts = pts;
  }
  
  public LineSegment getKey() {
    //-- key is stable due to points list normalization
    return new LineSegment(pts[0], pts[1]);
  }

  public Coordinate[] getCoordinates() {
    return pts;
  }

  public Coordinate getEndCoordinate() {
    return pts[pts.length - 1];
  }
  
  public Coordinate getStartCoordinate() {
    return pts[0];
  }
  
  public String toString() {
    return WKTWriter.toLineString(pts);
  }

}
