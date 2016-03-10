
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
package org.locationtech.jts.geom.util;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryComponentFilter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

/**
 * Extracts a single representative {@link Coordinate} 
 * from each connected component of a {@link Geometry}.
 *
 * @version 1.9
 */
public class ComponentCoordinateExtracter
  implements GeometryComponentFilter
{

  /**
   * Extracts the linear components from a single geometry.
   * If more than one geometry is to be processed, it is more
   * efficient to create a single {@link ComponentCoordinateExtracter} instance
   * and pass it to multiple geometries.
   *
   * @param geom the Geometry from which to extract
   * @return a list of Coordinates
   */
  public static List getCoordinates(Geometry geom)
  {
    List coords = new ArrayList();
    geom.apply(new ComponentCoordinateExtracter(coords));
    return coords;
  }

  private List coords;

  /**
   * Constructs a LineExtracterFilter with a list in which to store LineStrings found.
   */
  public ComponentCoordinateExtracter(List coords)
  {
    this.coords = coords;
  }

  public void filter(Geometry geom)
  {
    // add coordinates from connected components
    if (geom instanceof LineString
        || geom instanceof Point) 
      coords.add(geom.getCoordinate());
  }

}
