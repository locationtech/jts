/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Martin Davis
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Martin Davis BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package test.jts.perf.operation.distance;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.distance.*;

public class CachedBABDistance 
{

  private static Geometry cacheGeom = null;
  private static IndexedFacetDistance babDist;
  
  public CachedBABDistance() {
    super();
  }

  static double getDistance(Geometry g1, Geometry g2)
  {
    if (cacheGeom != g1) {
      babDist = new IndexedFacetDistance(g1);
      cacheGeom = g1;
    }
    return babDist.getDistance(g2);
  }
}
