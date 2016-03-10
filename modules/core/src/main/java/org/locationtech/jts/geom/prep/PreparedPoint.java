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
import org.locationtech.jts.geom.Puntal;


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
