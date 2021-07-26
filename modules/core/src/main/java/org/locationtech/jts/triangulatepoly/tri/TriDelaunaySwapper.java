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
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.triangulate.quadedge.TrianglePredicate;

/**
 * Improves a triangulation of {@link Tri}s via
 * iterated Delaunay swapping.
 * 
 * @author mdavis
 *
 */
public class TriDelaunaySwapper {
  
  public static void swap(List<Tri> triList) {
    TriDelaunaySwapper swapper = new TriDelaunaySwapper(triList);
    swapper.swap();
  }
  
  private static int MAX_ITERATION = 200;
  private List<Tri> triList;

  private TriDelaunaySwapper(List<Tri> triList) {
    this.triList = triList;
  }

  private void swap() {
    for (int i = 0; i < MAX_ITERATION; i++) {
      int improveCount = swapAll(triList);
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
  private int swapAll(List<Tri> triList) {
    int swapCount = 0;
    for (int i = 0; i < triList.size() - 1; i++) {
      Tri tri = triList.get(i);
      for (int j = 0; j < 3; j++) {
        Tri neighb = tri.getAdjacent(j);
        //tri.validateAdjacent(j);
        if ( doSwap(tri, neighb) ) {
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
  private boolean doSwap(Tri tri0, Tri tri1) {
    if ( tri0 == null || tri1 == null ) {
      return false;
    }
    //tri0.validate();
    //tri1.validate();

    int index0 = tri0.getIndex(tri1);
    int index1 = tri1.getIndex(tri0);

    Coordinate adj0 = tri0.getCoordinate(index0);
    Coordinate adj1 = tri0.getCoordinate(Tri.next(index0));
    Coordinate opp0 = tri0.getCoordinate(Tri.opp(index0));
    Coordinate opp1 = tri1.getCoordinate(Tri.opp(index1));
    
    /**
     * The candidate new edge is opp0 - opp1. 
     * Check if it is inside the quadrilateral formed by the two triangles. 
     * This is the case iff the quadrilateral is convex.
     */
    if ( ! isConvex(adj0, opp1, adj1, opp0) ) {
      return false;
    }
    /**
     * The candidate edge is inside the quadrilateral. Check to see if the flipping
     * criteria is met. The flipping criteria is to flip iff the two triangles are
     * not Delaunay (i.e. one of the opposite vertices is in the circumcircle of the
     * other triangle).
     */
    if ( ! isDelaunay(opp0, adj0, adj1, opp1) ) {
      //tri0.flip(tri1);
      //-- already have index and vertex data so use it
      tri0.swap(tri1, index0, index1, adj0, adj1, opp0, opp1);
      return true;
    }
    return false;
  }

  /**
   * Tests if a quadrilateral is convex.
   * 
   * @param p0 a vertex of the quadrilateral
   * @param p1 a vertex of the quadrilateral
   * @param p2 a vertex of the quadrilateral
   * @param p3 a vertex of the quadrilateral
   * @return true if the quadrilateral is convex
   */
  private static boolean isConvex(Coordinate p0, Coordinate p1, Coordinate p2, Coordinate p3) {
    int dir0 = Orientation.index(p0, p1, p2);
    int dir1 = Orientation.index(p1, p2, p3);
    boolean isConvex = dir0 == dir1;
    return isConvex;
  }  

  private static boolean isDelaunay(Coordinate c0, Coordinate a0, Coordinate a1, Coordinate c1) {
    return ! (isInCircle(c0, a0, a1, c1) || isInCircle(c1, a1, a0, c0));
  }

  private static boolean isInCircle(Coordinate a, Coordinate b, Coordinate c, Coordinate p) {
    if ( Triangle.isCCW(a, b, c) ) {
      return TrianglePredicate.isInCircleRobust(a, b, c, p);
    }
    return TrianglePredicate.isInCircleRobust(a, c, b, p);
  }

}