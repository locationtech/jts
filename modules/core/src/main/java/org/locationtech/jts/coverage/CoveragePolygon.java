/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;

class CoveragePolygon {

  private Polygon polygon;
  private Envelope polyEnv;
  IndexedPointInAreaLocator locator;

  public CoveragePolygon(Polygon poly) {
    this.polygon = poly;
    polyEnv = polygon.getEnvelopeInternal();
  }

  public boolean intersectsEnv(Envelope env) {
    //-- test intersection explicitly to avoid expensive null check
    return ! (env.getMinX() > polyEnv.getMaxX()
        || env.getMaxX() < polyEnv.getMinX()
        || env.getMinY() > polyEnv.getMaxY()
        || env.getMaxY() < polyEnv.getMinY());
  }

  private boolean intersectsEnv(Coordinate p) {
    //-- test intersection explicitly to avoid expensive null check
    return ! (p.x > polyEnv.getMaxX() ||
        p.x < polyEnv.getMinX() ||
        p.y > polyEnv.getMaxY() ||
        p.y < polyEnv.getMinY());
  }

  public boolean contains(Coordinate p) {
    if (! intersectsEnv(p))
      return false;
    PointOnGeometryLocator pia = getLocator();
    return Location.INTERIOR == pia.locate(p);    
  }
  
  private PointOnGeometryLocator getLocator() {
    if (locator == null) {
      locator = new IndexedPointInAreaLocator(polygon);
    }
    return locator;
  }

}
