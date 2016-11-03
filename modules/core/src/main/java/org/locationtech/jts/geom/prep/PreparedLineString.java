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
import org.locationtech.jts.noding.FastSegmentSetIntersectionFinder;
import org.locationtech.jts.noding.SegmentStringUtil;

/**
 * A prepared version for {@link Lineal} geometries.
 * <p>
 * Instances of this class are thread-safe.
 * 
 * @author mbdavis
 *
 */
public class PreparedLineString
  extends BasicPreparedGeometry
{
  private FastSegmentSetIntersectionFinder segIntFinder = null;

  public PreparedLineString(Lineal line) {
    super((Geometry) line);
  }

  public synchronized FastSegmentSetIntersectionFinder getIntersectionFinder()
  {
  	/**
  	 * MD - Another option would be to use a simple scan for 
  	 * segment testing for small geometries.  
  	 * However, testing indicates that there is no particular advantage 
  	 * to this approach.
  	 */
  	if (segIntFinder == null)
  		segIntFinder = new FastSegmentSetIntersectionFinder(SegmentStringUtil.extractSegmentStrings(getGeometry()));
    return segIntFinder;
  }
  
  public boolean intersects(Geometry g)
  {
  	if (! envelopesIntersect(g)) return false;
    return PreparedLineStringIntersects.intersects(this, g);
  }
  
  /**
   * There's not much point in trying to optimize contains, since 
   * contains for linear targets requires the entire test geometry 
   * to exactly match the target linework.
   */
}
