/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Position;
import org.locationtech.jts.geomgraph.DirectedEdge;

/**
 * Locates a subgraph inside a set of subgraphs,
 * in order to determine the outside depth of the subgraph.
 * The input subgraphs are assumed to have had depths
 * already calculated for their edges.
 *
 * @version 1.7
 */
class SubgraphDepthLocater
{
  private Collection subgraphs;
  private LineSegment seg = new LineSegment();

  public SubgraphDepthLocater(List subgraphs)
  {
    this.subgraphs = subgraphs;
  }

  public int getDepth(Coordinate p)
  {
    List stabbedSegments = findStabbedSegments(p);
    // if no segments on stabbing line subgraph must be outside all others.
    if (stabbedSegments.size() == 0)
      return 0;
    DepthSegment ds = (DepthSegment) Collections.min(stabbedSegments);
    return ds.leftDepth;
  }

  /**
   * Finds all non-horizontal segments intersecting the stabbing line.
   * The stabbing line is the ray to the right of stabbingRayLeftPt.
   *
   * @param stabbingRayLeftPt the left-hand origin of the stabbing line
   * @return a List of {@link DepthSegments} intersecting the stabbing line
   */
  private List findStabbedSegments(Coordinate stabbingRayLeftPt)
  {
    List stabbedSegments = new ArrayList();
    for (Iterator i = subgraphs.iterator(); i.hasNext(); ) {
      BufferSubgraph bsg = (BufferSubgraph) i.next();

      // optimization - don't bother checking subgraphs which the ray does not intersect
      Envelope env = bsg.getEnvelope();
      if (stabbingRayLeftPt.y < env.getMinY()
          || stabbingRayLeftPt.y > env.getMaxY())
        continue;

      findStabbedSegments(stabbingRayLeftPt, bsg.getDirectedEdges(), stabbedSegments);
    }
    return stabbedSegments;
  }

  /**
   * Finds all non-horizontal segments intersecting the stabbing line
   * in the list of dirEdges.
   * The stabbing line is the ray to the right of stabbingRayLeftPt.
   *
   * @param stabbingRayLeftPt the left-hand origin of the stabbing line
   * @param stabbedSegments the current list of {@link DepthSegments} intersecting the stabbing line
   */
  private void findStabbedSegments(Coordinate stabbingRayLeftPt,
                                   List dirEdges,
                                   List stabbedSegments)
  {
    /**
     * Check all forward DirectedEdges only.  This is still general,
     * because each Edge has a forward DirectedEdge.
     */
    for (Iterator i = dirEdges.iterator(); i.hasNext();) {
      DirectedEdge de = (DirectedEdge) i.next();
      if (! de.isForward())
        continue;
      findStabbedSegments(stabbingRayLeftPt, de, stabbedSegments);
    }
  }

  /**
   * Finds all non-horizontal segments intersecting the stabbing line
   * in the input dirEdge.
   * The stabbing line is the ray to the right of stabbingRayLeftPt.
   *
   * @param stabbingRayLeftPt the left-hand origin of the stabbing line
   * @param stabbedSegments the current list of {@link DepthSegments} intersecting the stabbing line
   */
  private void findStabbedSegments(Coordinate stabbingRayLeftPt,
                                   DirectedEdge dirEdge,
                                   List stabbedSegments)
  {
    Coordinate[] pts = dirEdge.getEdge().getCoordinates();
    for (int i = 0; i < pts.length - 1; i++) {
      seg.p0 = pts[i];
      seg.p1 = pts[i + 1];
      // ensure segment always points upwards
      if (seg.p0.y > seg.p1.y)
        seg.reverse();

      // skip segment if it is left of the stabbing line
      double maxx = Math.max(seg.p0.x, seg.p1.x);
      if (maxx < stabbingRayLeftPt.x)
        continue;

      // skip horizontal segments (there will be a non-horizontal one carrying the same depth info
      if (seg.isHorizontal())
        continue;

      // skip if segment is above or below stabbing line
      if (stabbingRayLeftPt.y < seg.p0.y || stabbingRayLeftPt.y > seg.p1.y)
        continue;

      // skip if stabbing ray is right of the segment
      if (Orientation.index(seg.p0, seg.p1, stabbingRayLeftPt)
          == Orientation.RIGHT)
        continue;

      // stabbing line cuts this segment, so record it
      int depth = dirEdge.getDepth(Position.LEFT);
      // if segment direction was flipped, use RHS depth instead
      if (! seg.p0.equals(pts[i]))
        depth = dirEdge.getDepth(Position.RIGHT);
      DepthSegment ds = new DepthSegment(seg, depth);
      stabbedSegments.add(ds);
    }
  }


