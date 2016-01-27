
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
package com.vividsolutions.jts.geom.util;

import java.util.*;
import com.vividsolutions.jts.geom.*;

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
