package org.locationtech.jts.algorithm.hull;

import java.util.ArrayDeque;
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
    constraintRings = extractShells(constraint);

    geomFactory = constraint.getFactory();
  }
  
  public Geometry getHull() {
    Polygon frame = createFrame(constraint);
    ConstrainedDelaunayTriangulator cdt = new ConstrainedDelaunayTriangulator(frame);
    List<Tri> tris = cdt.getTriangles();
    Coordinate[] framePts = frame.getExteriorRing().getCoordinates();
    
    removeFrameEdgeTris(tris, framePts);
    removeEdgeTris();
    
    Geometry hull = buildHullPolygon(hullTris);
    return hull;
  }

  private void removeEdgeTris() {
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
    if (isTouchingSinglePolygon(tri))
      return true;
    //TODO: check if outside edge is longer than threshold
    return false;
  }

  private boolean isTouchingSinglePolygon(Tri tri) {
    Envelope envTri = envelope(tri);
    for (LinearRing ring : constraintRings) {
      //-- touching tri must be in ring envelope
      if (ring.getEnvelopeInternal().intersects(envTri)) {
        if (hasAllVertices(ring, tri))
          return true;
      }
    }
    return false;
  }

  private static boolean hasAllVertices(LinearRing ring, Tri tri) {
    for (int i = 0; i < 3; i++) {
      Coordinate v = tri.getCoordinate(i);
      if (! hasVertex(ring, v)) {
        return false;
      }
    }
    return true;
  }
  
  private static boolean hasVertex(LinearRing ring, Coordinate v) {
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

  private void removeFrameEdgeTris(List<Tri> tris, Coordinate[] framePts) {
    hullTris = new HashSet<Tri>();
    edgeTriQue = new ArrayDeque<Tri>();
    for (Tri tri : tris) {
      int index = frameIndex(tri, framePts);
      if (index >= 0) {
        //-- frame tris are adjacent to at most one edge tri
        int oppIndex = Tri.oppEdge(index);
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

  private Geometry buildHullPolygon(Set<Tri> hullTris) {
    Geometry triCoverage = Tri.toGeometry(hullTris, geomFactory);
    //TODO: add in input polygons
    Geometry hull = CoverageUnion.union(triCoverage);
    return hull;
  }
  
  private Polygon createFrame(Geometry polygons) {
    Envelope env = polygons.getEnvelopeInternal();
    double diam = env.getDiameter();
    Envelope envFrame = env.copy();
    envFrame.expandBy(4 * diam);
    Polygon frameOuter = (Polygon) geomFactory.toGeometry(envFrame);
    LinearRing shell = (LinearRing) frameOuter.getExteriorRing().copy();
    Polygon frame = geomFactory.createPolygon(shell, constraintRings);
    return frame;
  }

  private static LinearRing[] extractShells(Geometry polygons) {
    LinearRing[] rings = new LinearRing[polygons.getNumGeometries()];
    for (int i = 0; i < polygons.getNumGeometries(); i++) {
      Polygon consPoly = (Polygon) polygons.getGeometryN(i);
      rings[i] = (LinearRing) consPoly.getExteriorRing().copy();
    }
    return rings;
  }
}
