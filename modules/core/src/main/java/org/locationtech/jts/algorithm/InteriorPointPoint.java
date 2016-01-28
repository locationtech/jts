
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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.*;

/**
 * Computes a point in the interior of an point geometry.
 * <h2>Algorithm</h2>
 * Find a point which is closest to the centroid of the geometry.
 *
 * @version 1.7
 */
public class InteriorPointPoint {

  private Coordinate centroid;
  private double minDistance = Double.MAX_VALUE;

  private Coordinate interiorPoint = null;

  public InteriorPointPoint(Geometry g)
  {
    centroid = g.getCentroid().getCoordinate();
    add(g);
  }

  /**
   * Tests the point(s) defined by a Geometry for the best inside point.
   * If a Geometry is not of dimension 0 it is not tested.
   * @param geom the geometry to add
   */
  private void add(Geometry geom)
  {
    if (geom instanceof Point) {
      add(geom.getCoordinate());
    }
    else if (geom instanceof GeometryCollection) {
      GeometryCollection gc = (GeometryCollection) geom;
      for (int i = 0; i < gc.getNumGeometries(); i++) {
        add(gc.getGeometryN(i));
      }
    }
  }
  private void add(Coordinate point)
  {
    double dist = point.distance(centroid);
    if (dist < minDistance) {
      interiorPoint = new Coordinate(point);
      minDistance = dist;
    }
  }

  public Coordinate getInteriorPoint()
  {
    return interiorPoint;
  }
}
