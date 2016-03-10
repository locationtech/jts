

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
package org.locationtech.jts.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;

/**
 *  A {@link CoordinateFilter} that creates an array containing every
 *  coordinate in a {@link Geometry}.
 *
 *@version 1.7
 */
public class CoordinateArrayFilter implements CoordinateFilter {
  Coordinate[] pts = null;
  int n = 0;

  /**
   *  Constructs a <code>CoordinateArrayFilter</code>.
   *
   *@param  size  the number of points that the <code>CoordinateArrayFilter</code>
   *      will collect
   */
  public CoordinateArrayFilter(int size) {
    pts = new Coordinate[size];
  }

  /**
   *  Returns the gathered <code>Coordinate</code>s.
   *
   *@return    the <code>Coordinate</code>s collected by this <code>CoordinateArrayFilter</code>
   */
  public Coordinate[] getCoordinates() {
    return pts;
  }

  public void filter(Coordinate coord) {
    pts[n++] = coord;
  }
}

