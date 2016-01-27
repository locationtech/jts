
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
 * Extracts a single point
 * from each connected element in a Geometry
 * (e.g. a polygon, linestring or point)
 * and returns them in a list
 *
 * @version 1.7
 */
public class ConnectedElementPointFilter
  implements GeometryFilter
{

  /**
   * Returns a list containing a Coordinate from each Polygon, LineString, and Point
   * found inside the specified geometry. Thus, if the specified geometry is
   * not a GeometryCollection, an empty list will be returned.
   */
  public static List getCoordinates(Geometry geom)
  {
    List pts = new ArrayList();
    geom.apply(new ConnectedElementPointFilter(pts));
    return pts;
  }

  private List pts;

  ConnectedElementPointFilter(List pts)
  {
    this.pts = pts;
  }

  public void filter(Geometry geom)
  {
    if (geom instanceof Point
      || geom instanceof LineString
      || geom instanceof Polygon )
      pts.add(geom.getCoordinate());
  }

}
