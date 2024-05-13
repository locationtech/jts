/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.noding.BasicSegmentString;

/**
 * Models a linear edge of a {@link RelateGeometry}.
 * 
 * @author mdavis
 *
 */
class RelateSegmentString extends BasicSegmentString {
  
  public static RelateSegmentString createLine(Coordinate[] pts, boolean isA, int elementId, RelateGeometry parent) {
    return createSegmentString(pts, isA, Dimension.L, elementId, -1, null, parent);
  }
  
  public static RelateSegmentString createRing(Coordinate[] pts, boolean isA, int elementId, int ringId, 
      Geometry poly, RelateGeometry parent) {
    return createSegmentString(pts, isA, Dimension.A, elementId, ringId, poly, parent);
  }

  private static RelateSegmentString createSegmentString(Coordinate[] pts, boolean isA, int dim, int elementId, int ringId, 
      Geometry poly, RelateGeometry parent) {
    pts = removeRepeatedPoints(pts);
    return new RelateSegmentString(pts, isA, dim, elementId, ringId, poly, parent);
  }

  private static Coordinate[] removeRepeatedPoints(Coordinate[] pts) {
    if (CoordinateArrays.hasRepeatedPoints(pts)) {
      pts = CoordinateArrays.removeRepeatedPoints(pts);
    }
    return pts;
  }
  
  private boolean isA;
  private int dimension;
  private int id;
  private int ringId;
  private RelateGeometry inputGeom;
  private Geometry parentPolygonal = null;

  private RelateSegmentString(Coordinate[] pts, boolean isA, int dimension, int id, int ringId, Geometry poly, RelateGeometry inputGeom) {
    super(pts, null);
    this.isA = isA;
    this.dimension = dimension;
    this.id = id;
    this.ringId = ringId;
    this.parentPolygonal = poly;
    this.inputGeom = inputGeom;
  }
  
  public boolean isA() {
    return isA;
  }
  
  public RelateGeometry getGeometry() {
    return inputGeom;
  }
  
  public Geometry getPolygonal() {
    return parentPolygonal;
  }
  
  public NodeSection createNodeSection(int segIndex, Coordinate intPt) {
    boolean isNodeAtVertex = 
        intPt.equals2D(getCoordinate(segIndex))
        || intPt.equals2D(getCoordinate(segIndex + 1));
    Coordinate prev = prevVertex(segIndex, intPt);
    Coordinate next = nextVertex(segIndex, intPt);
    NodeSection a = new NodeSection(isA, dimension, id, ringId, parentPolygonal, isNodeAtVertex, prev, intPt, next);
    return a;
  }
  
  /**
   * 
   * @param ss
   * @param segIndex
   * @param pt
   * @return the previous vertex, or null if none exists
   */
  private Coordinate prevVertex(int segIndex, Coordinate pt) {
    Coordinate segStart = getCoordinate(segIndex);
    if (! segStart.equals2D(pt))
      return segStart;
    //-- pt is at segment start, so get previous vertex
    if (segIndex > 0)
      return getCoordinate(segIndex - 1);
    if (isClosed())
      return prevInRing(segIndex);
    return null;
  }

  /**
   * 
   * @param ss
   * @param segIndex
   * @param pt
   * @return the next vertex, or null if none exists
   */
  private Coordinate nextVertex(int segIndex, Coordinate pt) {
    Coordinate segEnd = getCoordinate(segIndex + 1);
    if (! segEnd.equals2D(pt))
      return segEnd;
    //-- pt is at seg end, so get next vertex
    if (segIndex < size() - 2)
      return getCoordinate(segIndex + 2);
    if (isClosed())
      return nextInRing(segIndex + 1);
    //-- segstring is not closed, so there is no next segment
    return null;
  }

  /**
   * Tests if a segment intersection point has that segment as its
   * canonical containing segment.
   * Segments are half-closed, and contain their start point but not the endpoint,
   * except for the final segment in a non-closed segment string, which contains
   * its endpoint as well.
   * This test ensures that vertices are assigned to a unique segment in a segment string.
   * In particular, this avoids double-counting intersections which lie exactly 
   * at segment endpoints.
   * 
   * @param segIndex the segment the point may lie on
   * @param pt the point
   * @return true if the segment contains the point
   */
  public boolean isContainingSegment(int segIndex, Coordinate pt) {
    //-- intersection is at segment start vertex - process it
    if (pt.equals2D(getCoordinate(segIndex)))
      return true;
    if (pt.equals2D(getCoordinate(segIndex+1))) {
      boolean isFinalSegment = segIndex == size() - 2;
      if (isClosed() || ! isFinalSegment)
        return false;
      //-- for final segment, process intersections with final endpoint
      return true;
    }
    //-- intersection is interior - process it
    return true;
  }


}
