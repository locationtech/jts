/*
 * Copyright (c) 2016 Martin Davis.
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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Computes an interior point of a <code>{@link Geometry}</code>.
 * An interior point is guaranteed to lie in the interior of the Geometry,
 * if it possible to calculate such a point exactly. 
 * Otherwise, the point may lie on the boundary of the geometry.
 * <p>
 * The interior point of an empty geometry is <code>null</code>.
 */
public class InteriorPoint {
  
  /**
   * Compute a location of an interior point in a {@link Geometry}.
   * Handles all geometry types.
   * 
   * @param geom a geometry in which to find an interior point
   * @return the location of an interior point, 
   *  or <code>null</code> if the input is empty
   */
  public static Coordinate getInteriorPoint(Geometry geom) {
    if (geom.isEmpty()) 
      return null;
    
    Coordinate interiorPt = null;
    int dim = geom.getDimension();
    if (dim == 0) {
      interiorPt = InteriorPointPoint.getInteriorPoint(geom);
    }
    else if (dim == 1) {
      interiorPt = InteriorPointLine.getInteriorPoint(geom);
    }
    else {
      interiorPt = InteriorPointArea.getInteriorPoint(geom);
    }
    return interiorPt;
  }

}
