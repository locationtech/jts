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

import org.locationtech.jts.algorithm.construct.LargestEmptyCircle;
import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 * Computes an interior point of a <code>{@link Geometry}</code>.
 * An interior point is guaranteed to lie in the interior of the Geometry,
 * if it possible to calculate such a point exactly. 
 * Otherwise, the point may lie on the boundary of the geometry.
 * For collections the interior point is computed for the collection of 
 * non-empty elements of highest dimension.  
 * The interior point of an empty geometry is <code>null</code>.
 * 
 * <h2>Algorithm</h2>
 * The point is chosen to be "close to the center" of the geometry.
 * The location depends on the dimension of the input:
 * 
 * <ul>
 * <li><b>Dimension 2</b> - the interior point is constructed in the middle of the longest interior segment
 * of a line bisecting the area.
 * 
 * <li><b>Dimension 1</b> - the interior point is the interior or boundary vertex closest to the centroid.

 * <li><b>Dimension 0</b> - the point is the point closest to the centroid.
 * </ul> 
 * 
 * @see Centroid
 * @see MaximumInscribedCircle
 * @see LargestEmptyCircle
 */
public class InteriorPoint {
  
  /**
   * Computes a location of an interior point in a {@link Geometry}.
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
    //TODO: determine highest non-empty dimension
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
