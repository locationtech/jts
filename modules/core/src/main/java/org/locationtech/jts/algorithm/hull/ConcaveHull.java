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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.operation.overlayng.CoverageUnion;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.quadedge.QuadEdge;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.locationtech.jts.triangulate.quadedge.TriangleVisitor;
import org.locationtech.jts.triangulate.tri.Tri;
import org.locationtech.jts.triangulate.tri.TriangulationBuilder;
import org.locationtech.jts.util.Assert;

/**
 * Constructs a concave hull of a set of points.
 * The hull is constructed by removing the longest outer edges 
 * of the Delaunay Triangulation of the points
 * until a target criterium is reached.
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
 * <li><b>Maximum Area Ratio</b> - the ratio of the concave hull area to the convex hull area 
 * will be no larger than this value. 
 * A value of 1 produces the convex hull; a value of 0 produces maximum concaveness.
 * </ul>
 * The preferred criterium is the <b>Maximum Edge Length Ratio</b>, since it is 
 * scale-free and local (so that no assumption needs to be made about the 
 * total amount of concavity present).
 * Other length criteria can be used by setting the Maximum Edge Length.
 * For example, use a length relative  to the longest edge length
 * in the Minimum Spanning Tree of the point set.
 * Or, use a length derived from the {@link #uniformGridEdgeLength(Geometry)} value.
 * <p>
 * The computed hull is always a single connected {@link Polygon}
 * (unless it is degenerate, in which case it will be a {@link Point} or a {@link LineString}).
 * This constraint may cause the concave hull to fail to meet the target criteria.
 * <p>
 * Optionally the concave hull can be allowed to contain holes.
 * Note that this may result in substantially slower computation,
 * and it can produce results of lower quality.
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
   * Computes the concave hull of the vertices in a geometry
   * using the target criteria of maximum edge length.
   * 
   * @param geom the input geometry
   * @param maxLength the target maximum edge length
   * @return the concave hull
   */
  public static Geometry concaveHullByLength(Geometry geom, double maxLength) {
    return concaveHullByLength(geom, maxLength, false);
  }
  
  /**
   * Computes the concave hull of the vertices in a geometry
   * using the target criteria of maximum edge length,
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
   * Computes the concave hull of the vertices in a geometry
   * using the target criteria of maximum edge length ratio.
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
   * Computes the concave hull of the vertices in a geometry
   * using the target criteria of maximum edge length factor,
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
  
  /**
   * Computes the concave hull of the vertices in a geometry
   * using the target criteria of maximum area ratio.
   * 
   * @param geom the input geometry
   * @param areaRatio the target maximum area ratio
   * @return the concave hull
   */
  public static Geometry concaveHullByArea(Geometry geom, double areaRatio) {
    ConcaveHull hull = new ConcaveHull(geom);
    hull.setMaximumAreaRatio(areaRatio);
    return hull.getHull();
  }
  
  private Geometry inputGeometry;
  private double maxEdgeLength = 0.0;
  private double maxEdgeLengthRatio = -1;
  private double maxAreaRatio = 0.0; 
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
      throw new IllegalArgumentException("Edge length ratio must be in range [0,1]e");
    this.maxEdgeLengthRatio = edgeLengthRatio;
  }
  
  /**
   * Sets the target maximum concave hull area as a ratio of the convex hull area.
   * It is a value in the range 0 to 1. 
   * <ul>
   * <li>The value 0.0 produces a concave hull with the smallest area
   * that is still connected.
   * <li>The value 1.0 produces the convex hull 
   * (unless a maximum edge length is also specified).
   * </ul>
   * 
   * @param areaRatio a ratio value between 0 and 1
   */
  public void setMaximumAreaRatio(double areaRatio) {
    if (areaRatio < 0 || areaRatio > 1)
      throw new IllegalArgumentException("Area ratio must be in range [0,1]");
    this.maxAreaRatio = areaRatio;
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
    List<HullTri> triList = createDelaunayTriangulation(inputGeometry);
    if (maxEdgeLengthRatio >= 0) {
      maxEdgeLength = computeTargetEdgeLength(triList, maxEdgeLengthRatio);
    }
    if (triList.isEmpty())
      return inputGeometry.convexHull();
    computeHull(triList);
    Geometry hull = toPolygon(triList, geomFactory);
    return hull;
  }

  private static double computeTargetEdgeLength(List<? extends Tri> triList, 
      double edgeLengthRatio) {
    if (edgeLengthRatio == 0) return 0;
    double maxEdgeLen = -1;
    double minEdgeLen = -1;
    for (Tri tri : triList) {
      for (int i = 0; i < 3; i++) {
        double len = tri.getCoordinate(i).distance(tri.getCoordinate(Tri.next(i)));
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

  private void computeHull(List<HullTri> triList) {
    //-- used if area is the threshold criteria
    double areaConvex = Tri.area(triList);
    double areaConcave = areaConvex;
    
    PriorityQueue<HullTri> queue = initQueue(triList);
    // remove tris in order of decreasing size (edge length)
    while (! queue.isEmpty()) {
      if (isBelowAreaThreshold(areaConcave, areaConvex))
        break;

      HullTri tri = queue.poll();
      
      if (isBelowLengthThreshold(tri)) 
        break;
      
      if (isRemovable(tri, triList)) {
        //-- the non-null adjacents are now on the border
        HullTri adj0 = (HullTri) tri.getAdjacent(0);
        HullTri adj1 = (HullTri) tri.getAdjacent(1);
        HullTri adj2 = (HullTri) tri.getAdjacent(2);
        
        //-- remove tri
        tri.remove();
        triList.remove(tri);
        areaConcave -= tri.getArea();
        
        //-- if holes not allowed, add new border adjacents to queue
        if (! isHolesAllowed) {
          addBorderTri(adj0, queue);
          addBorderTri(adj1, queue);
          addBorderTri(adj2, queue);
        }
      }
    }
  }

  private PriorityQueue<HullTri> initQueue(List<HullTri> triList) {
    PriorityQueue<HullTri> queue = new PriorityQueue<HullTri>();
    for (HullTri tri : triList) {
      if (! isHolesAllowed) {
        //-- add only border triangles which could be eroded
        // (if tri has only 1 adjacent it can't be removed because that would isolate a vertex)
        if (tri.numAdjacent() != 2)
          continue;
        tri.setSizeToBorder();
      }
      queue.add(tri);
    }
    return queue;
  }

  /**
   * Adds a Tri to the queue.
   * Only add tris with a single border edge.
   * The ordering size is the length of the border edge.
   * 
   * @param tri the Tri to add
   * @param queue the priority queue
   */
  private void addBorderTri(HullTri tri, PriorityQueue<HullTri> queue) {
    if (tri == null) return;
    if (tri.numAdjacent() != 2) return;
    tri.setSizeToBorder();
    queue.add(tri);
  }
    
  private boolean isBelowAreaThreshold(double areaConcave, double areaConvex) {
    return areaConcave / areaConvex <= maxAreaRatio;
  }

  private boolean isBelowLengthThreshold(HullTri tri) {
    double len = 0;
    if (isHolesAllowed) {
      len = tri.lengthOfLongestEdge();
    }
    else {
      len = tri.lengthOfBorder();
    }
    return len < maxEdgeLength;
  }

  /**
   * Tests whether a Tri can be removed while preserving 
   * the connectivity of the hull.
   * 
   * @param tri the Tri to test
   * @param triList 
   * @return true if the Tri can be removed
   */
  private boolean isRemovable(HullTri tri, List<HullTri> triList) {
    if (isHolesAllowed) {
      /**
       * Don't remove if that would separate a single vertex
       */
      if (hasVertexSingleAdjacent(tri, triList))
        return false;
      return HullTri.isConnected(triList, tri);
    }
    
    //-- compute removable for no holes allowed
    int numAdj = tri.numAdjacent();
    /**
     * Tri must have exactly 2 adjacent tris.
     * If it it has only 0 or 1 adjacent then removal would remove a vertex.
     * If it has 3 adjacent then it is not on border.
     */
    if (numAdj != 2) return false;
    /**
     * The tri cannot be removed if it is connecting, because
     * this would create more than one result polygon.
     */
    return ! isConnecting(tri);
  }

  private static boolean hasVertexSingleAdjacent(HullTri tri, List<HullTri> triList) {
    for (int i = 0; i < 3; i++) {
      if (degree(tri.getCoordinate(i), triList) <= 1)
        return true;
    }
    return false;
  }

  /**
   * The degree of a Tri vertex is the number of tris containing it.
   * This must be done by searching the entire triangulation, 
   * since the containing tris may not be adjacent or edge-connected. 
   * 
   * @param v a vertex coordinate
   * @param triList a triangulation
   * @return the degree of the vertex
   */
  private static int degree(Coordinate v, List<HullTri> triList) {
    int degree = 0;
    for (HullTri tri : triList) {
      for (int i = 0; i < 3; i++) {
        if (v.equals2D(tri.getCoordinate(i)))
          degree++;
      }
    }
    return degree;
  }

  /**
   * Tests if a tri is the only one connecting its 2 adjacents.
   * Assumes that the tri is on the border of the triangulation
   * and that the triangulation does not contain holes
   * 
   * @param tri the tri to test
   * @return true if the tri is the only connection
   */
  private static boolean isConnecting(Tri tri) {
    int adj2Index = adjacent2VertexIndex(tri);
    boolean isInterior = isInteriorVertex(tri, adj2Index);
    return ! isInterior;
  }

  /**
   * A vertex of a triangle is interior if it 
   * is fully surrounded by triangles.
   * 
   * @param tri a tri containing the vertex
   * @param index the vertex index
   * @return true if the vertex is interior
   */
  private static boolean isInteriorVertex(Tri triStart, int index) {
    Tri curr = triStart;
    int currIndex = index;
    do {
      Tri adj = curr.getAdjacent(currIndex);
      if (adj == null) return false;
      int adjIndex = adj.getIndex(curr);
      curr = adj;
      currIndex = Tri.next(adjIndex);
    }
    while (curr != triStart);
    return true;
  }

  private static int adjacent2VertexIndex(Tri tri) {
    if (tri.hasAdjacent(0) && tri.hasAdjacent(1)) return 1;
    if (tri.hasAdjacent(1) && tri.hasAdjacent(2)) return 2;
    if (tri.hasAdjacent(2) && tri.hasAdjacent(0)) return 0;
    return -1;
  }
  
  private static class HullTri extends Tri 
      implements Comparable<HullTri> 
  {
    private double size;
    private boolean isMarked = false;
    
    public HullTri(Coordinate p0, Coordinate p1, Coordinate p2) {
      super(p0, p1, p2);
      this.size = lengthOfLongestEdge();
    }

    public double getSize() {
      return size;
    }
    
    /**
     * Sets the size to be the length of the border edges.
     * This is used when constructing hull without holes,
     * by erosion from the triangulation border.
     */
    public void setSizeToBorder() {
      size = lengthOfBorder();
    }
    
    public boolean isMarked() {
      return isMarked;
    }
    
    public void setMarked(boolean isMarked) {
      this.isMarked = isMarked;
    }

    public boolean isBorder() {
      return isBorder(0) || isBorder(1) || isBorder(2);
    }
    
    public boolean isBorder(int index) {
      return ! hasAdjacent(index);
    }
    
    public int borderIndex() {
      if (isBorder(0)) return 0;
      if (isBorder(1)) return 1;
      if (isBorder(2)) return 2;
      return -1;
    }
    
    /**
     * Gets the most CCW border edge index.
     * This assumes there is at least one non-border edge.
     * 
     * @return the CCW border edge index
     */
    public int borderIndexCCW() {
      int index = borderIndex();
      int prevIndex = prev(index);
      if (isBorder(prevIndex)) {
        return prevIndex;
      }
      return index;
    }
    
    /**
     * Gets the most CW border edge index.
     * This assumes there is at least one non-border edge.
     * 
     * @return the CW border edge index
     */
    public int borderIndexCW() {
      int index = borderIndex();
      int nextIndex = next(index);
      if (isBorder(nextIndex)) {
        return nextIndex;
      }
      return index;
    }
    
    public double lengthOfLongestEdge() {
      return Triangle.longestSideLength(p0, p1, p2);
    }
    
    private double lengthOfBorder() {
      double len = 0.0;
      for (int i = 0; i < 3; i++) {
        if (! hasAdjacent(i)) {
          len += getCoordinate(i).distance(getCoordinate(Tri.next(i)));
        }
      }
      return len;
    }
    
    public HullTri nextBorderTri() {
      HullTri tri = this;
      //-- start at first non-border edge CW
      int index = next(borderIndexCW());
      //-- scan CCW around vertex for next border tri
      do {
        HullTri adjTri = (HullTri) tri.getAdjacent(index);
        if (adjTri == this)
          throw new IllegalStateException("No outgoing border edge found");
        index = next(adjTri.getIndex(tri));
        tri = adjTri;
      }
      while (! tri.isBorder(index));
      return (tri);
    }

    /**
     * PriorityQueues sort in ascending order.
     * To sort with the largest at the head,
     * smaller sizes must compare as greater than larger sizes.
     * (i.e. the normal numeric comparison is reversed).
     * If the sizes are identical (which should be an infrequent case),
     * the areas are compared, with larger areas sorting before smaller.
     * (The rationale is that larger areas indicate an area of lower point density,
     * which is more likely to be in the exterior of the computed shape.)
     * This improves the determinism of the queue ordering. 
     */
    @Override
    public int compareTo(HullTri o) {
      /**
       * If size is identical compare areas to ensure a (more) deterministic ordering.
       * Larger areas sort before smaller ones.
       */
      if (size == o.size) {
        return -Double.compare(this.getArea(), o.getArea());
      }
      return -Double.compare(size, o.size);
    }
    
    public static boolean isConnected(List<HullTri> triList, HullTri exceptTri) {
      if (triList.size() == 0) return false;
      clearMarks(triList);
      HullTri triStart = findTri(triList, exceptTri);
      if (triStart == null) return false;
      markConnected(triStart, exceptTri);
      exceptTri.setMarked(true);
      return isAllMarked(triList);
    }
    
    public static void clearMarks(List<HullTri> triList) {
      for (HullTri tri : triList) {
        tri.setMarked(false);
      }
    }
    
    public static HullTri findTri(List<HullTri> triList, Tri exceptTri) {
      for (HullTri tri : triList) {
        if (tri != exceptTri) return tri;
      }
      return null;
    }
    
    public static boolean isAllMarked(List<HullTri> triList) {
      for (HullTri tri : triList) {
        if (! tri.isMarked())
          return false;
      }
      return true;
    }
    
    public static void markConnected(HullTri triStart, Tri exceptTri) {
      Deque<HullTri> queue = new ArrayDeque<HullTri>();
      queue.add(triStart);
      while (! queue.isEmpty()) {
        HullTri tri = queue.pop();
        tri.setMarked(true);
        for (int i = 0; i < 3; i++) {
          HullTri adj = (HullTri) tri.getAdjacent(i);
          //-- don't connect thru this tri
          if (adj == exceptTri)
            continue;
          if (adj != null && ! adj.isMarked() ) {
            queue.add(adj);
          }
        }
      }
    }
  }
  
  private static List<HullTri> createDelaunayTriangulation(Geometry geom) {
    //TODO: implement a DT on Tris directly?
    DelaunayTriangulationBuilder dt = new DelaunayTriangulationBuilder();
    dt.setSites(geom);
    QuadEdgeSubdivision subdiv = dt.getSubdivision();
    List<HullTri> triList = toTris(subdiv);
    return triList;
  }
  
  private static List<HullTri> toTris(QuadEdgeSubdivision subdiv) {
    HullTriVisitor visitor = new HullTriVisitor();
    subdiv.visitTriangles(visitor, false);
    List<HullTri> triList = visitor.getTriangles();
    TriangulationBuilder.build(triList);
    return triList;
  }
  
  private static class HullTriVisitor implements TriangleVisitor {
    private List<HullTri> triList = new ArrayList<HullTri>();

    public HullTriVisitor() {
    }

    public void visit(QuadEdge[] triEdges) {
      Coordinate p0 = triEdges[0].orig().getCoordinate();
      Coordinate p1 = triEdges[1].orig().getCoordinate();
      Coordinate p2 = triEdges[2].orig().getCoordinate();
      HullTri tri;
      if (Triangle.isCCW(p0, p1, p2)) {
        tri = new HullTri(p0, p2, p1);
      }
      else {
        tri = new HullTri(p0, p1, p2);
      }
      triList.add(tri);
    }
    
    public List<HullTri> getTriangles() {
      return triList;
    }
  }

  private Geometry toPolygon(List<HullTri> triList, GeometryFactory geomFactory) {
    if (! isHolesAllowed) {
      return extractPolygon(triList, geomFactory);
    }
    //-- in case holes are present use union (slower but handles holes)
    return union(triList, geomFactory);
  }

  private Geometry extractPolygon(List<HullTri> triList, GeometryFactory geomFactory) {
    if (triList.size() == 1) {
      Tri tri = triList.get(0);
      return tri.toPolygon(geomFactory);
    }
    Coordinate[] pts = traceBorder(triList);
    return geomFactory.createPolygon(pts);
  }

  private static Geometry union(List<? extends Tri> triList, GeometryFactory geomFactory) {
    List<Polygon> polys = new ArrayList<Polygon>();
    for (Tri tri : triList) {
      Polygon poly = tri.toPolygon(geomFactory);
      polys.add(poly);
    }
    return CoverageUnion.union(geomFactory.buildGeometry(polys));
  }
  
  /**
   * Extracts the coordinates along the border of a triangulation,
   * by tracing CW around the border triangles.
   * Assumption: there are at least 2 tris, they are connected,
   * and there are no holes.
   * So each tri has at least one non-border edge, and there is only one border.
   * 
   * @param triList the triangulation
   * @return the border of the triangulation
   */
  private static Coordinate[] traceBorder(List<HullTri> triList) {
    HullTri triStart = findBorderTri(triList);
    CoordinateList coordList = new CoordinateList();
    HullTri tri = triStart;
    do {
      int borderIndex = tri.borderIndexCCW();
      //-- add border vertex
      coordList.add(tri.getCoordinate(borderIndex).copy(), false);
      int nextIndex = Tri.next(borderIndex);
      //-- if next edge is also border, add it and move to next
      if (tri.isBorder(nextIndex)) {
        coordList.add(tri.getCoordinate(nextIndex).copy(), false);
        borderIndex = nextIndex;
      }
      //-- find next border tri CCW around non-border edge
      tri = tri.nextBorderTri();
    } while (tri != triStart);
    coordList.closeRing();
    return coordList.toCoordinateArray();
  }
  
  public static HullTri findBorderTri(List<HullTri> triList) {
    for (HullTri tri : triList) {
      if (tri.isBorder()) return tri;
    }
    Assert.shouldNeverReachHere("No border triangles found");
    return null;
  }
}
