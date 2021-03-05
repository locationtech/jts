/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.operation.distance;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

public class CachedFastDistance 
{
  private static Geometry cacheGeom = null;
  private static IndexedFacetDistance fastDistanceOp;
  
  public CachedFastDistance() {
    super();
  }

  static double getDistance(Geometry g1, Geometry g2)
  {
    if (cacheGeom != g1) {
      fastDistanceOp = new IndexedFacetDistance(g1);
      cacheGeom = g1;
    }
    return fastDistanceOp.distance(g2);
  }
}
