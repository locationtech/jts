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
