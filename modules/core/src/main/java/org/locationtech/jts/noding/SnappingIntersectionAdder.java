/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.noding;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Finds intersections between line segments which will be snap-rounded,
 * and adds them as nodes.
 * <p>
 * Intersections are detected and computed using full precision.
 * Snapping takes place in a subsequent phase.
 * To avoid robustness issues with vertices which lie very close to line segments,
 * the following heuristic is used:
 * nodes are created if a vertex lies within a tolerance distance
 * of the interior of a segment.
 * The tolerance distance is chosen to be significantly below the snap-rounding grid size.
 * This has empirically proven to eliminate noding failures.
 *
 * @version 1.17
 */
public class SnappingIntersectionAdder
    implements SegmentIntersector
{
  
  private LineIntersector li;

  private double snapTolerance;


  /**
   * Creates an intersector which finds all snapped interior intersections,
   * and adds them as nodes.
   *
   * @param pm the precision mode to use
   */
  public SnappingIntersectionAdder(double snapTolerance)
  {
    this.snapTolerance = snapTolerance;
    
    /**
     * Intersections are detected and computed using full precision.
     */
    li = new RobustLineIntersector();
  }

  /**
   * A trivial intersection is an apparent self-intersection which in fact
   * is simply the point shared by adjacent line segments.
   * Note that closed edges require a special check for the point shared by the beginning
   * and end segments.
   */
  private boolean isAdjacentIntersection(SegmentString e0, int segIndex0, SegmentString e1, int segIndex1)
  {
    if (e0 == e1) {
      if (li.getIntersectionNum() == 1) {
        boolean isAdjacent = Math.abs(segIndex0 - segIndex1) == 1;
        if (isAdjacent)
          return true;
        if (e0.isClosed()) {
          int maxSegIndex = e0.size() - 1;
          if (    (segIndex0 == 0 && segIndex1 == maxSegIndex)
              ||  (segIndex1 == 0 && segIndex0 == maxSegIndex) ) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  /**
   * This method is called by clients
   * of the {@link SegmentIntersector} class to process
   * intersections for two segments of the {@link SegmentString}s being intersected.
   * Note that some clients (such as <code>MonotoneChain</code>s) may optimize away
   * this call for segment pairs which they have determined do not intersect
   * (e.g. by an disjoint envelope test).
   */
  public void processIntersections(
      SegmentString e0,  int segIndex0,
      SegmentString e1,  int segIndex1
      )
  {
    // don't bother intersecting a segment with itself
    if (e0 == e1 && segIndex0 == segIndex1) return;

    Coordinate p00 = e0.getCoordinates()[segIndex0];
    Coordinate p01 = e0.getCoordinates()[segIndex0 + 1];
    Coordinate p10 = e1.getCoordinates()[segIndex1];
    Coordinate p11 = e1.getCoordinates()[segIndex1 + 1];

    li.computeIntersection(p00, p01, p10, p11);
//if (li.hasIntersection() && li.isProper()) Debug.println(li);

    if (li.hasIntersection()) {
      /*
      if (li.isInteriorIntersection()) {
        ((NodedSegmentString) e0).addIntersections(li, segIndex0, 0);
        ((NodedSegmentString) e1).addIntersections(li, segIndex1, 1);
        return;
      }
      */
      if (! isAdjacentIntersection(e0, segIndex0, e1, segIndex1)) {
        ((NodedSegmentString) e0).addIntersections(li, segIndex0, 0);
        ((NodedSegmentString) e1).addIntersections(li, segIndex1, 1);
      }
    }
    
    /**
     * Segments did not actually intersect, within the limits of orientation index robustness.
     * 
     * To avoid certain robustness issues in snap-rounding, 
     * also treat very near vertex-segment situations as intersections.
     */
    processNearVertex(p00, e1, segIndex1, p10, p11 );
    processNearVertex(p01, e1, segIndex1, p10, p11 );
    processNearVertex(p10, e0, segIndex0, p00, p01 );
    processNearVertex(p11, e0, segIndex0, p00, p01 );
  }
  
  /**
   * If an endpoint of one segment is near 
   * the <i>interior</i> of the other segment, add it as an intersection.
   * EXCEPT if the endpoint is also close to a segment endpoint
   * (since this can introduce "zigs" in the linework).
   * <p>
   * This resolves situations where
   * a segment A endpoint is extremely close to another segment B,
   * but is not quite crossing.  Due to robustness issues
   * in orientation detection, this can 
   * result in the snapped segment A crossing segment B
   * without a node being introduced.
   * 
   * @param p
   * @param edge
   * @param segIndex
   * @param p0
   * @param p1
   */
  private void processNearVertex(Coordinate p, SegmentString edge, int segIndex, Coordinate p0, Coordinate p1) {
    
    /**
     * Don't add intersection if candidate vertex is near endpoints of segment.
     * This avoids creating "zig-zag" linework
     * (since the vertex could actually be outside the segment envelope).
     */
    if (p.distance(p0) < snapTolerance) return;
    if (p.distance(p1) < snapTolerance) return;
    
    double distSeg = Distance.pointToSegment(p, p0, p1);
    if (distSeg < snapTolerance) {
      ((NodedSegmentString) edge).addIntersection(p, segIndex);
    }
  }

  /**
   * Always process all intersections
   * 
   * @return false always
   */
  public boolean isDone() { return false; }

}