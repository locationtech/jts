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
package org.locationtech.jts.geom.prep;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.Puntal;

/**
 * A factory for creating {@link PreparedGeometry}s.
 * It chooses an appropriate implementation of PreparedGeometry
 * based on the geoemtric type of the input geometry.
 * <p>
 * In the future, the factory may accept hints that indicate
 * special optimizations which can be performed.
 * 
 * 
 * @author Martin Davis
 *
 */
public class PreparedGeometryFactory 
{
  /**
   * Creates a new {@link PreparedGeometry} appropriate for the argument {@link Geometry}.
   * 
   * @param geom the geometry to prepare
   * @return the prepared geometry
   */
	public static PreparedGeometry prepare(Geometry geom)
	{
		return (new PreparedGeometryFactory()).create(geom); 
	}

  public PreparedGeometryFactory() {
  }

  /**
   * Creates a new {@link PreparedGeometry} appropriate for the argument {@link Geometry}.
   * 
   * @param geom the geometry to prepare
   * @return the prepared geometry
   */
  public PreparedGeometry create(Geometry geom)
  {
    if (geom instanceof Polygonal) 
      return new PreparedPolygon((Polygonal) geom);
    if (geom instanceof Lineal) 
      return new PreparedLineString((Lineal) geom);
    if (geom instanceof Puntal) 
      return new PreparedPoint((Puntal) geom);
    
    /**
     * Default representation.
     */
    return new BasicPreparedGeometry(geom);
  }
}
