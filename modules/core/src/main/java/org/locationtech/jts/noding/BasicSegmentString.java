/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.noding;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.WKTWriter;

/**
 * Represents a read-only list of contiguous line segments.
 * This can be used for detection of intersections or nodes.
 * {@link SegmentString}s can carry a context object, which is useful
 * for preserving topological or parentage information.
 * <p>
 * If adding nodes is required use {@link NodedSegmentString}.
 *
 * @version 1.7
 * @see NodedSegmentString
 */
public class BasicSegmentString
	implements SegmentString 
{
  public static BasicSegmentString substring(SegmentString segString, int start, int end) {
    Coordinate[] pts = new Coordinate[end - start + 1];
    int ipts = 0;
    for (int i = start; i < end + 1; i++) {
      pts[ipts++] = segString.getCoordinate(i).copy();
    }
    return new BasicSegmentString(pts, segString.getData());
  }
  
  private Coordinate[] pts;
  private Object data;

  /**
   * Creates a new segment string from a list of vertices.
   *
   * @param pts the vertices of the segment string
   * @param data the user-defined data of this segment string (may be null)
   */
  public BasicSegmentString(Coordinate[] pts, Object data)
  {
    this.pts = pts;
    this.data = data;
  }

  /**
   * Gets the user-defined data for this segment string.
   *
   * @return the user-defined data
   */
  public Object getData() { return data; }

  /**
   * Sets the user-defined data for this segment string.
   *
   * @param data an Object containing user-defined data
   */
  public void setData(Object data) { this.data = data; }

  public int size() { return pts.length; }
  public Coordinate getCoordinate(int i) { return pts[i]; }
  public Coordinate[] getCoordinates() { return pts; }

  public boolean isClosed()
  {
    return pts[0].equals(pts[pts.length - 1]);
  }

  /**
   * Gets the octant of the segment starting at vertex <code>index</code>.
   *
   * @param index the index of the vertex starting the segment.  Must not be
   * the last index in the vertex list
   * @return the octant of the segment at the vertex
   */
  public int getSegmentOctant(int index)
  {
    if (index == pts.length - 1) return -1;
    return Octant.octant(getCoordinate(index), getCoordinate(index + 1));
  }

  public String toString()
  {
    return WKTWriter.toLineString(new CoordinateArraySequence(pts));
  }
}
