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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

/**
 * Functions for computing length.
 * 
 * @author Martin Davis
 *
 */
public class Length {

  /**
   * Computes the length of a linestring specified by a sequence of points.
   * 
   * @param pts the points specifying the linestring
   * @return the length of the linestring
   */
  public static double ofLine(CoordinateSequence pts)
  {
    // optimized for processing CoordinateSequences
    int n = pts.size();
    if (n <= 1)
      return 0.0;
  
    double len = 0.0;
  
    Coordinate p = new Coordinate();
    pts.getCoordinate(0, p);
    double x0 = p.x;
    double y0 = p.y;
  
    for (int i = 1; i < n; i++) {
      pts.getCoordinate(i, p);
      double x1 = p.x;
      double y1 = p.y;
      double dx = x1 - x0;
      double dy = y1 - y0;
  
      len += Math.sqrt(dx * dx + dy * dy);
  
      x0 = x1;
      y0 = y1;
    }
    return len;
  }

}
