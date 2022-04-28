package org.locationtech.jts.algorithm.hull;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator;
import org.locationtech.jts.triangulate.tri.Tri;

public class ConstrainedConcaveHull {
  
  public static Geometry hull(Geometry constraint, double tolerance) {
    ConstrainedConcaveHull hull = new ConstrainedConcaveHull(constraint, tolerance);
    return hull.getHull();
  }
  
  private Geometry constraint;
  private GeometryFactory geomFactory;

  ConstrainedConcaveHull(Geometry constraint, double tolerance) {
    this.constraint = constraint;
    geomFactory = constraint.getFactory();
  }
  
  public Geometry getHull() {
    Polygon mask = createMask(constraint);
    ConstrainedDelaunayTriangulator cdt = new ConstrainedDelaunayTriangulator(mask);
    List<Tri> tris = cdt.getTriangles();
    Coordinate[] framePts = mask.getExteriorRing().getCoordinates();
    List<Tri> hullTris = removeFrameTris(tris, framePts);
    
    Geometry hull = Tri.toGeometry(hullTris, geomFactory).union();
    return hull;
  }

  private List<Tri> removeFrameTris(List<Tri> tris, Coordinate[] framePts) {
    List<Tri> hullTris = new ArrayList<Tri>();
    for (Tri tri : tris) {
      if (isFrameTri(tri, framePts)) {
        tri.remove();
      }
      else {
        hullTris.add(tri);
      }
    }
    return hullTris;
  }

  private boolean isFrameTri(Tri tri, Coordinate[] framePts) {
    for (int i = 0; i < 4; i++) {
      if (hasVertex(tri, framePts[i])) 
        return true;
    }
    return false;
  }

  private boolean hasVertex(Tri tri, Coordinate p) {
    for (int i = 0; i < 3; i++) {
      if (tri.getCoordinate(i).equals2D(p))
        return true;
    }
    return false;
  }

  private Polygon createMask(Geometry polygons) {
    Envelope env = polygons.getEnvelopeInternal();
    double diam = env.getDiameter();
    Envelope envFrame = env.copy();
    envFrame.expandBy(4 * diam);
    Polygon frame = (Polygon) geomFactory.toGeometry(envFrame);
    LinearRing shell = (LinearRing) frame.getExteriorRing().copy();
    LinearRing[] holes = new LinearRing[polygons.getNumGeometries()];
    for (int i = 0; i < polygons.getNumGeometries(); i++) {
      Polygon consPoly = (Polygon) polygons.getGeometryN(i);
      holes[i] = (LinearRing) consPoly.getExteriorRing().copy();
    }
    Polygon mask = geomFactory.createPolygon(shell, holes);
    return mask;
  }
}
