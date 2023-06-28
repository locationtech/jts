/*
 * Copyright (c) 2023 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.util;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * Extracts the {@link Polygon} and {@link MultiPolygon} elements from a {@link Geometry}.
 */
public class PolygonalExtracter
{
  /**
   * Extracts the {@link Polygon} and {@link MultiPolygon} elements from a {@link Geometry}
   * and adds them to the provided list.
   * 
   * @param geom the geometry from which to extract
   * @param list the list to add the extracted elements to
   */
  public static List<Geometry> getPolygonals(Geometry geom, List<Geometry> list)
  {
  	if (geom instanceof Polygon || geom instanceof MultiPolygon) {
  		list.add(geom);
  	}
  	else if (geom instanceof GeometryCollection) {
  	  for (int i = 0; i < geom.getNumGeometries(); i++) {
  	    getPolygonals(geom.getGeometryN(i), list);
  	  }
  	}
  	// skip non-Polygonal elemental geometries 	
    return list;
  }

  /**
   * Extracts the {@link Polygon} and {@link MultiPolygon} elements from a {@link Geometry}
   * and returns them in a list.
   * 
   * @param geom the geometry from which to extract
   */
  public static List<Geometry> getPolygonals(Geometry geom)
  {
    return getPolygonals(geom, new ArrayList<Geometry>());
  }

}
