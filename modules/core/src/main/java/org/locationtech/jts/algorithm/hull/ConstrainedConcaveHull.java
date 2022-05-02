package org.locationtech.jts.algorithm.hull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.overlayng.CoverageUnion;
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator;
import org.locationtech.jts.triangulate.tri.Tri;

public class ConstrainedConcaveHull {
  
  public static Geometry hull(Geometry constraint, double tolerance) {
    ConstrainedConcaveHull hull = new ConstrainedConcaveHull(constraint, tolerance);
    return hull.getHull();
  }
  
  private Geometry constraint;
  private GeometryFactory geomFactory;
  private LinearRing[] constraintRings;
  private Set<Tri> hullTris;
  private ArrayDeque<Tri> edgeTriQue;

  ConstrainedConcaveHull(Geometry constraint, double tolerance) {
    this.constraint = constraint;
    constraintRings = constraintRings(constraint);

    geomFactory = constraint.getFactory();
  }
  
  public Geometry getHull() {
    Polygon mask = createMask(constraint);
    ConstrainedDelaunayTriangulator cdt = new ConstrainedDelaunayTriangulator(mask);
    List<Tri> tris = cdt.getTriangles();
    Coordinate[] framePts = mask.getExteriorRing().getCoordinates();
    removeFrameTris(tris, framePts);
    
    pruneEdgeTris();
    Geometry hull = createHullPolygon(hullTris);
    return hull;
  }



  private void pruneEdgeTris() {
    while (! edgeTriQue.isEmpty()) {
      Tri tri = edgeTriQue.pop();
      //-- tri might have been removed already
      if (! hullTris.contains(tri)) {
        continue;
      }
      if (isRemovable(tri)) {
        addAdjacent(tri, edgeTriQue);
        tri.remove();
        hullTris.remove(tri);
      }
    }
  }

  private void addAdjacent(Tri tri, ArrayDeque<Tri> que) {
    for (int i = 0; i < 3; i++) {
      Tri adj = tri.getAdjacent(i);
      if (adj != null) {
        que.add(adj);
      }
    }
  }

  private boolean isRemovable(Tri tri) {
    if (isAllVerticesInSamePolygon(tri))
      return true;
    //TODO: check if outside edge is longer than threshold
    return false;
  }

  private boolean isAllVerticesInSamePolygon(Tri tri) {
    Envelope envTri = envelope(tri);
    for (LinearRing ring : constraintRings) {
      if (ring.getEnvelopeInternal().intersects(envTri)) {
        if (containsAllVertices(ring, tri))
          return true;
      }
    }
    return false;
  }

  private boolean containsAllVertices(LinearRing ring, Tri tri) {
    for (int i = 0; i < 3; i++) {
      Coordinate v = tri.getCoordinate(i);
      if (! containsVertex(ring, v)) {
        return false;
      }
    }
    return true;
  }
  
  private boolean containsVertex(LinearRing ring, Coordinate v) {
    for(int i = 1; i < ring.getNumPoints(); i++) {
      if (v.equals2D(ring.getCoordinateN(i))) {
        return true;
      }
    }
    return false;
  }

  private static Envelope envelope(Tri tri) {
    Envelope env = new Envelope(tri.getCoordinate(0), tri.getCoordinate(1));
    env.expandToInclude(tri.getCoordinate(2));
    return env;
  }

  private void removeFrameTris(List<Tri> tris, Coordinate[] framePts) {
    hullTris = new HashSet<Tri>();
    edgeTriQue = new ArrayDeque<Tri>();
    for (Tri tri : tris) {
      int index = frameIndex(tri, framePts);
      if (index >= 0) {
        //-- frame tris are adjacent to at most one edge tri
        int oppIndex = tri.oppEdge(index);
        Tri edgeTri = tri.getAdjacent(oppIndex);
        if (edgeTri != null) {
          edgeTriQue.add(edgeTri);
        }
        tri.remove();
      }
      else {
        hullTris.add(tri);
      }
    }
  }

  private static int frameIndex(Tri tri, Coordinate[] framePts) {
    for (Coordinate framePt : framePts) {
      int index = tri.getIndex(framePt);
      if (index >= 0) 
        return index;
    }
    return -1;
  }

  private Geometry createHullPolygon(Set<Tri> hullTris) {
    List<Tri> hullTriList =  new ArrayList<Tri>();
    hullTriList.addAll(hullTris);
    Geometry triCoverage = Tri.toGeometry(hullTriList, geomFactory);
    //TODO: add in input polygons
    Geometry hull = CoverageUnion.union(triCoverage);
    return hull;
  }
  
  private Polygon createMask(Geometry polygons) {
    Envelope env = polygons.getEnvelopeInternal();
    double diam = env.getDiameter();
    Envelope envFrame = env.copy();
    envFrame.expandBy(4 * diam);
    Polygon frame = (Polygon) geomFactory.toGeometry(envFrame);
    LinearRing shell = (LinearRing) frame.getExteriorRing().copy();
    Polygon mask = geomFactory.createPolygon(shell, constraintRings);
    return mask;
  }

  private LinearRing[] constraintRings(Geometry polygons) {
    LinearRing[] rings = new LinearRing[polygons.getNumGeometries()];
    for (int i = 0; i < polygons.getNumGeometries(); i++) {
      Polygon consPoly = (Polygon) polygons.getGeometryN(i);
      rings[i] = (LinearRing) consPoly.getExteriorRing().copy();
    }
    return rings;
  }
}
