
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
