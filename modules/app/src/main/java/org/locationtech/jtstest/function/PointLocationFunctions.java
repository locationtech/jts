/*
 * Copyright (c) 2017 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class PointLocationFunctions {

  public static boolean isPointInRing(Geometry g1, Geometry g2) {
    Geometry ring = g1;
    Geometry pt = g2;
    if (g1.getNumPoints() == 1) {
      ring = g2;
      pt = g1;
    }
    Coordinate[] ptsRing = OrientationFunctions.getRing(ring);
    if (ptsRing == null) return false;
    return PointLocation.isInRing(pt.getCoordinate(), ptsRing);
  }

}
