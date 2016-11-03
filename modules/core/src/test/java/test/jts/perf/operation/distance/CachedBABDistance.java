/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.operation.distance;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

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
