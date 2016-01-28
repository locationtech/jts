
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
 * Computes the centroid of a point geometry.
 * <h2>Algorithm</h2>
 * Compute the average of all points.
 *
 * @version 1.7
 * @deprecated use Centroid instead
 */
public class CentroidPoint
{
  private int ptCount = 0;
  private Coordinate centSum = new Coordinate();

  public CentroidPoint()
  {
  }

  /**
   * Adds the point(s) defined by a Geometry to the centroid total.
   * If the geometry is not of dimension 0 it does not contribute to the centroid.
   * @param geom the geometry to add
   */
  public void add(Geometry geom)
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

  /**
   * Adds the length defined by an array of coordinates.
   * @param pts an array of {@link Coordinate}s
   */
  public void add(Coordinate pt)
  {
    ptCount += 1;
    centSum.x += pt.x;
    centSum.y += pt.y;
  }

  public Coordinate getCentroid()
  {
    Coordinate cent = new Coordinate();
    cent.x = centSum.x / ptCount;
    cent.y = centSum.y / ptCount;
    return cent;
  }

}
