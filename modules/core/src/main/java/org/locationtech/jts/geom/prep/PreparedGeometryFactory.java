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
