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
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.overlayng.CoverageUnion;
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator;
import org.locationtech.jts.triangulate.tri.Tri;

/**
 * Constructs a concave hull of a set of polygons, respecting 
 * the polygons as constraints.
 * A concave hull is a concave or convex polygon containing all the input polygons,
 * whose vertices are a subset of the vertices in the input.
 * A given set of polygons has a sequence of hulls of increasing concaveness,
 * determined by a numeric target parameter.
 * The computed hull "fills the gap" between the polygons,
 * and does not intersect their interior.
 * <p>
 * The concave hull is constructed by removing the longest outer edges 
 * of the constrained Delaunay Triangulation of the space between the polygons,
 * until the target criterion parameter is reached.
 * <p>
 * The target criteria are:
 * <ul>
 * <li><b>Maximum Edge Length</b> - the length of the longest edge between the polygons is no larger
 * than this value.
 * <li><b>Maximum Edge Length Ratio</b> - determine the Maximum Edge Length 
 * as a fraction of the difference between the longest and shortest edge lengths 
 * between the polygons.  
 * This provides a scale-free parameter.
 * A value of 1 produces the convex hull; a value of 0 produces the original polygons.
 * </ul>
 * Optionally the concave hull can be allowed to contain holes, 
 * via {@link #setHolesAllowed(boolean)}.
 * <p>
 * The hull can be specified as being "tight", via {@link #setTight(boolean)}.
 * This causes the result to follow the outer boundaries of the input polygons. 
 * <p>
 * Instead of the complete hull, the "fill area" between the input polygons 
 * can be computed using {@link #getFill()}.
 * <p>
 * The input polygons must form a valid {@link MultiPolygon}
 * (i.e. they must be non-overlapping and non-edge-adjacent).
 * If needed, a set of possibly-overlapping Polygons 
 * can be converted to a valid MultiPolygon
 * by using {@link Geometry#union()};
 * 
 * @author Martin Davis
 *
 */
public class ConcaveHullOfPolygons {
 
  /**
   * Computes a concave hull of set of polygons
   * using the target criterion of maximum edge length.
   * 
   * @param polygons the input polygons
   * @param maxLength the target maximum edge length
   * @return the concave hull
   */
  public static Geometry concaveHullByLength(Geometry polygons, double maxLength) {
    return concaveHullByLength(polygons, maxLength, false, false);
  }
  
  /**
   * Computes a concave hull of set of polygons
   * using the target criterion of maximum edge length,
   * and allowing control over whether the hull boundary is tight
   * and can contain holes.
   * 
   * @param polygons the input polygons
   * @param maxLength the target maximum edge length
   * @param isTight true if the hull should be tight to the outside of the polygons
   * @param isHolesAllowed true if holes are allowed in the hull polygon
   * @return the concave hull
   */
  public static Geometry concaveHullByLength(Geometry polygons, double maxLength,
      boolean isTight, boolean isHolesAllowed) {
    ConcaveHullOfPolygons hull = new ConcaveHullOfPolygons(polygons);
    hull.setMaximumEdgeLength(maxLength);
    hull.setHolesAllowed(isHolesAllowed);
    hull.setTight(isTight);
    return hull.getHull();
  }
  
  /**
   * Computes a concave hull of set of polygons
   * using the target criterion of maximum edge length ratio.
   * 
   * @param polygons the input polygons
   * @param lengthRatio the target maximum edge length ratio
   * @return the concave hull
   */
  public static Geometry concaveHullByLengthRatio(Geometry polygons, double lengthRatio) {
    return concaveHullByLengthRatio(polygons, lengthRatio, false, false);
  }
  
  /**
   * Computes a concave hull of set of polygons
   * using the target criterion of maximum edge length ratio,
   * and allowing control over whether the hull boundary is tight
   * and can contain holes.
   * 
   * @param polygons the input polygons
   * @param lengthRatio the target maximum edge length ratio
   * @param isTight true if the hull should be tight to the outside of the polygons
   * @param isHolesAllowed true if holes are allowed in the hull polygon
   * @return the concave hull
   */
  public static Geometry concaveHullByLengthRatio(Geometry polygons, double lengthRatio,
      boolean isTight, boolean isHolesAllowed) {
    ConcaveHullOfPolygons hull = new ConcaveHullOfPolygons(polygons);
    hull.setMaximumEdgeLengthRatio(lengthRatio);
    hull.setHolesAllowed(isHolesAllowed);
    hull.setTight(isTight);
    return hull.getHull();
  }
  
  /**
   * Computes a concave fill area between a set of polygons,
   * using the target criterion of maximum edge length.
   * 
   * @param polygons the input polygons
   * @param maxLength the target maximum edge length
   * @return the concave fill
   */
  public static Geometry concaveFillByLength(Geometry polygons, double maxLength) {
    ConcaveHullOfPolygons hull = new ConcaveHullOfPolygons(polygons);
    hull.setMaximumEdgeLength(maxLength);
    return hull.getFill();
  }
  
