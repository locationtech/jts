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
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.io.WKTWriter;

/**
 * An edge of a polygonal coverage formed from all or a section of a polygon ring.
 * 
 * @author mdavis
 *
 */
class CoverageEdge {

  public static CoverageEdge createEdge(LinearRing ring) {
    Coordinate[] pts = extractEdgePoints(ring, 0, ring.getNumPoints() - 1);
    return new CoverageEdge(pts);
  }

  public static CoverageEdge createEdge(LinearRing ring, int start, int end) {
    Coordinate[] pts = extractEdgePoints(ring, start, end);
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
   * Computes a key segment for a ring.
   * The key is the segment starting at the lowest vertex,
   * towards the lowest adjacent distinct vertex.
   * 
   * @param ring a linear ring
   * @return a LineSegment representing the key
   */
  public static LineSegment key(LinearRing ring) {
    Coordinate[] pts = ring.getCoordinates();
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
   * Computes a distinct key for a section of a linear ring.
   * 
   * @param ring the linear ring
   * @param start index of the start of the section
   * @param end end index of the end of the section
   * @return a LineSegment representing the key
   */
  public static LineSegment key(LinearRing ring, int start, int end) {
    Coordinate[] pts = ring.getCoordinates();
    //-- endpoints are distinct in a line edge
    Coordinate end0 = pts[start];
    Coordinate end1 = pts[end];
    boolean isForward = 0 > end0.compareTo(end1);
    Coordinate key0, key1;
    if (isForward) {
      key0 = end0;
      key1 = findDistinctPoint(pts, start, true, key0);
    }
    else {
      key0 = end1;
      key1 = findDistinctPoint(pts, end, false, key0);
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
  private int ringCount = 0;
  
  public CoverageEdge(Coordinate[] pts) {
    this.pts = pts;
  }

  public void incRingCount() {
    ringCount++;
  }
  
  public int getRingCount() {
    return ringCount;
  }
  
  public void setCoordinates(Coordinate[] pts) {
    this.pts = pts;
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
