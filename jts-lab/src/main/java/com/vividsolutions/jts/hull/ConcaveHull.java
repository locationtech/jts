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

package com.vividsolutions.jts.hull;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
import com.vividsolutions.jts.triangulate.quadedge.QuadEdgeSubdivision;
import com.vividsolutions.jts.triangulate.quadedge.QuadEdgeTriangle;

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
