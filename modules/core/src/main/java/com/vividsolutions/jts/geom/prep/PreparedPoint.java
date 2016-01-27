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
package com.vividsolutions.jts.geom.prep;

import com.vividsolutions.jts.geom.*;


/**
 * A prepared version for {@link Puntal} geometries.
 * <p>
 * Instances of this class are thread-safe.
 * 
 * @author Martin Davis
 *
 */
public class PreparedPoint
  extends BasicPreparedGeometry
{
  public PreparedPoint(Puntal point) {
    super((Geometry) point);
  }

  /**
   * Tests whether this point intersects a {@link Geometry}.
   * <p>
   * The optimization here is that computing topology for the test geometry
   * is avoided.  This can be significant for large geometries.
   */
  public boolean intersects(Geometry g)
  {
  	if (! envelopesIntersect(g)) return false;
  	
  	/**
  	 * This avoids computing topology for the test geometry
  	 */
    return isAnyTargetComponentInTest(g);
  }  
}
