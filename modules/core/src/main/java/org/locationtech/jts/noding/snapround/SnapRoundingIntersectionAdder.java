/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.noding.snapround;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentString;

/**
 * Finds intersections between line segments which will be snap-rounded,
 * and adds them as nodes to the segments.
 * <p>
 * Intersections are detected and computed using full precision.
 * Snapping takes place in a subsequent phase.
 * <p>
 * The intersection points are recorded, so that HotPixels can be created for them.
 * <p>
 * To avoid robustness issues with vertices which lie very close to line segments
 * a heuristic is used: 
 * nodes are created if a vertex lies within a tolerance distance
 * of the interior of a segment.
 * The tolerance distance is chosen to be significantly below the snap-rounding grid size.
 * This has empirically proven to eliminate noding failures.
 *
 * @version 1.17
 */
public class SnapRoundingIntersectionAdder
    implements SegmentIntersector
{
  /**
   * The division factor used to determine
   * nearness distance tolerance for interior intersection detection.
   */
  private static final int NEARNESS_FACTOR = 100;
  
  private final LineIntersector li;
  private final List<Coordinate> intersections;
  private final PrecisionModel precModel;
  private final double nearnessTol;


  /**
   * Creates an intersector which finds all snapped interior intersections,
   * and adds them as nodes.
   *
   * @param pm the precision mode to use
   */
  public SnapRoundingIntersectionAdder(PrecisionModel pm)
  {
    precModel = pm;
    /**
     * Nearness distance tolerance is a small fraction of the snap grid size
     */
    double snapGridSize = 1.0 / precModel.getScale();
    nearnessTol =  snapGridSize / NEARNESS_FACTOR;
    
    /**
     * Intersections are detected and computed using full precision.
     * They are snapped in a subsequent phase.
     */
    li = new RobustLineIntersector();
    intersections = new ArrayList();
  }

  /**
   * Gets the created intersection nodes, 
   * so they can be processed as hot pixels.
   * 
   * @return a list of the intersection points
   */
  public List<Coordinate> getIntersections()  {    return intersections;  }

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

    Coordinate p00 = e0.getCoordinate(segIndex0);
    Coordinate p01 = e0.getCoordinate(segIndex0 + 1);
    Coordinate p10 = e1.getCoordinate(segIndex1);
    Coordinate p11 = e1.getCoordinate(segIndex1 + 1);

    li.computeIntersection(p00, p01, p10, p11);
//if (li.hasIntersection() && li.isProper()) Debug.println(li);

    if (li.hasIntersection()) {
      if (li.isInteriorIntersection()) {
        for (int intIndex = 0; intIndex < li.getIntersectionNum(); intIndex++) {
          intersections.add(li.getIntersection(intIndex));
        }
        ((NodedSegmentString) e0).addIntersections(li, segIndex0, 0);
        ((NodedSegmentString) e1).addIntersections(li, segIndex1, 1);
        return;
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
    if (p.distance(p0) < nearnessTol) return;
    if (p.distance(p1) < nearnessTol) return;
    
    double distSeg = Distance.pointToSegment(p, p0, p1);
    if (distSeg < nearnessTol) {
      intersections.add(p);
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