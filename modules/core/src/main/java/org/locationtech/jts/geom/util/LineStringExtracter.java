
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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFilter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

/**
 * Extracts all the {@link LineString} elements from a {@link Geometry}.
 *
 * @version 1.7
 * @see GeometryExtracter
 */
public class LineStringExtracter
  implements GeometryFilter
{
  /**
   * Extracts the {@link LineString} elements from a single {@link Geometry}
   * and adds them to the provided {@link List}.
   * 
   * @param geom the geometry from which to extract
   * @param lines the list to add the extracted LineStrings to
   * @return the list argument
   */
  public static List getLines(Geometry geom, List lines)
  {
  	if (geom instanceof LineString) {
  		lines.add(geom);
  	}
  	else if (geom instanceof GeometryCollection) {
  		geom.apply(new LineStringExtracter(lines));
  	}
  	// skip non-LineString elemental geometries
  	
    return lines;
  }

  /**
   * Extracts the {@link LineString} elements from a single {@link Geometry}
   * and returns them in a {@link List}.
   * 
   * @param geom the geometry from which to extract
   * @return a list containing the linear elements
   */
  public static List getLines(Geometry geom)
  {
    return getLines(geom, new ArrayList());
  }

  /**
   * Extracts the {@link LineString} elements from a single {@link Geometry}
   * and returns them as either a {@link LineString} or {@link MultiLineString}.
   * 
   * @param geom the geometry from which to extract
   * @return a linear geometry
  */
  public static Geometry getGeometry(Geometry geom)
  {
    return geom.getFactory().buildGeometry(getLines(geom));
  }

  private List comps;
  
  /**
   * Constructs a filter with a list in which to store the elements found.
   */
  public LineStringExtracter(List comps)
  {
    this.comps = comps;
  }

  public void filter(Geometry geom)
  {
    if (geom instanceof LineString) comps.add(geom);
  }

}
