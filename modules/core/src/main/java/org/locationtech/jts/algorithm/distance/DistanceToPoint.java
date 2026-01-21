/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

/**
 * Computes the Euclidean distance (L2 metric) from a {@link Coordinate} to a {@link Geometry}.
 * Also computes a point on the geometry which has the given distance to the coordinate.
 */
public class DistanceToPoint 
{

  public DistanceToPoint() {
  }

  public static void computeDistance(Geometry geom, Coordinate pt, PointPairDistance geomPtDist)
  {
    if (geom instanceof LineString) {
      computeDistance((LineString) geom, pt, geomPtDist);
    }
    else if (geom instanceof Polygon) {
      computeDistance((Polygon) geom, pt, geomPtDist);
    }
    else if (geom instanceof GeometryCollection) {
      GeometryCollection gc = (GeometryCollection) geom;
      for (int i = 0; i < gc.getNumGeometries(); i++) {
        Geometry g = gc.getGeometryN(i);
        computeDistance(g, pt, geomPtDist);
      }
    }
    else { // assume geom is Point
      geomPtDist.setMinimum(geom.getCoordinate(), pt);
    }
  }
  
  public static void computeDistance(LineString line, Coordinate pt, PointPairDistance geomPtDist)
  {
    LineSegment tempSegment = new LineSegment();
    Coordinate[] coords = line.getCoordinates();
    for (int i = 0; i < coords.length - 1; i++) {
      tempSegment.setCoordinates(coords[i], coords[i + 1]);
      // this is somewhat inefficient - could do better
      Coordinate closestPt = tempSegment.closestPoint(pt);
      geomPtDist.setMinimum(closestPt, pt);
    }
  }

  public static void computeDistance(LineSegment segment, Coordinate pt, PointPairDistance geomPtDist)
  {
    Coordinate closestPt = segment.closestPoint(pt);
    geomPtDist.setMinimum(closestPt, pt);
  }

  public static void computeDistance(Polygon poly, Coordinate pt, PointPairDistance geomPtDist)
  {
    computeDistance(poly.getExteriorRing(), pt, geomPtDist);
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      computeDistance(poly.getInteriorRingN(i), pt, geomPtDist);
    }
  }
}
