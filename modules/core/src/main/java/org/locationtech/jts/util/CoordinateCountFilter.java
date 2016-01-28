

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
package org.locationtech.jts.util;

import org.locationtech.jts.geom.*;

/**
 *  A {@link CoordinateFilter} that counts the total number of coordinates
 *  in a <code>Geometry</code>.
 *
 *@version 1.7
 */
public class CoordinateCountFilter implements CoordinateFilter {
  private int n = 0;

  public CoordinateCountFilter() { }

  /**
   *  Returns the result of the filtering.
   *
   *@return    the number of points found by this <code>CoordinateCountFilter</code>
   */
  public int getCount() {
    return n;
  }

  public void filter(Coordinate coord) {
    n++;
  }
}

