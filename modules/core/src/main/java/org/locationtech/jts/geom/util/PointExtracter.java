
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
import java.util.Collections;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFilter;
import org.locationtech.jts.geom.Point;


/**
 * Extracts all the 0-dimensional ({@link Point}) components from a {@link Geometry}.
 *
 * @version 1.7
 * @see GeometryExtracter
 */
public class PointExtracter
  implements GeometryFilter
{
  /**
   * Extracts the {@link Point} elements from a single {@link Geometry}
   * and adds them to the provided {@link List}.
   * 
   * @param geom the geometry from which to extract
   * @param list the list to add the extracted elements to
   */
  public static List getPoints(Geometry geom, List list)
  {
  	if (geom instanceof Point) {
  		list.add(geom);
  	}
  	else if (geom instanceof GeometryCollection) {
  		geom.apply(new PointExtracter(list));
  	}
  	// skip non-Polygonal elemental geometries
  	
    return list;
  }

  /**
   * Extracts the {@link Point} elements from a single {@link Geometry}
   * and returns them in a {@link List}.
   * 
   * @param geom the geometry from which to extract
   */
  public static List getPoints(Geometry geom) {
    if (geom instanceof Point) {
      return Collections.singletonList(geom);
    }
    return getPoints(geom, new ArrayList());
  }

  private List pts;
  /**
   * Constructs a PointExtracterFilter with a list in which to store Points found.
   */
  public PointExtracter(List pts)
  {
    this.pts = pts;
  }

  public void filter(Geometry geom)
  {
    if (geom instanceof Point) pts.add(geom);
  }

}
