

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

