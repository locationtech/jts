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
package org.locationtech.jts.triangulatepoly.tri;

import java.util.List;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.triangulate.quadedge.TrianglePredicate;

/**
 * Improves the quality of a triangulation of {@link Tri}s via
 * iterated Delaunay swapping.
 * This produces the Constrained Delaunay Triangulation
 * with the constraints being the boundary of the input triangulation.
 * 
 * @author mdavis
 *
 */
public class TriDelaunayImprover {
  
  /**
   * Improves the quality of a triangulation of {@link Tri}s via
   * iterated Delaunay swapping.
   * The Tris are assumed to be linked into a Triangulation
   * (e.g. via {@link TriangulationBuilder}).
   * 
   * @param triList the list of Tris to swap.
   */
  public static void improve(List<Tri> triList) {
    TriDelaunayImprover swapper = new TriDelaunayImprover(triList);
    swapper.improve();
  }
  
  private static int MAX_ITERATION = 200;
  private List<Tri> triList;

  private TriDelaunayImprover(List<Tri> triList) {
    this.triList = triList;
  }

  private void improve() {
    for (int i = 0; i < MAX_ITERATION; i++) {
      int improveCount = improveScan(triList);
      //System.out.println("improve #" + i + " - count = " + improveCount);
      if ( improveCount == 0 ) {
        return;
      }
    }
  }

  /**
   * Improves a triangulation by examining pairs of triangles with a
   * common edge, forming a quadrilateral, and testing if swapping the diagonal of
   * the quadrilateral would produce two new triangles with larger minimum
   * interior angles.
   * 
   * @return the number of swaps that were made
   */
  private int improveScan(List<Tri> triList) {
    int swapCount = 0;
    for (int i = 0; i < triList.size() - 1; i++) {
      Tri tri = triList.get(i);
      for (int j = 0; j < 3; j++) {
        Tri neighb = tri.getAdjacent(j);
        //tri.validateAdjacent(j);
        if ( improveNonDelaunay(tri, neighb) ) {
          // TODO: improve performance by only rescanning tris adjacent to swaps?
          swapCount++;
        }
      }
    }
    return swapCount;
  }

  /**
   * Does a swap of two Tris if the Delaunay condition is not met.
   * 
   * @param tri0 a Tri
   * @param tri1 a Tri
   * @return true if the triangles were swapped
   */
  private boolean improveNonDelaunay(Tri tri0, Tri tri1) {
    if ( tri0 == null || tri1 == null ) {
      return false;
    }
    //tri0.validate();
    //tri1.validate();

    int index0 = tri0.getIndex(tri1);
    int index1 = tri1.getIndex(tri0);

    Coordinate adj0 = tri0.getCoordinate(index0);
    Coordinate adj1 = tri0.getCoordinate(Tri.next(index0));
    Coordinate opp0 = tri0.getCoordinate(Tri.oppVertex(index0));
    Coordinate opp1 = tri1.getCoordinate(Tri.oppVertex(index1));
    
    /**
     * The candidate new edge is opp0 - opp1. 
     * Check if it is inside the quadrilateral formed by the two triangles. 
     * This is the case if the quadrilateral is convex.
     */
    if ( ! isConvex(adj0, adj1, opp0, opp1) ) {
      return false;
    }
    
    /**
     * The candidate edge is inside the quadrilateral. Check to see if the flipping
     * criteria is met. The flipping criteria is to flip if the two triangles are
     * not Delaunay (i.e. one of the opposite vertices is in the circumcircle of the
     * other triangle).
     */
    if ( ! isDelaunay(adj0, adj1, opp0, opp1) ) {
      //tri0.flip(tri1);
      //-- already have index and vertex data so use it
      tri0.swap(tri1, index0, index1, adj0, adj1, opp0, opp1);
      return true;
    }
    return false;
  }

  /**
   * Tests if the quadrilateral formed by two adjacent triangles is convex.
   * opp0-adj0-adj1 and opp1-adj1-adj0 are the triangle corners 
   * and hence are known to be convex.
   * The quadrilateral is convex if the other corners opp0-adj0-opp1
   * and opp1-adj1-opp0 have the same orientation (since at least one must be convex).
   * 
   * @param adj0 adjacent edge vertex 0
   * @param adj1 adjacent edge vertex 1
   * @param opp0 corner vertex of triangle 0
   * @param opp1 corner vertex of triangle 1
   * @return true if the quadrilateral is convex
   */
  private static boolean isConvex(Coordinate adj0, Coordinate adj1, Coordinate opp0, Coordinate opp1) {
    int dir0 = Orientation.index(opp0, adj0, opp1);
    int dir1 = Orientation.index(opp1, adj1, opp0);
    boolean isConvex = dir0 == dir1;
    return isConvex;
  }  

  /**
   * Tests if either of a pair of adjacent triangles satisfy the Delaunay condition.
   * The triangles are opp0-adj0-adj1 and opp1-adj1-adj0.
   * The Delaunay condition is not met if one opposite vertex 
   * lies is in the circumcircle of the other triangle.
   * 
   * @param adj0 adjacent edge vertex 0
   * @param adj1 adjacent edge vertex 1
   * @param opp0 corner vertex of triangle 0
   * @param opp1 corner vertex of triangle 1
   * @return true if the triangles are Delaunay
   */
  private static boolean isDelaunay(Coordinate adj0, Coordinate adj1, Coordinate opp0, Coordinate opp1) {
    if (isInCircle(adj0, adj1, opp0, opp1)) return false; 
    if (isInCircle(adj1, adj0, opp1, opp0)) return false;
    return true;
  }

  /**
   * Tests whether a point p is in the circumcircle of a triangle abc
   * (oriented clockwise).
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point
   * 
   * @return true if the point is in the circumcircle
   */
  private static boolean isInCircle(Coordinate a, Coordinate b, Coordinate c, Coordinate p) {
    return TrianglePredicate.isInCircleRobust(a, c, b, p);
  }

}