  /**
   * Computes a concave fill area between a set of polygons,
   * using the target criterion of maximum edge length ratio.
   * 
   * @param polygons the input polygons
   * @param lengthRatio the target maximum edge length ratio
   * @return the concave fill
   */
  public static Geometry concaveFillByLengthRatio(Geometry polygons, double lengthRatio) {
    ConcaveHullOfPolygons hull = new ConcaveHullOfPolygons(polygons);
    hull.setMaximumEdgeLengthRatio(lengthRatio);
    return hull.getFill();
  }
  
  private static final int FRAME_EXPAND_FACTOR = 4;
  private static final int NOT_SPECIFIED = -1;
  private static final int NOT_FOUND = -1;

  private Geometry inputPolygons;
  private double maxEdgeLength = 0.0;
  private double maxEdgeLengthRatio = NOT_SPECIFIED;
  private boolean isHolesAllowed = false;
  private boolean isTight = false;
  
  private GeometryFactory geomFactory;
  private LinearRing[] polygonRings;
  
  private Set<Tri> hullTris;
  private ArrayDeque<Tri> borderTriQue;
  /**
   * Records the edge index of the longest border edge for border tris,
   * so it can be tested for length and possible removal.
   */
  private Map<Tri, Integer> borderEdgeMap = new HashMap<Tri, Integer>();
  
  /**
   * Creates a new instance for a given geometry.
   * 
   * @param geom the input geometry
   */
  public ConcaveHullOfPolygons(Geometry polygons) {
    if (! (polygons instanceof Polygon || polygons instanceof MultiPolygon)) {
      throw new IllegalArgumentException("Input must be polygonal");
    }
    this.inputPolygons = polygons;
    geomFactory = inputPolygons.getFactory();
  }

  /**
   * Sets the target maximum edge length for the concave hull.
   * The length value must be zero or greater.
   * <ul>
   * <li>The value 0.0 produces the input polygons.
   * <li>Larger values produce less concave results.
   * Above a certain large value the result is the convex hull of the input.
   * <p>
   * The edge length ratio provides a scale-free parameter which
   * is intended to produce similar concave results for a variety of inputs.
   * 
   * @param edgeLength a non-negative length
   */
  public void setMaximumEdgeLength(double edgeLength) {
    if (edgeLength < 0)
      throw new IllegalArgumentException("Edge length must be non-negative");
    this.maxEdgeLength = edgeLength;
    maxEdgeLengthRatio = NOT_SPECIFIED;
  }
  
