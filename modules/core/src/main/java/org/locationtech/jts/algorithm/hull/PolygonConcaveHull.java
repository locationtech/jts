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

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.triangulate.polygon.VertexSequencePackedRtree;

/**
 * 
 * @author Martin Davis
 *
 */
public class PolygonConcaveHull {
  
  private static final int NO_VERTEX_INDEX = -1;

  public static Geometry hull(Geometry geom, double vertexCountFraction) {
    Geometry geomNorm = geom.norm();
    Coordinate[] pts = geomNorm.getCoordinates();
    PolygonConcaveHull hull = new PolygonConcaveHull(pts, vertexCountFraction);
    return hull.getResult();
  }
  
  /**
   * The polygon vertices are provided in CW orientation. 
   * Thus for convex interior angles 
   * the vertices forming the angle are in CW orientation.
   */
  private final Coordinate[] vertex;
  
  private final int[] vertexNext;
  private int vertexSize;
  // first available vertex index
  private int vertexFirst;
  
  // indices for current corner
  private int[] cornerIndex;
  
  /**
   * Indexing vertices improves ear intersection testing performance a lot.
   * The polyShell vertices are contiguous, so are suitable for an SPRtree.
   */
  private VertexSequencePackedRtree vertexCoordIndex;

  private int targetVertexCount;

  /**
   * Creates a new PolygonConcaveHull instance.
   * 
   * @param polyShell the polygon vertices to process
   */
  public PolygonConcaveHull(Coordinate[] polyShell, double vertexSizeFraction) {
    this.vertex = polyShell;
    
    // init working storage
    vertexSize = vertex.length - 1;
    vertexNext = createNextLinks(vertexSize);
    vertexFirst = 0;
    
    vertexCoordIndex = new VertexSequencePackedRtree(vertex);
    
    targetVertexCount = (int) (vertexSize * vertexSizeFraction);
  }

  public Geometry getResult() {
    compute();
    return toGeometry();
  }
  
  private static int[] createNextLinks(int size) {
    int[] next = new int[size];
    for (int i = 0; i < size; i++) {
      next[i] = i + 1;
    }
    next[size - 1] = 0;
    return next;
  }
  
  public void compute() {    
   
    initCornerIndex();
    Coordinate[] corner = new Coordinate[3];
    fetchCorner(corner);
    
    /**
     * Scan continuously around vertex ring, 
     * until all ears have been found.
     */
    while (vertexSize > targetVertexCount) {
      /**
       * Non-convex corner- remove if flat, or skip
       * (a concave corner will turn into a convex corner
       * after enough ears are removed)
       */
      if (! isConcave(corner)) {
      }
      /**
       * Concave corner - check if it is a valid ear
       */
      else if ( isConcave(corner) && isValidEar(cornerIndex[1], corner) ) {
        removeCorner();
      }
      
      /**
       * Skip to next corner.
       * This is done even after an ear is removed, 
       * since that creates fewer skinny triangles.
       */
      nextCorner(corner);
    }
  }
  
  private boolean isValidEar(int cornerIndex, Coordinate[] corner) {
    int intApexIndex = findIntersectingVertex(cornerIndex, corner);
    //--- no intersections found
    if (intApexIndex == NO_VERTEX_INDEX)
      return true;
    /*
    //TODO: make this work
    //--- check for duplicate corner apex vertex
    if ( vertex[intApexIndex].equals2D(corner[1]) ) {
      //--- a duplicate corner vertex requires a full scan
      return isValidEarScan(cornerIndex, corner);
    }
    */
    return false;
  }

  /**
   * Finds another vertex intersecting the corner triangle, if any.
   * Uses the vertex spatial index for efficiency.
   * <p>
   * Also finds any vertex which is a duplicate of the corner apex vertex,
   * which then requires a full scan of the vertices to confirm ear is valid. 
   * This is usually a rare situation, so has little impact on performance.
   * 
   * @param cornerIndex the index of the corner apex vertex
   * @param corner the corner vertices
   * @return the index of an intersecting or duplicate vertex, or {@link #NO_VERTEX_INDEX} if none
   */
  private int findIntersectingVertex(int cornerIndex, Coordinate[] corner) {
    Envelope cornerEnv = envelope(corner);
    int[] result = vertexCoordIndex.query(cornerEnv);
    
    int dupApexIndex = NO_VERTEX_INDEX;
    //--- check for duplicate vertices
    for (int i = 0; i < result.length; i++) {
      int vertIndex = result[i];
      
      if (vertIndex == cornerIndex 
          || vertIndex == vertex.length - 1
          || isRemoved(vertIndex)) 
        continue;
      
      Coordinate v = vertex[vertIndex];
      /**
       * If another vertex at the corner is found,
       * need to do a full scan to check the incident segments.
       * This happens when the polygon ring self-intersects,
       * usually due to hold joining.
       * But only report this if no properly intersecting vertex is found,
       * for efficiency.
       */
      if ( v.equals2D(corner[1]) ) {
        dupApexIndex = vertIndex;
      }
      //--- don't need to check other corner vertices 
      else if ( v.equals2D(corner[0]) || v.equals2D(corner[2]) ) {
        continue;
      }
      //--- this is a properly intersecting vertex
      else if (Triangle.intersects(corner[0], corner[1], corner[2], v) )
        return vertIndex;
    }
    if (dupApexIndex != NO_VERTEX_INDEX) {
      return dupApexIndex;
    }
    return NO_VERTEX_INDEX;
  }

