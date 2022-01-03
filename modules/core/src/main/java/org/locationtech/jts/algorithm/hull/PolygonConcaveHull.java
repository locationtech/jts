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

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
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

  private Geometry inputGeom;
  private boolean isOuter;
  private double vertexCountFraction;
  private GeometryFactory geomFactory;

  /**
   * Creates a new PolygonConcaveHull instance.
   * 
   * @param inputGeom the polygonal geometry to process
   * @param vertexCountFraction the fraction of number of vertices to target
   */
  public PolygonConcaveHull(Geometry inputGeom, double vertexCountFraction) {
    this.inputGeom = inputGeom; 
    this.geomFactory = inputGeom.getFactory();
    this.isOuter = vertexCountFraction >= 0;
    this.vertexCountFraction = Math.abs(vertexCountFraction); 
  }

  public Geometry getResult() {
    Polygon poly = (Polygon) inputGeom;
    List<RingConcaveHull> polyHulls = initPolygon(poly);
    Polygon hull = polygonHull(poly, polyHulls);
    return hull;
  }

  private List<RingConcaveHull> initPolygon(Polygon poly) {
    List<RingConcaveHull> rchList = new ArrayList<RingConcaveHull>();
    if (poly.isEmpty()) 
      return rchList;
    
    rchList.add( createRingHull( poly.getExteriorRing(), isOuter));
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      //Assert: interior ring is not empty
      rchList.add( createRingHull( poly.getInteriorRingN(i), ! isOuter));
    }
    return rchList;
  }
  
  private RingConcaveHull createRingHull(LinearRing ring, boolean isOuter) {
    int targetVertexCount = (int) Math.ceil(vertexCountFraction * (ring.getNumPoints() - 1));
    RingConcaveHull ringHull = new RingConcaveHull(ring, isOuter, targetVertexCount);
    return ringHull;
  }

  private Polygon polygonHull(Polygon poly, List<RingConcaveHull> polyHulls) {
    if (poly.isEmpty()) 
      return geomFactory.createPolygon();
    
    int ringIndex = 0;
    LinearRing shellHull = polyHulls.get(ringIndex++).getHull();
    List<LinearRing> holeHulls = new ArrayList<LinearRing>();
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      LinearRing hull = polyHulls.get(ringIndex++).getHull();
      //TODO: handle empty
      holeHulls.add(hull);
    }
    LinearRing[] resultHoles = GeometryFactory.toLinearRingArray(holeHulls);
    return geomFactory.createPolygon(shellHull, resultHoles);
  }
  
}
