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

/**
 * An interface for classes which represent a sequence of contiguous line segments.
 * SegmentStrings can carry a context object, which is useful
 * for preserving topological or parentage information.
 *
 * @version 1.7
 */
public interface SegmentString
{
  /**
   * Gets the user-defined data for this segment string.
   *
   * @return the user-defined data
   */
  public Object getData();

  /**
   * Sets the user-defined data for this segment string.
   *
   * @param data an Object containing user-defined data
   */
  public void setData(Object data);

  /**
   * Gets the number of coordinates in this segment string.
   * 
   * @return the number of coordinates
   */
  public int size();
  
  /**
   * Gets the segment string coordinate at a given index.
   *  
   * @param i the coordinate index
   * @return the coordinate at the index
   */
  public Coordinate getCoordinate(int i);
  
  /**
   * Gets the coordinates in this segment string.
   * 
   * @return the coordinates as an array
   */
  public Coordinate[] getCoordinates();
  
  /**
   * Tests if a segment string is a closed ring.
   * 
   * @return true if the segment string is closed
   */
  public boolean isClosed();
  
  /**
   * Gets the previous vertex in a ring from a vertex index.
   * 
   * @param ringSS a segment string forming a ring
   * @param index the vertex index
   * @return the previous vertex in the ring
   * 
   * @see #isClosed
   */
  public default Coordinate prevInRing(int index) {
    int prevIndex = index - 1;
    if (prevIndex < 0) {
      prevIndex = size() - 2;
    }
    return getCoordinate( prevIndex );
  }

  /**
   * Gets the next vertex in a ring from a vertex index.
   * 
   * @param ringSS a segment string forming a ring
   * @param index the vertex index
   * @return the next vertex in the ring
   * 
   * @see #isClosed
   */
  public default Coordinate nextInRing(int index) {
    int nextIndex = index + 1;
    if (nextIndex > size() - 1) {
      nextIndex = 1;
    }
    return getCoordinate( nextIndex );
  }
}
