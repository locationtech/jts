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

import org.locationtech.jts.geom.*;
import org.locationtech.jts.noding.*;

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
