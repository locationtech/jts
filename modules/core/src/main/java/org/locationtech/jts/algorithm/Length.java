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

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.math.MathUtil;

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
  
    double x0 = pts.getX(0);
    double y0 = pts.getY(0);

    for (int i = 1; i < n; i++) {
      double x1 = pts.getX(i);
      double y1 = pts.getY(i);
      double dx = x1 - x0;
      double dy = y1 - y0;
  
      len += MathUtil.hypot(dx, dy);
  
      x0 = x1;
      y0 = y1;
    }
    return len;
  }

}
