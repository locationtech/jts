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

/**
 * Constructs a concave hull of a set of points.
 * The hull is constructed by eroding the Delaunay Triangulation of the points
 * until specified target criteria are reached.
 * The target criteria are:
 * <ul>
 * <li><b>Maximum Edge Length</b> - the length of the longest edge of the hull will be no larger
 * than this value
 * <li><b>Maximum Area Ratio</b> - the ratio of the concave hull area to the convex hull area 
 * will be no larger than this value
 * </ul>
 * Usually only a single criteria is specified, but both may be provided.
 * <p>
 * The computed hull is always a single connected Polygon.
 * This constraint may cause the concave hull to not fully meet the target criteria.
 * <p>
 * Optionally the concave hull can be allowed to contain holes.
 * Note that this may be substantially slower than not permitting holes,
 * and it can produce results of low quality.
 * 
 * @author mdavis
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
  public static double uniformEdgeLength(Geometry geom) {
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
  
  public static Geometry concaveHullByLength(Geometry geom, double maxLength, boolean isHolesAllowed) {
    ConcaveHull hull = new ConcaveHull(geom);
    hull.setMaximumEdgeLength(maxLength);
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
  private double maxAreaRatio = 0.0; 
  private boolean isHolesAllowed = false;
  private GeometryFactory geomFactory;


  public ConcaveHull(Geometry geom) {
    this.inputGeometry = geom;
    this.geomFactory = geom.getFactory();
  }
  
  /**
   * Sets the target maximum edge length for the concave hull.
   * A value of 0.0 produces a concave hull of minimum area
   * that is still connected.
   * The {@link #uniformEdgeLength(Geometry)} may be used as
   * the basis for estimating an appropriate target maximum edge length.
   * 
   * @param edgeLength a non-negative length
   * 
   * @see #uniformEdgeLength(Geometry)
   */
  public void setMaximumEdgeLength(double edgeLength) {
    if (edgeLength < 0)
      throw new IllegalArgumentException("Edge length must be non-negative");
    this.maxEdgeLength = edgeLength;
  }
  
  /**
   * Sets the target maximum concave hull area as a ratio of the convex hull area.
   * A value of 1.0 produces the convex hull 
   * (unless a maximum edge length is also specified).
   * A value of 0.0 produces a concave hull with the smallest area
   * that is still connected.
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
    List<HullTri> triList = createDelaunayTriangulation(inputGeometry);
    computeHull(triList);
    Geometry hull = toPolygonal(triList, geomFactory);
    return hull;
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
   * This must be done by searching the entire subdivision, 
   * since the containing tris may not be adjacent or edge-connected. 
   * 
   * @param v the vertex coordinate
   * @param triList the tri subdivision
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
   * Assumes that the tri is on the border of the tri subdivision
   * and that the subdivision does not contain holes
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
     * by erosion from the subdivision border.
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
      return getAdjacent(0) == null
          || getAdjacent(1) == null
          || getAdjacent(2) == null;
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
    
    /**
     * PriorityQueues sort in ascending order.
     * To sort with the largest at the head,
     * smaller sizes must compare as greater than larger sizes.
     * (i.e. the normal numeric comparison is reversed)
     */
    @Override
    public int compareTo(HullTri o) {
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

  private static Geometry toPolygonal(List<? extends Tri> triList, GeometryFactory geomFactory) {
    //TODO: make this more efficient by tracing border
    List<Polygon> polys = new ArrayList<Polygon>();
    for (Tri tri : triList) {
      Polygon poly = tri.toPolygon(geomFactory);
      polys.add(poly);
    }
    return CoverageUnion.union(geomFactory.buildGeometry(polys));
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
      HullTri tri = new HullTri(p0, p1, p2);
      triList.add(tri);
    }
    
    public List<HullTri> getTriangles() {
      return triList;
    }
  }


}
