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
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.triangulatepoly.tri.Tri;

/**
 * Triangulates a polygon via the classic Ear-Clipping technique.
 * The polygon is provided as a closed list of vertices
 * defining its boundary.
 * <p>
 * The polygon boundary must not self-cross, but may self-touch and have coincident edges.
 * Polygons with holes can be triangulated by preparing them 
 * with {@link PolygonHoleJoiner}.
 * 
 * @author mdavis
 *
 */
class PolygonEarClipper {
  
  private static final int NO_VERTEX_INDEX = -1;

  public static List<Tri> clip(Coordinate[] polyShell) {
    PolygonEarClipper clipper = new PolygonEarClipper(polyShell);
    return clipper.compute();
  }
  
  /**
   * The polygon vertices are maintain in CW order. 
   * Thus for convex
   * interior angles the vertices forming the angle are in CW orientation.
   */
  private final Coordinate[] vertex;
  
  private final int[] vertexNext;
  private int vertexSize;
  // first available vertex index
  private int vertexFirst;
  
  // indices for current candidate corner
  private int[] cornerCandidate;
  
  /**
   * Indexing vertices improves ear intersection testing performance a lot.
   * The polyShell vertices are contiguous, so are suitable for an SPRtree.
   */
  private VertexSequencePackedRtree vertexCoordIndex;

  private boolean isCollinearVerticesSkipped = false;

  public PolygonEarClipper(Coordinate[] polyShell) {
    this.vertex = polyShell;
    
    // init working storage
    vertexSize = vertex.length - 1;
    vertexNext = createNextLinks(vertexSize);
    vertexFirst = 0;
    
    cornerCandidate = new int[3];
    cornerCandidate[0] = 0;
    cornerCandidate[1] = 1;
    cornerCandidate[2] = 2;
    
    vertexCoordIndex = new VertexSequencePackedRtree(vertex);
  }

  private static int[] createNextLinks(int size) {
    int[] next = new int[size];
    for (int i = 0; i < size; i++) {
      next[i] = i + 1;
    }
    next[size - 1] = 0;
    return next;
  }

  /**
   * Sets whether flat ears formed by collinear adjacent line segments
   * are skipped in the triangulation.
   * This reduces the number of triangles in the output.
   * However, it produces a triangulation which does not include
   * all input vertices.  This may be an issue for client processes
   * (such as computing a Constrained Delaunay Triangulation for 
   * purposes of computing the medial axis).
   * <p>
   * The default is to include all vertices in the result triangulation.  
   * This still produces a valid triangulation, with no zero-area triangles.
   * 
   * @param isSkipFlatEars whether to skip collinear vertices
   */
  public void setSkipCollinearVertices(boolean isCollinearVerticesSkipped) {
    this.isCollinearVerticesSkipped  = isCollinearVerticesSkipped;
  }
  
  public List<Tri> compute() {
    List<Tri> triList = new ArrayList<Tri>();

    int cornerCount = 0;
    Coordinate[] corner = new Coordinate[3];
    nextCorner(false, corner);
    while (true) {
      //--- find next convex corner, which is the next candidate ear
      //Polygon remainder = toGeometry();
      while (! isConvex(corner)) {
        // delete the corner if it is flat
        if (isCollinearVerticesSkipped) {
          if (isFlat(corner) ) {
            removeCorner();
            if ( vertexSize < 3 ) {
              return triList;
            }
          }
        }
        nextCorner(true, corner);
        if ( cornerCount > 2 * vertexSize ) {
          throw new IllegalStateException("Unable to find a convex corner");
        }
      }
      cornerCount++;
      if ( cornerCount > 2 * vertexSize ) {
        throw new IllegalStateException("Unable to find a valid ear");
        //System.out.println(WKTWriter.toLineString(corner));
        //return triList;
      }
      if ( isValidEar(cornerCandidate[1], corner) ) {
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
  
  private boolean isValidEar(int cornerIndex, Coordinate[] corner) {
    int dupApexIndex = findIntersectingVertex(cornerIndex, corner);
    //--- no intersections found
    if (dupApexIndex < 0)
      return true;
    //--- found duplicate corner apex vertex
    if ( vertex[dupApexIndex].equals2D(corner[1]) ) {
      return isValidEarScanApex(cornerIndex, corner);
    }
    return false;
  }

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
      if ( vertIndex != cornerIndex && v.equals2D(corner[1]) ) {
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

  private boolean isValidEarScanApex(int cornerIndex, Coordinate[] corner) {
    double cornerAngle = Angle.angleBetweenOriented(corner[0], corner[1], corner[2]);
    
    int currIndex = nextIndex(vertexFirst);
    int prevIndex = vertexFirst;
    Coordinate vPrev = vertex[prevIndex];
    for (int i = 0; i < vertexSize; i++) {
      Coordinate v = vertex[currIndex];
      /**
       * Because of hole joining vertices can occur more than once.
       * If vertex is same as corner[1],
       * must check whether adjacent edges lie inside the ear corner.
       * If so this makes the ear invalid.
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
   * Remove the corner apex and update the candidate corner location.
   */
  private void removeCorner() {
    int cornerIndex = cornerCandidate[1];
    if ( vertexFirst ==  cornerIndex) {
      vertexFirst = vertexNext[cornerIndex];
    }
    vertexNext[cornerCandidate[0]] = vertexNext[cornerIndex];
    vertexCoordIndex.remove(cornerIndex);
    vertexNext[cornerIndex] = NO_VERTEX_INDEX;
    vertexSize--;
    nextCorner(false, null);
  }

  private boolean isRemoved(int vertexIndex) {
    return NO_VERTEX_INDEX == vertexNext[vertexIndex];
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
      corner[0] = vertex[cornerCandidate[0]]; 
      corner[1] = vertex[cornerCandidate[1]]; 
      corner[2] = vertex[cornerCandidate[2]]; 
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

  private static boolean isConvex(Coordinate[] pts) {
    return Orientation.CLOCKWISE == Orientation.index(pts[0], pts[1], pts[2]);
  }
  
  private static boolean isFlat(Coordinate[] pts) {
    return Orientation.COLLINEAR == Orientation.index(pts[0], pts[1], pts[2]);
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