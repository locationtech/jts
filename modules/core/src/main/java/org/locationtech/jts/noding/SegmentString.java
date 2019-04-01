
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
  Object getData();

  /**
   * Sets the user-defined data for this segment string.
   *
   * @param data an Object containing user-defined data
   */
  void setData(Object data);

  /**
   * Gets the number of coordinates that make up the segment string
   *
   * @return the number of coordinates
   */
  int size();

  /**
   * Get the i'th coordinate of this segment string.
   *
   * @param i the index of the coordinate to get. Must be in the range [0, {@linkplain #size()} - 1]
   * @return a coordinate.
   */
  Coordinate getCoordinate(int i);

  /**
   * Gets the array of coordinates that make up this segment string.
   *
   * @return an array of coordinates
   */
  Coordinate[] getCoordinates();

  /**
   * Tests if the segment string is closed, start- and endpoint are the same.
   *
   * @return <c>true</c> if the segment string is closed
   */
  boolean isClosed();

  /**
   * Test if this segment has at least two different coordinates.
   *
   * @return <c>true</c> if there are at least 2 different points in the sequence
   */
  boolean hasExtent();
}
