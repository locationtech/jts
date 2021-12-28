/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.hull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * 
 * @author Martin Davis
 *
 */
public class PolygonConcaveHull {
  
  public static Geometry hull(Geometry poly, double vertexCountFraction) {
    PolygonConcaveHull hull = new PolygonConcaveHull(poly, vertexCountFraction);
    return hull.getResult();
  }

  private int targetVertexCount;
  private boolean isOuter;
  private Geometry inputGeom;
  private double vertexCountFraction;

  /**
   * Creates a new PolygonConcaveHull instance.
   * 
   * @param poly the polygon vertices to process
   */
  public PolygonConcaveHull(Geometry inputGeom, double vertexCountFraction) {
    this.inputGeom = inputGeom; 
    isOuter = vertexCountFraction >= 0;
    this.vertexCountFraction = Math.abs(vertexCountFraction); 
  }

  public Geometry getResult() {
    Polygon poly = (Polygon) inputGeom;
    LinearRing ring = poly.getExteriorRing();
    Coordinate[] pts = ring.getCoordinates();
    targetVertexCount = (int) ((pts.length - 1) * vertexCountFraction);

    Coordinate[] hullPts =  RingConcaveHull.hull(pts, isOuter, targetVertexCount);
    return inputGeom.getFactory().createPolygon(hullPts);
  }
  
}