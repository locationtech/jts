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
package org.locationtech.jts.triangulatepoly;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.triangulatepoly.tri.Tri;

/**
 * Triangulates a polygon via the classic Ear Clipping technique.
 * The polygon is provided as a closed list of vertices
 * defining its boundary.
 * 
 * It must not self-cross, but may self-touch and have coincident edges.
 * Polygons with holes may be triangulated by preparing them 
 * with {@link PolygonHoleJoiner}.
 * 
 * @author mdavis
 *
 */
class PolygonEarClipper {
  
  public static List<Tri> clip(List<Coordinate> polyBoundary) {
    PolygonEarClipper clipper = new PolygonEarClipper(polyBoundary);
    return clipper.compute();
  }
  
  /**
   * The polygon vertices are maintain in CW order. This means that for convex
   * interior angles, the vertices forming the angle are in CW orientation.
   */
  private final List<Coordinate> vertex;
  
  private final int[] vertexNext;
  private int vertexSize;
  // first available vertex index
  private int vertexFirst;
  
  // indices for current candidate corner
  public int[] cornerCandidate;

  public PolygonEarClipper(List<Coordinate> polyVertex) {
    this.vertex = polyVertex;
    
    // init working storage
    vertexSize = this.vertex.size() - 1;
    vertexNext = createNextList(vertexSize);
    vertexFirst = 0;
    
    cornerCandidate = new int[3];
    cornerCandidate[0] = 0;
    cornerCandidate[1] = 1;
    cornerCandidate[2] = 2;
  }

  private static int[] createNextList(int size) {
    int[] next = new int[size];
    for (int i = 0; i < size; i++) {
      next[i] = i + 1;
    }
    next[size - 1] = 0;
    return next;
  }

  public List<Tri> compute() {
    List<Tri> triList = new ArrayList<Tri>();

    int cornerCount = 0;
    Coordinate[] corner = new Coordinate[3];
    nextCorner(false, corner);
    while (true) {
      //--- find next convex corner, which is the next candidate ear
      while (! isCW(corner)) {
        // delete the corner if it is flat
        if ( isCollinear(corner) ) {
          removeCorner();
          if ( vertexSize < 3 ) {
            return triList;
          }
        }
        nextCorner(true, corner);
      }
      cornerCount++;
      if ( cornerCount > 2 * vertexSize ) {
        throw new IllegalStateException("Unable to find a convex corner which is a valid ear");
      }
      if ( isValidEar(corner) ) {
        triList.add(Tri.create(corner));
        removeCorner();
        if ( vertexSize < 3 ) {
          return triList;
        }
        cornerCount = 0;
      }
      /**
       * Always skip to next corner - creates fewer skinny triangles.
       */
      nextCorner(true, corner);
    }
  }
  
  /**
   * Tests if a corner triangle is a valid ear.
   * This is the case if:
   * <ol>
   * <li></li>
   * <li></li>
   * </ol>
   * 
   * @param corner the corner triangle to check
   * @return true if the corner is valid
   */
  private boolean isValidEar(Coordinate[] corner) {
    double angle = Angle.angleBetweenOriented(corner[0], corner[1], corner[2]);
    int currIndex = nextIndex(vertexFirst);
    int prevIndex = vertexFirst;
    Coordinate prevV = vertex.get(prevIndex);
    for (int i = 0; i < vertexSize; i++) {
      Coordinate v = vertex.get(currIndex);
      /**
       * when corner[1] occurs, cannot simply skip. It might occur
       * multiple times and is connected with a hole
       */
      if ( v.equals2D(corner[1]) ) {
        Coordinate nextTmp = vertex.get(nextIndex(currIndex));
        double aOut = Angle.angleBetweenOriented(corner[0], corner[1], nextTmp);
        double aIn = Angle.angleBetweenOriented(corner[0], corner[1], prevV);
        if ( aOut > 0 && aOut < angle ) {
          return false;
        }
        if ( aIn > 0 && aIn < angle ) {
          return false;
        }
        if ( aOut == 0 && aIn == angle ) {
          return false;
        }
        prevV = v;
        prevIndex = currIndex;
        currIndex = nextIndex(currIndex);
        continue;
      }
      prevV = v;
      prevIndex = currIndex;
      currIndex = nextIndex(currIndex);
      if ( v.equals2D(corner[0]) || v.equals2D(corner[2]) ) {
        continue;
      }
      // not valid if vertex is contained in corner triangle
      if ( Triangle.intersects(corner[0], corner[1], corner[2], v) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Remove the corner apex and update the candidate corner location.
   */
  private void removeCorner() {
    if ( vertexFirst == cornerCandidate[1] ) {
      vertexFirst = vertexNext[cornerCandidate[1]];
    }
    vertexNext[cornerCandidate[0]] = vertexNext[cornerCandidate[1]];
    vertexNext[cornerCandidate[1]] = -1;
    vertexSize--;
    nextCorner(false, null);
  }

  /**
   * Set to next corner candidate.
   * Read the corner vertices if required.
   * 
   * @param moveFirst if corner[0] should be moved to next available coordinates.
   * @param corner an array for the corner vertices
   */
  private void nextCorner(boolean moveFirst, Coordinate[] corner) {
    if ( vertexSize < 3 ) {
      return;
    }
    if ( moveFirst ) {
      cornerCandidate[0] = nextIndex(cornerCandidate[0]);
    }
    cornerCandidate[1] = nextIndex(cornerCandidate[0]);
    cornerCandidate[2] = nextIndex(cornerCandidate[1]);
    
    if (corner != null) {
      corner[0] = vertex.get(cornerCandidate[0]); 
      corner[1] = vertex.get(cornerCandidate[1]); 
      corner[2] = vertex.get(cornerCandidate[2]); 
    }
  }

  /**
   * Get the index of the next available shell coordinate starting from the given
   * index.
   * 
   * @param index candidate position
   * @return index of the next available shell coordinate
   */
  private int nextIndex(int index) {
    return vertexNext[index];
  }

  private static boolean isCW(Coordinate[] pts) {
    return Orientation.CLOCKWISE == Orientation.index(pts[0], pts[1], pts[2]);
  }
  
  private static boolean isCollinear(Coordinate[] pts) {
    return Orientation.COLLINEAR == Orientation.index(pts[0], pts[1], pts[2]);
  }
  
  public Polygon toGeometry() {
    GeometryFactory fact = new GeometryFactory();
    CoordinateList coordList = new CoordinateList();
    int index = vertexFirst;
    for (int i = 0; i < vertexSize; i++) {
      Coordinate v = vertex.get(index);
      index = nextIndex(index);
      // if (i < shellCoordAvailable.length && shellCoordAvailable.get(i))
      coordList.add(v, true);
    }
    coordList.closeRing();
    return fact.createPolygon(fact.createLinearRing(coordList.toCoordinateArray()));
  }
}