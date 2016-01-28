
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

import java.util.*;

import org.locationtech.jts.geom.*;

/**
 * Extracts the components of a given type from a {@link Geometry}.
 *
 * @version 1.7
 */
public class GeometryExtracter
  implements GeometryFilter
{
	
	protected static boolean isOfClass(Object o, Class clz)
	{
		return clz.isAssignableFrom(o.getClass());
//		return o.getClass() == clz;
	}
	
  /**
   * Extracts the components of type <tt>clz</tt> from a {@link Geometry}
   * and adds them to the provided {@link List}.
   * 
   * @param geom the geometry from which to extract
   * @param list the list to add the extracted elements to
   */
  public static List extract(Geometry geom, Class clz, List list)
  {
  	if (isOfClass(geom, clz)) {
  		list.add(geom);
  	}
  	else if (geom instanceof GeometryCollection) {
  		geom.apply(new GeometryExtracter(clz, list));
  	}
  	// skip non-LineString elemental geometries
  	
    return list;
  }

  /**
   * Extracts the components of type <tt>clz</tt> from a {@link Geometry}
   * and returns them in a {@link List}.
   * 
   * @param geom the geometry from which to extract
   */
  public static List extract(Geometry geom, Class clz)
  {
    return extract(geom, clz, new ArrayList());
  }

  private Class clz;
  private List comps;
  
  /**
   * Constructs a filter with a list in which to store the elements found.
   * 
   * @param clz the class of the components to extract (null means all types)
   * @param comps the list to extract into
   */
  public GeometryExtracter(Class clz, List comps)
  {
  	this.clz = clz;
    this.comps = comps;
  }

  public void filter(Geometry geom)
  {
    if (clz == null || isOfClass(geom, clz)) comps.add(geom);
  }

}
