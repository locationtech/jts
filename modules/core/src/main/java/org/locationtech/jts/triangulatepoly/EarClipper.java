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
import org.locationtech.jts.triangulatepoly.tri.Triangulation;

class EarClipper {
  
  public static List<Tri> clip(List<Coordinate> polyBoundary) {
    EarClipper clipper = new EarClipper(polyBoundary);
    return clipper.compute();
  }
  
  /**
   * The polygon coordinates are maintain in CW order. This means that for convex
   * interior angles, the vertices forming the angle are in CW orientation.
   */
  private final List<Coordinate> polyVertex;
  private final int[] vertexNext;
  private int vertexSize;
  // index for current candidate corner
  public int[] cornerCandidate;
  // first available coordinate index
  private int firstAvailable;

  public EarClipper(List<Coordinate> polyVertex) {
    this.polyVertex = polyVertex;
    
    // init working storage
    vertexSize = polyVertex.size() - 1;
    vertexNext = new int[vertexSize];
    for (int i = 0; i < vertexSize; i++) {
      vertexNext[i] = i + 1;
    }
    vertexNext[vertexSize - 1] = 0;
    cornerCandidate = new int[3];
    cornerCandidate[0] = 0;
    cornerCandidate[1] = 1;
    cornerCandidate[2] = 2;
    firstAvailable = 0;
  }

  public List<Tri> compute() {
    List<Tri> triList = new ArrayList<Tri>();

    int cornerCount = 0;
    //--- find next convex corner (which is the next candidate ear)
    Coordinate[] corner = new Coordinate[3];
    nextCorner(false);
    readCorner(corner);
    while (true) {
      // foundEar = false;
      while (! isCW(corner)) {
        // delete the "corner" if three points are in the same line
        if ( isCollinear(corner) ) {
          removeCorner();
          if ( vertexSize < 3 ) {
            return triList;
          }
        }
        nextCorner(true);
        readCorner(corner);
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
       * Always skip to next corner - produces fewer skinny triangles.
       */
      nextCorner(true);
      readCorner(corner);
    }
  }

  private boolean isCW(Coordinate[] pts) {
    return Orientation.CLOCKWISE == Orientation.index(pts[0], pts[1], pts[2]);
  }
  
  private static boolean isCollinear(Coordinate[] pts) {
    return Orientation.COLLINEAR == Orientation.index(pts[0], pts[1], pts[2]);
  }
  
  /**
   * Check if the current corner candidate is valid without using cover()
   * @param corner 
   * 
   * @return
   */
  private boolean isValidEar(Coordinate[] corner) {
    double angle = Angle.angleBetweenOriented(corner[0], corner[1], corner[2]);
    int currIndex = nextIndex(firstAvailable);
    int prevIndex = firstAvailable;
    Coordinate prevV = polyVertex.get(prevIndex);
    for (int i = 0; i < vertexSize; i++) {
      Coordinate v = polyVertex.get(currIndex);
      // when corner[1] occurs, cannot simply skip. It might occur
      // multiple times and is connected with a hole
      if ( v.equals2D(corner[1]) ) {
        Coordinate nextTmp = polyVertex.get(nextIndex(currIndex));
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
      // not valid if vertex is contained in tri
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
    if ( firstAvailable == cornerCandidate[1] ) {
      firstAvailable = vertexNext[cornerCandidate[1]];
    }
    vertexNext[cornerCandidate[0]] = vertexNext[cornerCandidate[1]];
    vertexNext[cornerCandidate[1]] = -1;
    vertexSize--;
    nextCorner(false);
  }

  /**
   * Read the corner candidate coordinates based on current candidate index
   */
  private void readCorner(Coordinate[] corner) {
    corner[0] = polyVertex.get(cornerCandidate[0]); 
    corner[1] = polyVertex.get(cornerCandidate[1]); 
    corner[2] = polyVertex.get(cornerCandidate[2]); 
  }

  /**
   * Set to next corner candidate.
   * 
   * @param moveFirst if corner[0] should be moved to next available coordinates.
   */
  private void nextCorner(boolean moveFirst) {
    if ( vertexSize < 3 ) {
      return;
    }
    if ( moveFirst ) {
      cornerCandidate[0] = nextIndex(cornerCandidate[0]);
    }
    cornerCandidate[1] = nextIndex(cornerCandidate[0]);
    cornerCandidate[2] = nextIndex(cornerCandidate[1]);
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

  public Polygon toGeometry() {
    GeometryFactory fact = new GeometryFactory();
    CoordinateList coordList = new CoordinateList();
    int availIndex = firstAvailable;
    for (int i = 0; i < vertexSize; i++) {
      Coordinate v = polyVertex.get(availIndex);
      availIndex = nextIndex(availIndex);
      // if (i < shellCoordAvailable.length && shellCoordAvailable.get(i))
      coordList.add(v, true);
    }
    coordList.closeRing();
    return fact.createPolygon(fact.createLinearRing(coordList.toCoordinateArray()), null);
  }
}