  /**
   * A segment from a directed edge which has been assigned a depth value
   * for its sides.
   */
  static class DepthSegment
      implements Comparable
  {
    private LineSegment upwardSeg;
    private int leftDepth;

    public DepthSegment(LineSegment seg, int depth)
    {
      // Assert: input seg is upward (p0.y <= p1.y)
      upwardSeg = new LineSegment(seg);
      this.leftDepth = depth;
    }
    
    public boolean isUpward() {
      return upwardSeg.p0.y <= upwardSeg.p1.y;
    }
    
    /**
     * A comparison operation
     * which orders segments left to right
     * along some horizontal line.
     * If segments don't touch the same line, 
     * or touch at the same point,
     * they are compared in their Y extent.
     * 
     * <p>
     * The definition of the ordering is:
     * <ul>
     * <li>-1 : if DS1.seg is left of or below DS2.seg (DS1 < DS2)
     * <li>1 : if  DS1.seg is right of or above DS2.seg (DS1 > DS2) 
     * <li>0 : if the segments are identical 
     * </ul>
     * 
     * @param obj a DepthSegment
     * @return the comparison value
     */
    public int compareTo(Object obj)
    {
      LineSegment otherSeg = ((DepthSegment) obj).upwardSeg;
      
      /**
       * If segments are disjoint in X, X values provides ordering.
       * This is the most common case.
       */
      if (upwardSeg.minX() > otherSeg.maxX())
        return 1;
      if (upwardSeg.maxX() < otherSeg.minX())
        return -1;
      /**
       * The segments Y ranges should intersect since they lie on same stabbing line.
       * But check for this and provide a result based on Y ordering
       */
      if (upwardSeg.minY() > otherSeg.maxY())
        return 1;
      if (upwardSeg.maxY() < otherSeg.minY())
        return -1;
      
      /**
       * Check if some segment point is left or right
       * of the other segment in its Y extent.
       */
      int comp00 = comparePointInYExtent(upwardSeg.p0, otherSeg);
      if (comp00 != 0) return comp00;
      int comp01 = comparePointInYExtent(upwardSeg.p1, otherSeg);
      if (comp01 != 0) return comp01;
      //-- negate orientation for other/this checks
      int comp10 = -comparePointInYExtent(otherSeg.p0, upwardSeg);
      if (comp10 != 0) return comp10;
      int comp11 = -comparePointInYExtent(otherSeg.p1, upwardSeg);
      if (comp11 != 0) return comp11;
      
      /**
       * If point checks in Y range are indeterminate,
       * segments touch at a point
       * and lie above and below that point, or are horizontal.
       * Order according to their Y values.
       * (The ordering in this case doesn't matter, it just has to be consistent)
       */
      if (upwardSeg.maxY() > otherSeg.maxY())
        return 1;
      if (upwardSeg.maxY() < otherSeg.maxY())
        return -1;
      
      /**
       * If both are horizontal order by X
       */
      if (upwardSeg.isHorizontal() && otherSeg.isHorizontal()) {
        if (upwardSeg.minX() < otherSeg.minX())
          return -1;
        if (upwardSeg.minX() > otherSeg.minX())
          return 1;
      }
      
      // assert: segments are equal
      return 0;
    }
    
    /**
     * Compares a point to a segment for left/right position, 
     * as long as the point lies within the segment Y extent.
     * Otherwise the point is not comparable.
     * If the point is not comparable or it lies on the segment
     * returns 0.
     * 
     * @param p
     * @param seg
     * @return
     */
    private int comparePointInYExtent(Coordinate p, LineSegment seg) {
      //-- if point is comparable to segment
      if (p.y >= seg.minY() && p.y <= seg.maxY()) {
        //-- flip sign, since orientation and order relation are opposite
        int orient = seg.orientationIndex(p);
        switch (orient) {
        case Orientation.LEFT: return -1;
        case Orientation.RIGHT: return 1;
        }
        //-- collinear, so indeterminate
      }
      //-- not computable
      return 0;
    }

    public String toString()
    {
      return upwardSeg.toString();
    }
  }
}