  /**
   * Scan all vertices in current ring to check if any are duplicates
   * of the corner apex vertex, and if so whether the corner ear
   * intersects the adjacent segments and thus is invalid.
   * 
   * @param cornerIndex the index of the corner apex
   * @param corner the corner vertices
   * @return true if the corner ia a valid ear
   */
  private boolean isValidEarScan(int cornerIndex, Coordinate[] corner) {
    double cornerAngle = Angle.angleBetweenOriented(corner[0], corner[1], corner[2]);
    
    int currIndex = nextIndex(vertexFirst);
    int prevIndex = vertexFirst;
    Coordinate vPrev = vertex[prevIndex];
    for (int i = 0; i < vertexSize; i++) {
      Coordinate v = vertex[currIndex];
      /**
       * Because of hole-joining vertices can occur more than once.
       * If vertex is same as corner[1],
       * check whether either adjacent edge lies inside the ear corner.
       * If so the ear is invalid.
       */
      if ( currIndex != cornerIndex 
          && v.equals2D(corner[1]) ) {
        Coordinate vNext = vertex[nextIndex(currIndex)];
        
        //TODO: for robustness use segment orientation instead
        double aOut = Angle.angleBetweenOriented(corner[0], corner[1], vNext);
        double aIn = Angle.angleBetweenOriented(corner[0], corner[1], vPrev);
        if ( aOut > 0 && aOut < cornerAngle ) {
          return false;
        }
        if ( aIn > 0 && aIn < cornerAngle ) {
          return false;
        }
        if ( aOut == 0 && aIn == cornerAngle ) {
          return false;
        }
      }

      //--- move to next vertex
      vPrev = v;
      prevIndex = currIndex;
      currIndex = nextIndex(currIndex);
    }
    return true;
  }
  
  private static Envelope envelope(Coordinate[] corner) {
    Envelope cornerEnv = new Envelope(corner[0], corner[1]);
    cornerEnv.expandToInclude(corner[2]);
    return cornerEnv;
  }

  /**
   * Remove the corner apex vertex and update the candidate corner location.
   */
  private void removeCorner() {
    int cornerApexIndex = cornerIndex[1];
    if ( vertexFirst ==  cornerApexIndex) {
      vertexFirst = vertexNext[cornerApexIndex];
    }
    vertexNext[cornerIndex[0]] = vertexNext[cornerApexIndex];
    vertexCoordIndex.remove(cornerApexIndex);
    vertexNext[cornerApexIndex] = NO_VERTEX_INDEX;
    vertexSize--;
    //-- adjust following corner indexes
    cornerIndex[1] = nextIndex(cornerIndex[0]);
    cornerIndex[2] = nextIndex(cornerIndex[1]);
  }

  private boolean isRemoved(int vertexIndex) {
    return NO_VERTEX_INDEX == vertexNext[vertexIndex];
  }
  
  private void initCornerIndex() {
    cornerIndex = new int[3];
    cornerIndex[0] = 0;
    cornerIndex[1] = 1;
    cornerIndex[2] = 2;
  }
  
  /**
   * Fetch the corner vertices from the indices.
   * 
   * @param corner an array for the corner vertices
   */
  private void fetchCorner(Coordinate[] cornerVertex) {
    cornerVertex[0] = vertex[cornerIndex[0]]; 
    cornerVertex[1] = vertex[cornerIndex[1]]; 
    cornerVertex[2] = vertex[cornerIndex[2]]; 
  }

  /**
   * Move to next corner.
   */
  private void nextCorner(Coordinate[] cornerVertex) {
    if ( vertexSize < 3 ) {
      return;
    }
    cornerIndex[0] = nextIndex(cornerIndex[0]);
    cornerIndex[1] = nextIndex(cornerIndex[0]);
    cornerIndex[2] = nextIndex(cornerIndex[1]);
    fetchCorner(cornerVertex);
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

  private static boolean isConvex(Coordinate[] pts) {
    return Orientation.CLOCKWISE == Orientation.index(pts[0], pts[1], pts[2]);
  }
  
  private static boolean isConcave(Coordinate[] pts) {
    return Orientation.COUNTERCLOCKWISE == Orientation.index(pts[0], pts[1], pts[2]);
  }
  
  private static boolean isFlat(Coordinate[] pts) {
    return Orientation.COLLINEAR == Orientation.index(pts[0], pts[1], pts[2]);
  }
  
  private static boolean hasRepeatedPoint(Coordinate[] pts) {
    return pts[1].equals2D(pts[0]) || pts[1].equals2D(pts[2]);
  }
  
  public Polygon toGeometry() {
    GeometryFactory fact = new GeometryFactory();
    CoordinateList coordList = new CoordinateList();
    int index = vertexFirst;
    for (int i = 0; i < vertexSize; i++) {
      Coordinate v = vertex[index];
      index = nextIndex(index);
      // if (i < shellCoordAvailable.length && shellCoordAvailable.get(i))
      coordList.add(v, true);
    }
    coordList.closeRing();
    return fact.createPolygon(fact.createLinearRing(coordList.toCoordinateArray()));
  }
}