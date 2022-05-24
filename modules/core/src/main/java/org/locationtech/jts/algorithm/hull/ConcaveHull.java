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
import java.util.PriorityQueue;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Constructs a concave hull of a set of points.
 * A concave hull is a concave or convex polygon containing all the input points,
 * whose vertices are a subset of the vertices in the input.
 * A given set of points has a sequence of hulls of increasing concaveness,
 * determined by a numeric target parameter.
 * <p>
 * The concave hull is constructed by removing the longest outer edges 
 * of the Delaunay Triangulation of the points,
 * until the target criterion parameter is reached.
 * <p>
 * The target criteria are:
 * <ul>
 * <li><b>Maximum Edge Length</b> - the length of the longest edge of the hull is no larger
 * than this value.
 * <li><b>Maximum Edge Length Ratio</b> - determine the Maximum Edge Length 
 * as a fraction of the difference between the longest and shortest edge lengths 
 * in the Delaunay Triangulation.  
 * This normalizes the <b>Maximum Edge Length</b> to be scale-free.
 * A value of 1 produces the convex hull; a value of 0 produces maximum concaveness.
 * </ul>
 * The preferred criterion is the <b>Maximum Edge Length Ratio</b>, since it is 
 * scale-free and local (so that no assumption needs to be made about the 
 * total amount of concaveness present).
 * Other length criteria can be used by setting the Maximum Edge Length directly.
 * For example, use a length relative  to the longest edge length
 * in the Minimum Spanning Tree of the point set.
 * Or, use a length derived from the {@link #uniformGridEdgeLength(Geometry)} value.
 * <p>
 * The computed hull is always a single connected {@link Polygon}
 * (unless it is degenerate, in which case it will be a {@link Point} or a {@link LineString}).
 * This constraint may cause the concave hull to fail to meet the target criterion.
 * <p>
 * Optionally the concave hull can be allowed to contain holes.
 * Note that when using the area-based criterion 
 * this may result in substantially slower computation.
 * 
 * @author Martin Davis
 *
 */
public class ConcaveHull 
{
  /**
   * Computes the approximate edge length of
   * a uniform square grid having the same number of
   * points as a geometry and the same area as its convex hull.
   * This value can be used to determine a suitable length threshold value
   * for computing a concave hull.  
   * A value from 2 to 4 times the uniform grid length 
   * seems to produce reasonable results.
   *  
   * @param geom a geometry
   * @return the approximate uniform grid length
   */
  public static double uniformGridEdgeLength(Geometry geom) {
    double areaCH = geom.convexHull().getArea();
    int numPts = geom.getNumPoints();
    return Math.sqrt(areaCH / numPts);
  }
  
  /**
   * Computes a concave hull of the vertices in a geometry
   * using the target criterion of maximum edge length.
   * 
   * @param geom the input geometry
   * @param maxLength the target maximum edge length
   * @return the concave hull
   */
  public static Geometry concaveHullByLength(Geometry geom, double maxLength) {
    return concaveHullByLength(geom, maxLength, false);
  }
  
  /**
   * Computes a concave hull of the vertices in a geometry
   * using the target criterion of maximum edge length,
   * and optionally allowing holes.
   * 
   * @param geom the input geometry
   * @param maxLength the target maximum edge length
   * @param isHolesAllowed whether holes are allowed in the result
   * @return the concave hull
   */
  public static Geometry concaveHullByLength(Geometry geom, double maxLength, boolean isHolesAllowed) {
    ConcaveHull hull = new ConcaveHull(geom);
    hull.setMaximumEdgeLength(maxLength);
    hull.setHolesAllowed(isHolesAllowed);
    return hull.getHull();
  }
  
  /**
   * Computes a concave hull of the vertices in a geometry
   * using the target criterion of maximum edge length ratio.
   * The edge length ratio is a fraction of the length difference
   * between the longest and shortest edges 
   * in the Delaunay Triangulation of the input points. 
   * 
   * @param geom the input geometry
   * @param lengthRatio the target edge length factor
   * @return the concave hull
   */
  public static Geometry concaveHullByLengthRatio(Geometry geom, double lengthRatio) {
    return concaveHullByLengthRatio(geom, lengthRatio, false);
  }
  
  /**
   * Computes a concave hull of the vertices in a geometry
   * using the target criterion of maximum edge length factor,
   * and optionally allowing holes.
   * The edge length factor is a fraction of the length difference
   * between the longest and shortest edges 
   * in the Delaunay Triangulation of the input points. 
   * 
   * @param geom the input geometry
   * @param maxLength the target maximum edge length
   * @param isHolesAllowed whether holes are allowed in the result
   * @return the concave hull
   */
  public static Geometry concaveHullByLengthRatio(Geometry geom, double lengthRatio, boolean isHolesAllowed) {
    ConcaveHull hull = new ConcaveHull(geom);
    hull.setMaximumEdgeLengthRatio(lengthRatio);
    hull.setHolesAllowed(isHolesAllowed);
    return hull.getHull();
  }
  
  private Geometry inputGeometry;
  private double maxEdgeLength = 0.0;
  private double maxEdgeLengthRatio = -1;
  private boolean isHolesAllowed = false;
  private GeometryFactory geomFactory;


  /**
   * Creates a new instance for a given geometry.
   * 
   * @param geom the input geometry
   */
  public ConcaveHull(Geometry geom) {
    this.inputGeometry = geom;
    this.geomFactory = geom.getFactory();
  }
  
  /**
   * Sets the target maximum edge length for the concave hull.
   * The length value must be zero or greater.
   * <ul>
   * <li>The value 0.0 produces the concave hull of smallest area
   * that is still connected.
   * <li>Larger values produce less concave results.
   * A value equal or greater than the longest Delaunay Triangulation edge length
   * produces the convex hull.
   * </ul>
   * The {@link #uniformGridEdgeLength(Geometry)} value may be used as
   * the basis for estimating an appropriate target maximum edge length.
   * 
   * @param edgeLength a non-negative length
   * 
   * @see #uniformGridEdgeLength(Geometry)
   */
  public void setMaximumEdgeLength(double edgeLength) {
    if (edgeLength < 0)
      throw new IllegalArgumentException("Edge length must be non-negative");
    this.maxEdgeLength = edgeLength;
    maxEdgeLengthRatio = -1;
  }
  
  /**
   * Sets the target maximum edge length ratio for the concave hull.
   * The edge length ratio is a fraction of the difference
   * between the longest and shortest edge lengths 
   * in the Delaunay Triangulation of the input points.
   * It is a value in the range 0 to 1. 
   * <ul>
   * <li>The value 0.0 produces a concave hull of minimum area
   * that is still connected.
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
   * Gets the computed concave hull.
   * 
   * @return the concave hull
   */
  public Geometry getHull() {
    if (inputGeometry.isEmpty()) {
      return geomFactory.createPolygon();
    }
    List<HullTri> triList = HullTriangulation.createDelaunayTriangulation(inputGeometry);
    if (maxEdgeLengthRatio >= 0) {
      maxEdgeLength = computeTargetEdgeLength(triList, maxEdgeLengthRatio);
    }
    if (triList.isEmpty())
      return inputGeometry.convexHull();
    
    computeHull(triList);    

    Geometry hull = toGeometry(triList, geomFactory);
    return hull;
  }

  private static double computeTargetEdgeLength(List<HullTri> triList, 
      double edgeLengthRatio) {
    if (edgeLengthRatio == 0) return 0;
    double maxEdgeLen = -1;
    double minEdgeLen = -1;
    for (HullTri tri : triList) {
      for (int i = 0; i < 3; i++) {
        double len = tri.getCoordinate(i).distance(tri.getCoordinate(HullTri.next(i)));
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
  
  /**
   * Computes the concave hull using edge length as the target criterion.
   * The erosion is done in two phases: first the border, then any
   * internal holes (if required).
   * This allows an fast connection check to be used
   * when eroding holes,
   * which makes this much more efficient than the area-based algorithm.
   * 
   * @param triList
   */
  private void computeHull(List<HullTri> triList) {
    computeHullBorder(triList);
    if (isHolesAllowed) {
      computeHullHoles(triList);
    }
  }
  
  private void computeHullBorder(List<HullTri> triList) {
    PriorityQueue<HullTri> queue = createBorderQueue(triList);
    // remove tris in order of decreasing size (edge length)
    while (! queue.isEmpty()) {
      HullTri tri = queue.poll();
      
      if (isBelowLengthThreshold(tri)) 
        break;
      
      if (isRemovableBorder(tri)) {
        //-- the non-null adjacents are now on the border
        HullTri adj0 = (HullTri) tri.getAdjacent(0);
        HullTri adj1 = (HullTri) tri.getAdjacent(1);
        HullTri adj2 = (HullTri) tri.getAdjacent(2);
        
        tri.remove(triList);
        
        //-- add border adjacents to queue
        addBorderTri(adj0, queue);
        addBorderTri(adj1, queue);
        addBorderTri(adj2, queue);
      }
    }
  }
  
  private PriorityQueue<HullTri> createBorderQueue(List<HullTri> triList) {
    PriorityQueue<HullTri> queue = new PriorityQueue<HullTri>();
    for (HullTri tri : triList) {
      //-- add only border triangles which could be eroded
      // (if tri has only 1 adjacent it can't be removed because that would isolate a vertex)
      if (tri.numAdjacent() != 2)
        continue;
      tri.setSizeToBoundary();
      queue.add(tri);
    }
    return queue;
  }

  /**
   * Adds a Tri to the queue.
   * Only add tris with a single border edge,
   * sice otherwise that would risk isolating a vertex.
   * Sets the ordering size to the length of the border edge.
   * 
   * @param tri the Tri to add
   * @param queue the priority queue to add to
   */
  private void addBorderTri(HullTri tri, PriorityQueue<HullTri> queue) {
    if (tri == null) return;
    if (tri.numAdjacent() != 2) return;
    tri.setSizeToBoundary();
    queue.add(tri);
  }

  private boolean isBelowLengthThreshold(HullTri tri) {
    return tri.lengthOfBoundary() < maxEdgeLength;
  }
  
  private void computeHullHoles(List<HullTri> triList) {
    List<HullTri> candidateHoles = findCandidateHoles(triList, maxEdgeLength);
    // remove tris in order of decreasing size (edge length)
    for (HullTri tri : candidateHoles) {
      if (tri.isRemoved() 
          || tri.isBorder() 
          || tri.hasBoundaryTouch())
        continue;
      removeHole(triList, tri);
    }
  }
  
  /**
   * Finds tris which may be the start of holes.
   * Only tris which have a long enough edge and which do not touch the current hull
   * boundary are included.
   * This avoids the risk of disconnecting the result polygon.
   * The list is sorted in decreasing order of edge length.
   * 
   * @param triList
   * @param minEdgeLen minimum length of edges to consider
   * @return
   */
  private static List<HullTri> findCandidateHoles(List<HullTri> triList, double minEdgeLen) {
    List<HullTri> candidates = new ArrayList<HullTri>();
    for (HullTri tri : triList) {
      if (tri.getSize() < minEdgeLen) continue;
      boolean isTouchingBoundary = tri.isBorder() || tri.hasBoundaryTouch();
      if (! isTouchingBoundary) {
        candidates.add(tri);
      }
    }
    // sort by HullTri comparator - longest edge length first
    candidates.sort(null);
    return candidates;
  }
  
  /**
   * Erodes a hole starting at a given triangle, 
   * and eroding all adjacent triangles with boundary edge length above target.
   * @param triList the triangulation
   * @param triHole triangle which is a hole
   */
  private void removeHole(List<HullTri> triList, HullTri triHole) {
    PriorityQueue<HullTri> queue = new PriorityQueue<HullTri>();
    queue.add(triHole);
    
    while (! queue.isEmpty()) {
      HullTri tri = queue.poll();
      
      if (tri != triHole && isBelowLengthThreshold(tri)) 
        break;
      
      if (tri == triHole || isRemovableHole(tri)) {
        //-- the non-null adjacents are now on the border
        HullTri adj0 = (HullTri) tri.getAdjacent(0);
        HullTri adj1 = (HullTri) tri.getAdjacent(1);
        HullTri adj2 = (HullTri) tri.getAdjacent(2);
        
        tri.remove(triList);
        
        //-- add border adjacents to queue
        addBorderTri(adj0, queue);
        addBorderTri(adj1, queue);
        addBorderTri(adj2, queue);
      }
    }
  }
  
  private boolean isRemovableBorder(HullTri tri) {
    /**
     * Tri must have exactly 2 adjacent tris (i.e. a single boundary edge).
     * If it it has only 0 or 1 adjacent then removal would remove a vertex.
     * If it has 3 adjacent then it is not on border.
     */
    if (tri.numAdjacent() != 2) return false;
    /**
     * The tri cannot be removed if it is connecting, because
     * this would create more than one result polygon.
     */
    return ! tri.isConnecting();
  }
  
  private boolean isRemovableHole(HullTri tri) {
    /**
     * Tri must have exactly 2 adjacent tris (i.e. a single boundary edge).
     * If it it has only 0 or 1 adjacent then removal would remove a vertex.
     * If it has 3 adjacent then it is not connected to hole.
     */
    if (tri.numAdjacent() != 2) return false;
    /**
     * Ensure removal does not disconnect hull area.
     * This is a fast check which ensure holes and boundary
     * do not touch at single points.
     * (But it is slightly over-strict, since it prevents
     * any touching holes.)
     */
    return ! tri.hasBoundaryTouch();
  }
  
  private Geometry toGeometry(List<HullTri> triList, GeometryFactory geomFactory) {
    if (! isHolesAllowed) {
      return HullTriangulation.traceBoundaryPolygon(triList, geomFactory);
    }
    //-- in case holes are present use union (slower but handles holes)
    return HullTriangulation.union(triList, geomFactory);
  }
}
