
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
package org.locationtech.jts.operation.distance;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFilter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * A ConnectedElementPointFilter extracts a single point
 * from each connected element in a Geometry
 * (e.g. a polygon, linestring or point)
 * and returns them in a list. The elements of the list are 
 * {@link org.locationtech.jts.operation.distance.GeometryLocation}s.
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
   * are {@link org.locationtech.jts.operation.distance.GeometryLocation}s.
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
