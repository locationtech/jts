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
import java.util.Deque;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.triangulate.tri.Tri;

/**
 * Tris which are used to form a concave hull.
 * If a Tri has an edge (or edges) with no adjacent tri
 * the tri is on the border of the triangulation.
 * The edge is a boundary edge. 
 * The union of those edges
 * forms the (linear) boundary of the triangulation.
 * The triangulation area may be a Polygon or MultiPolygon, and may or may not contain holes.
 * 
 * @author Martin Davis
 *
 */
class HullTri extends Tri 
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
   * Sets the size to be the length of the boundary edges.
   * This is used when constructing hull without holes,
   * by erosion from the triangulation border.
   */
  public void setSizeToBoundary() {
    size = lengthOfBoundary();
  }
  
  public boolean isMarked() {
    return isMarked;
  }
  
  public void setMarked(boolean isMarked) {
    this.isMarked = isMarked;
  }
  
  public boolean isRemoved() {
    return ! hasAdjacent();
  }
  
  /**
   * Gets an index of a boundary edge, if there is one.
   * 
   * @return a boundary edge index, or -1
   */
  public int boundaryIndex() {
    if (isBoundary(0)) return 0;
    if (isBoundary(1)) return 1;
    if (isBoundary(2)) return 2;
    return -1;
  }
  
  /**
   * Gets the most CCW boundary edge index.
   * This assumes there is at least one non-boundary edge.
   * 
   * @return the CCW boundary edge index
   */
  public int boundaryIndexCCW() {
    int index = boundaryIndex();
    if (index < 0) return -1;
    int prevIndex = prev(index);
    if (isBoundary(prevIndex)) {
      return prevIndex;
    }
    return index;
  }
  
  /**
   * Gets the most CW boundary edge index.
   * This assumes there is at least one non-boundary edge.
   * 
   * @return the CW boundary edge index
   */
  public int boundaryIndexCW() {
    int index = boundaryIndex();
    if (index < 0) return -1;
    int nextIndex = next(index);
    if (isBoundary(nextIndex)) {
      return nextIndex;
    }
    return index;
  }
  
  /**
   * Tests if a tri is the only one connecting its 2 adjacents.
   * Assumes that the tri is on the border of the triangulation
   * and that the triangulation does not contain holes
   * 
   * @param tri the tri to test
   * @return true if the tri is the only connection
   */
  public boolean isConnecting() {
    int adj2Index = adjacent2VertexIndex();
    boolean isInterior = isInteriorVertex(adj2Index);
    return ! isInterior;
  }
  
  /**
   * Gets the index of a vertex which is adjacent to two other tris (if any).
   * 
   * @return the vertex index, or -1
   */
  public int adjacent2VertexIndex() {
    if (hasAdjacent(0) && hasAdjacent(1)) return 1;
    if (hasAdjacent(1) && hasAdjacent(2)) return 2;
    if (hasAdjacent(2) && hasAdjacent(0)) return 0;
    return -1;
  }
  
  /**
   * Tests whether some vertex of this Tri has degree = 1.
   * In this case it is not in any other Tris.
   * 
   * @param tri
   * @param triList
   * @return true if a vertex has degree 1
   */
  public int isolatedVertexIndex(List<HullTri> triList) {
    for (int i = 0; i < 3; i++) {
      if (degree(i, triList) <= 1)
        return i;
    }
    return -1;
  }
  
  public double lengthOfLongestEdge() {
    return Triangle.longestSideLength(p0, p1, p2);
  }
  
  double lengthOfBoundary() {
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
  
  /**
   * Tests if this tri has a vertex which is in the boundary,
   * but not in a boundary edge.
   * 
   * @return true if the tri touches the boundary at a vertex
   */
  public boolean hasBoundaryTouch() {
    for (int i = 0; i < 3; i++) {
      if (isBoundaryTouch(i))
        return true;
    }
    return false;
  }
  
  private boolean isBoundaryTouch(int index) {
    //-- If vertex is in a boundary edge it is not a touch
    if (isBoundary(index)) return false;
    if (isBoundary(prev(index))) return false;
    //-- if vertex is not in interior it is on boundary
    return ! isInteriorVertex(index);
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
  
  public static void clearMarks(List<HullTri> triList) {
    for (HullTri tri : triList) {
      tri.setMarked(false);
    }
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

  /**
   * Tests if a triangulation is edge-connected, if a triangle is removed.
   * NOTE: this is a relatively slow operation.
   * 
   * @param triList the triangulation
   * @param removedTri the triangle to remove
   * @return true if the triangulation is still connnected
   */
  public static boolean isConnected(List<HullTri> triList, HullTri removedTri) {
    if (triList.size() == 0) return false;
    clearMarks(triList);
    HullTri triStart = findTri(triList, removedTri);
    if (triStart == null) return false;
    markConnected(triStart, removedTri);
    removedTri.setMarked(true);
    return isAllMarked(triList);
  }
}