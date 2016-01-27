
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
package com.vividsolutions.jts.operation.distance;

import java.util.*;
import com.vividsolutions.jts.geom.*;

/**
 * A ConnectedElementPointFilter extracts a single point
 * from each connected element in a Geometry
 * (e.g. a polygon, linestring or point)
 * and returns them in a list. The elements of the list are 
 * {@link com.vividsolutions.jts.operation.distance.GeometryLocation}s.
 *
 * @version 1.7
 */
public class ConnectedElementLocationFilter
  implements GeometryFilter
{

  /**
   * Returns a list containing a point from each Polygon, LineString, and Point
   * found inside the specified geometry. Thus, if the specified geometry is
   * not a GeometryCollection, an empty list will be returned. The elements of the list 
   * are {@link com.vividsolutions.jts.operation.distance.GeometryLocation}s.
   */  
  public static List getLocations(Geometry geom)
  {
    List locations = new ArrayList();
    geom.apply(new ConnectedElementLocationFilter(locations));
    return locations;
  }

  private List locations;

  ConnectedElementLocationFilter(List locations)
  {
    this.locations = locations;
  }

  public void filter(Geometry geom)
  {
    if (geom instanceof Point
      || geom instanceof LineString
      || geom instanceof Polygon )
      locations.add(new GeometryLocation(geom, 0, geom.getCoordinate()));
  }

}