  /**
   * Sets the target maximum edge length ratio for the concave hull.
   * The edge length ratio is a fraction of the difference
   * between the longest and shortest edge lengths 
   * in the Delaunay Triangulation of the area between the input polygons.
   * (Roughly speaking, it is a fraction of the difference between
   * the shortest and longest distances between the input polygons.)
   * It is a value in the range 0 to 1. 
   * <ul>
   * <li>The value 0.0 produces the original input polygons.
   * <li>The value 1.0 produces the convex hull.
   * <ul> 
   * 
   * @param edgeLengthRatio a length factor value between 0 and 1
   */
  public void setMaximumEdgeLengthRatio(double edgeLengthRatio) {
    if (edgeLengthRatio < 0 || edgeLengthRatio > 1)
      throw new IllegalArgumentException("Edge length ratio must be in range [0,1]");
    this.maxEdgeLengthRatio = edgeLengthRatio;
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
   * Sets whether the boundary of the hull polygon is kept
   * tight to the outer edges of the input polygons.
   * 
   * @param isTight true if the boundary is kept tight
   */
  public void setTight(boolean isTight) {
    this.isTight = isTight;
  }
  
  /**
   * Gets the computed concave hull.
   * 
   * @return the concave hull
   */
  public Geometry getHull() {
    if (inputPolygons.isEmpty()) {
      return createEmptyHull();
    }
    buildHullTris();
    Geometry hull = createHullGeometry(hullTris, true);
    return hull;
  }

  /**
   * Gets the concave fill, which is the area between the input polygons, 
   * subject to the concaveness control parameter.
   * 
   * @return the concave fill
   */
  public Geometry getFill() {
    isTight = true;
    if (inputPolygons.isEmpty()) {
      return createEmptyHull();
    }
    buildHullTris();
    Geometry fill = createHullGeometry(hullTris, false);
    return fill;
  }
  
  private Geometry createEmptyHull() {
    return geomFactory.createPolygon();
  }
  
  private void buildHullTris() {
    polygonRings = extractShellRings(inputPolygons);
    Polygon frame = createFrame(inputPolygons.getEnvelopeInternal(), polygonRings, geomFactory);
    ConstrainedDelaunayTriangulator cdt = new ConstrainedDelaunayTriangulator(frame);
    List<Tri> tris = cdt.getTriangles();
    //System.out.println(tris);
    
    Coordinate[] framePts = frame.getExteriorRing().getCoordinates();
    if (maxEdgeLengthRatio >= 0) {
      maxEdgeLength = computeTargetEdgeLength(tris, framePts, maxEdgeLengthRatio);
    }
    
    hullTris = removeFrameCornerTris(tris, framePts);
    
    removeBorderTris();
    if (isHolesAllowed) removeHoleTris();
  }

  private static double computeTargetEdgeLength(List<Tri> triList, 
      Coordinate[] frameCorners,
      double edgeLengthRatio) {
    if (edgeLengthRatio == 0) return 0;
    double maxEdgeLen = -1;
    double minEdgeLen = -1;
    for (Tri tri : triList) {
      //-- don't include frame triangles
      if (isFrameTri(tri, frameCorners))
        continue;
      
      for (int i = 0; i < 3; i++) {
        //-- constraint edges are not used to determine ratio
        if (! tri.hasAdjacent(i))
          continue;
        
        double len = tri.getLength(i);
        if (len > maxEdgeLen) 
          maxEdgeLen = len;
        if (minEdgeLen < 0 || len < minEdgeLen)
          minEdgeLen = len;
      }
    }
    //-- if ratio = 1 ensure all edges are included
    if (edgeLengthRatio == 1) 
      return 2 * maxEdgeLen;
    
    return edgeLengthRatio * (maxEdgeLen - minEdgeLen) + minEdgeLen;
  }

  private static boolean isFrameTri(Tri tri, Coordinate[] frameCorners) {
    int index = vertexIndex(tri, frameCorners);
    boolean isFrameTri = index >= 0;
    return isFrameTri;
  }
  
  private Set<Tri> removeFrameCornerTris(List<Tri> tris, Coordinate[] frameCorners) {
    Set<Tri> hullTris = new HashSet<Tri>();
    borderTriQue = new ArrayDeque<Tri>();
    for (Tri tri : tris) {
      int index = vertexIndex(tri, frameCorners);
      boolean isFrameTri = index != NOT_FOUND;
      if (isFrameTri) {
        /**
         * Frame tris are adjacent to at most one border tri,
         * which is opposite the frame corner vertex.
         * The opposite tri may be another frame tri. 
         * This is detected when it is processed,
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
    return hullTris;
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
    return NOT_FOUND;
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
    //-- remove non-bridging tris if keeping hull boundary tight
    if (isTight && isTouchingSinglePolygon(tri))
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

  /**
   * Tests whether a triangle touches a single polygon at all vertices.
   * If so, it is a candidate for removal if the hull polygon
   * is being kept tight to the outer boundary of the input polygons.
   * Tris which touch more than one polygon are called "bridging".
   * 
   * @param tri
   * @return true if the tri touches a single polygon
   */
  private boolean isTouchingSinglePolygon(Tri tri) {
    Envelope envTri = envelope(tri);
    for (LinearRing ring : polygonRings) {
      //-- optimization heuristic: a touching tri must be in ring envelope
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

  private Geometry createHullGeometry(Set<Tri> hullTris, boolean isIncludeInput) {
    if (! isIncludeInput && hullTris.isEmpty())
      return createEmptyHull();
    
    //-- union triangulation
    Geometry triCoverage = Tri.toGeometry(hullTris, geomFactory);
    //System.out.println(triCoverage);
    Geometry fillGeometry = CoverageUnion.union(triCoverage);
    
    if (! isIncludeInput) {
      return fillGeometry;
    }
    if (fillGeometry.isEmpty()) {
      return inputPolygons.copy();
    }
    //-- union with input polygons
    Geometry[] geoms = new Geometry[] { fillGeometry, inputPolygons };
    GeometryCollection geomColl = geomFactory.createGeometryCollection(geoms);
    Geometry hull = CoverageUnion.union(geomColl);
    return hull;
  }
  
  /**
   * Creates a rectangular "frame" around the input polygons,
   * with the input polygons as holes in it.
   * The frame is large enough that the constrained Delaunay triangulation
   * of it should contain the convex hull of the input as edges.
   * The frame corner triangles can be removed to produce a 
   * triangulation of the space around and between the input polygons.
   * 
   * @param polygonsEnv
   * @param polygonRings
   * @param geomFactory 
   * @return the frame polygon
   */
  private static Polygon createFrame(Envelope polygonsEnv, LinearRing[] polygonRings, GeometryFactory geomFactory) {
    double diam = polygonsEnv.getDiameter();
    Envelope envFrame = polygonsEnv.copy();
    envFrame.expandBy(FRAME_EXPAND_FACTOR * diam);
    Polygon frameOuter = (Polygon) geomFactory.toGeometry(envFrame);
    LinearRing shell = (LinearRing) frameOuter.getExteriorRing().copy();
    Polygon frame = geomFactory.createPolygon(shell, polygonRings);
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
