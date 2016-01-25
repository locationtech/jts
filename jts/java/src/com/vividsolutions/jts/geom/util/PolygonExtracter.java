
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
 * Extracts all the {@link Polygon} elements from a {@link Geometry}.
 *
 * @version 1.7
 * @see GeometryExtracter
 */
public class PolygonExtracter
  implements GeometryFilter
{
  /**
   * Extracts the {@link Polygon} elements from a single {@link Geometry}
   * and adds them to the provided {@link List}.
   * 
   * @param geom the geometry from which to extract
   * @param list the list to add the extracted elements to
   */
  public static List getPolygons(Geometry geom, List list)
  {
  	if (geom instanceof Polygon) {
  		list.add(geom);
  	}
  	else if (geom instanceof GeometryCollection) {
  		geom.apply(new PolygonExtracter(list));
  	}
  	// skip non-Polygonal elemental geometries
  	
    return list;
  }

  /**
   * Extracts the {@link Polygon} elements from a single {@link Geometry}
   * and returns them in a {@link List}.
   * 
   * @param geom the geometry from which to extract
   */
  public static List getPolygons(Geometry geom)
  {
    return getPolygons(geom, new ArrayList());
  }

  private List comps;
  /**
   * Constructs a PolygonExtracterFilter with a list in which to store Polygons found.
   */
  public PolygonExtracter(List comps)
  {
    this.comps = comps;
  }

  public void filter(Geometry geom)
  {
    if (geom instanceof Polygon) comps.add(geom);
  }

}
