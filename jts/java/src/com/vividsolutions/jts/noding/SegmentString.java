
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jts.noding;

import com.vividsolutions.jts.geom.Coordinate;

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

  public int size();
  public Coordinate getCoordinate(int i);
  public Coordinate[] getCoordinates();
  public boolean isClosed();
}
