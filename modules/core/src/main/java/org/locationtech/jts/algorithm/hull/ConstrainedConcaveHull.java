/*
 * Copyright (c) 2022 Martin Davis.
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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.overlayng.CoverageUnion;
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator;
import org.locationtech.jts.triangulate.tri.Tri;

public class ConstrainedConcaveHull {
  
  public static Geometry hull(Geometry constraints, double tolerance) {
    return hull(constraints, tolerance, false, false);
  }
  
  public static Geometry hull(Geometry constraints, double tolerance,
      boolean isHolesAllowed, boolean isKeepOuterEdge) {
    ConstrainedConcaveHull hull = new ConstrainedConcaveHull(constraints, tolerance);
    hull.setHolesAllowed(isHolesAllowed);
    hull.setKeepOuterEdge(isKeepOuterEdge);
    return hull.getHull();
  }
  
  private Geometry constraints;
  private double maxEdgeLength;
  private boolean isHolesAllowed = false;
  private boolean isKeepOuterEdge = true;
  
  private GeometryFactory geomFactory;
  private LinearRing[] constraintRings;
  
  private Set<Tri> hullTris;
  private ArrayDeque<Tri> borderTriQue;
  /**
   * Records the border edge of border tris,
   * so it can be tested for length and possible removal.
   */
  private Map<Tri, Integer> borderEdgeMap = new HashMap<Tri, Integer>();

  public ConstrainedConcaveHull(Geometry constraints, double maxLength) {
    this.constraints = constraints;
    this.maxEdgeLength = maxLength;
    geomFactory = constraints.getFactory();
    constraintRings = extractShellRings(constraints);
  }
  
  /**
   * Sets whether holes are allowed in the concave hull polygon.
   * 
   * @param isHolesAllowed true if holes are allowed in the result
   */
  public void setHolesAllowed(boolean isHolesAllowed) {
    this.isHolesAllowed = isHolesAllowed;
  }
  
  /**
   * Sets whether holes are allowed in the concave hull polygon.
   * 
   * @param isHolesAllowed true if holes are allowed in the result
   */
  public void setKeepOuterEdge(boolean isKeepOuterEdge) {
    this.isKeepOuterEdge = isKeepOuterEdge;
  }
  
  public Geometry getHull() {
    Polygon frame = createFrame(constraints.getEnvelopeInternal(), constraintRings);
    ConstrainedDelaunayTriangulator cdt = new ConstrainedDelaunayTriangulator(frame);
    List<Tri> tris = cdt.getTriangles();
    
    Coordinate[] framePts = frame.getExteriorRing().getCoordinates();
    removeFrameCornerTris(tris, framePts);
    
    removeBorderTris();
    if (isHolesAllowed) removeHoleTris();
    
    Geometry hull = buildHullPolygon(hullTris);
    return hull;
  }

  private void removeFrameCornerTris(List<Tri> tris, Coordinate[] frameCorners) {
    hullTris = new HashSet<Tri>();
    borderTriQue = new ArrayDeque<Tri>();
    for (Tri tri : tris) {
      int index = vertexIndex(tri, frameCorners);
      boolean isFrameCornerTri = index >= 0;
      if (isFrameCornerTri) {
        /**
         * Frame tris are adjacent to at most one border tri,
         * which is opposite the frame corner vertex.
         * The opposite tri may be another frame tri. 
         * This will be detected when it is processed,
         * since it is not in the hullTri set.
         */
        int oppIndex = Tri.oppEdge(index);
        addBorderTri(tri, oppIndex);
        tri.remove();
      }
      else {
        hullTris.add(tri);
      }
    }
  }

  /**
   * Get the tri vertex index of some point in a list, 
   * or -1 if none are vertices.
   * 
   * @param tri the tri to test for containing a point
   * @param pts the points to test
   * @return the vertex index of a point, or -1
   */
  private static int vertexIndex(Tri tri, Coordinate[] pts) {
    for (Coordinate p : pts) {
      int index = tri.getIndex(p);
      if (index >= 0) 
        return index;
    }
    return -1;
  }
  
  private void removeBorderTris() {
    while (! borderTriQue.isEmpty()) {
      Tri tri = borderTriQue.pop();
      //-- tri might have been removed already
      if (! hullTris.contains(tri)) {
        continue;
      }
      if (isRemovable(tri)) {
        addBorderTris(tri);
        removeBorderTri(tri);
      }
    }
  }

  private void removeHoleTris() {
    while (true) {
      Tri holeTri = findHoleTri(hullTris);
      if (holeTri == null)
        return;
      addBorderTris(holeTri);
      removeBorderTri(holeTri);
      removeBorderTris();
    }
  }
  
  private Tri findHoleTri(Set<Tri> tris) {
    for (Tri tri : tris) {
      if (isHoleTri(tri))
        return tri;
    }
    return null;
  }

  private boolean isHoleTri(Tri tri) {
    for (int i = 0; i < 3; i++) {
      if (tri.hasAdjacent(i)
          && tri.getLength(i) > maxEdgeLength)
         return true;
    }
    return false;
  }

  private boolean isRemovable(Tri tri) {
    //-- remove non-connecting edges if requested
    if (isKeepOuterEdge && isTouchingSinglePolygon(tri))
      return true;
    //-- check if outside edge is longer than threshold
    if (borderEdgeMap.containsKey(tri)) {
      int borderEdgeIndex = borderEdgeMap.get(tri);
      double edgeLen = tri.getLength(borderEdgeIndex);
      if (edgeLen > maxEdgeLength)
        return true;
    }
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

  private void addBorderTris(Tri tri) {
    addBorderTri(tri, 0);
    addBorderTri(tri, 1);
    addBorderTri(tri, 2);
  }
  
  /**
   * Adds an adjacent tri to the current border.
   * The adjacent edge is recorded as the border edge for the tri.
   * Note that only edges adjacent to another tri can become border edges.
   * Since constraint-adjacent edges do not have an adjacent tri,
   * they can never be on the border and thus will not be removed
   * due to being shorter than the length threshold.
   * The tri containing them may still be removed via another edge, however. 
   * 
   * @param tri the tri adjacent to the tri to be added to the border
   * @param index the index of the adjacent tri
   */
  private void addBorderTri(Tri tri, int index) {
    Tri adj = tri.getAdjacent( index );
    if (adj == null) 
      return;
    borderTriQue.add(adj);
    int borderEdgeIndex = adj.getIndex(tri);
    borderEdgeMap.put(adj, borderEdgeIndex);
  }

  private void removeBorderTri(Tri tri) {
    tri.remove();
    hullTris.remove(tri);
    borderEdgeMap.remove(tri);
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

  private Geometry buildHullPolygon(Set<Tri> hullTris) {
    //-- union triangulation
    Geometry triCoverage = Tri.toGeometry(hullTris, geomFactory);
    Geometry filler = CoverageUnion.union(triCoverage);
    
    if (filler.isEmpty()) {
      return constraints.copy();
    }
    //-- union with input constraints
    Geometry[] geoms = new Geometry[] { filler, constraints };
    GeometryCollection geomColl = geomFactory.createGeometryCollection(geoms);
    Geometry hull = CoverageUnion.union(geomColl);
    return hull;
  }
  
  private Polygon createFrame(Envelope constraintEnv, LinearRing[] constraintRings) {
    double diam = constraintEnv.getDiameter();
    Envelope envFrame = constraintEnv.copy();
    envFrame.expandBy(4 * diam);
    Polygon frameOuter = (Polygon) geomFactory.toGeometry(envFrame);
    LinearRing shell = (LinearRing) frameOuter.getExteriorRing().copy();
    Polygon frame = geomFactory.createPolygon(shell, constraintRings);
    return frame;
  }

  private static LinearRing[] extractShellRings(Geometry polygons) {
    LinearRing[] rings = new LinearRing[polygons.getNumGeometries()];
    for (int i = 0; i < polygons.getNumGeometries(); i++) {
      Polygon consPoly = (Polygon) polygons.getGeometryN(i);
      rings[i] = (LinearRing) consPoly.getExteriorRing().copy();
    }
    return rings;
  }
}
