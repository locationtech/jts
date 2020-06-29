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
package org.locationtech.jts.hull;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeTriangle;

public class ConcaveHull {
  
  private Geometry geom;
  private double tolerance;

  public ConcaveHull(Geometry geom, double tolerance) {
    this.geom = geom;
    this.tolerance = tolerance;
  }
  
  public Geometry getResult() {
    QuadEdgeSubdivision subdiv = buildDelaunay();
    List tris = extractTriangles(subdiv);
    Geometry hull = computeHull(tris);
    return hull;
  }

  private List extractTriangles(QuadEdgeSubdivision subdiv) {
    List qeTris = QuadEdgeTriangle.createOn(subdiv);
    return qeTris;
  }

  private Geometry computeHull(List tris) {
    return null;
    
  }

  private QuadEdgeSubdivision buildDelaunay() {
    DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(geom);
    return builder.getSubdivision();
  }
}
