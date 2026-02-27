/*
 * Copyright (c) 2023 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.construct;

import org.locationtech.jts.algorithm.locate.IndexedPointInPolygonsLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

/**
 * Computes the distance between a point and a geometry
 * (which may be a collection containing any type of geometry).
 * Also computes the pair of points containing the input
 * point and the nearest point on the geometry.
 * 
 * @author mdavis
 *
 */
class IndexedDistanceToPoint {
  
  private Geometry targetGeometry;
  private IndexedFacetDistance facetDistance;
  private IndexedPointInPolygonsLocator ptLocater;

  public IndexedDistanceToPoint(Geometry geom) {
    this.targetGeometry = geom;
  }
  
  private void init() {
    if (facetDistance != null)
      return;
    facetDistance = new IndexedFacetDistance(targetGeometry);
    ptLocater = new IndexedPointInPolygonsLocator(targetGeometry);
  }
  
  /**
   * Computes the distance from a point to the geometry.
   * 
   * @param pt the input point
   * @return the distance to the geometry
   */
  public double distance(Point pt) {
    init();
    //-- distance is 0 if point is inside a target polygon
    if (isInArea(pt)) {
      return 0;
    }
    return facetDistance.distance(pt);
  }
  
  private boolean isInArea(Point pt) {
    return Location.EXTERIOR != ptLocater.locate(pt.getCoordinate());
  }
  
  /**
   * Gets the nearest locations between the geometry and a point.
   * The first location lies on the geometry, 
   * and the second location is the provided point.
   * 
   * @param pt the point to compute the nearest location for
   * @return a pair of locations
   */
  public Coordinate[] nearestPoints(Point pt) {
    init();
    if (isInArea(pt)) {
      Coordinate p = pt.getCoordinate();
      return new Coordinate[] { p.copy(), p.copy() };
    }
    return facetDistance.nearestPoints(pt);
  }
}
