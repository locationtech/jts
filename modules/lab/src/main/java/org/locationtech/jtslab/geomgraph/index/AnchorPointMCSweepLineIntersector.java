/*
 * Copyright (c) 2016 Vivid Solutions.
 * Copyright (c) 2019 Felix Obermaier
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtslab.geomgraph.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geomgraph.index.*;
import org.locationtech.jtslab.algorithm.AnchorPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geomgraph.Edge;
import org.locationtech.jts.index.kdtree.KdTree;

/**
 * Finds all intersections in one or two sets of edges,
 * using an x-axis sweepline algorithm in conjunction with Monotone Chains.
 * While still O(n^2) in the worst case, this algorithm
 * drastically improves the average-case time.
 * The use of MonotoneChains as the items in the index
 * seems to offer an improvement in performance over a sweep-line alone.
 *
 * @version 1.17
 */
public class AnchorPointMCSweepLineIntersector
  extends EdgeSetIntersector
{

  /** The index of {@linkplain AnchorPoint}s. */
  private final KdTree anchorPoints;

  List events = new ArrayList();

  // statistics information
  int nOverlaps;

  /**
   * Creates an instance of this class
   *
   * @param anchorPoints an index of {@linkplain AnchorPoint}s.
   */
  public AnchorPointMCSweepLineIntersector(KdTree anchorPoints) {
    if (anchorPoints == null)
      throw new IllegalArgumentException("anchorPoints");

    this.anchorPoints = anchorPoints;
  }

  public void computeIntersections(List edges, SegmentIntersector si, boolean testAllSegments)
  {
    if (testAllSegments)
      addEdges(edges, null);
    else
      addEdges(edges);
    computeIntersections(si);
  }

  public void computeIntersections(List edges0, List edges1, SegmentIntersector si)
  {
    addEdges(edges0, edges0);
    addEdges(edges1, edges1);
    computeIntersections(si);
  }

  private void addEdges(List edges)
  {
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      Edge edge = (Edge) i.next();
      // edge is its own group
      addEdge(edge, edge);
    }
  }
  private void addEdges(List edges, Object edgeSet)
  {
    for (Iterator it = edges.iterator(); it.hasNext(); ) {
      Edge edge = (Edge) it.next();
      // add the edge's vertices to the anchor points index
      for (int i = 0; i < edge.getNumPoints(); i++) {
        Coordinate c = edge.getCoordinate(i);
        this.anchorPoints.insert(c, new AnchorPoint(c, true));
      }
      addEdge(edge, edgeSet);
    }
  }

  private void addEdge(Edge edge, Object edgeSet)
  {
    MonotoneChainEdge mce = edge.getMonotoneChainEdge();
    int[] startIndex = mce.getStartIndexes();
    for (int i = 0; i < startIndex.length - 1; i++) {
      MonotoneChain mc = new MonotoneChain(mce, i);
      SweepLineEvent insertEvent = new SweepLineEvent(edgeSet, mce.getMinX(i), mc);
      events.add(insertEvent);
      events.add(new SweepLineEvent(mce.getMaxX(i), insertEvent));
    }
  }

  /**
   * Because Delete Events have a link to their corresponding Insert event,
   * it is possible to compute exactly the range of events which must be
   * compared to a given Insert event object.
   */
  private void prepareEvents()
  {
    Collections.sort(events);
    // set DELETE event indexes
    for (int i = 0; i < events.size(); i++ )
    {
      SweepLineEvent ev = (SweepLineEvent) events.get(i);
      if (ev.isDelete()) {
        ev.getInsertEvent().setDeleteEventIndex(i);
      }
    }
  }

  private void computeIntersections(SegmentIntersector si)
  {
    nOverlaps = 0;
    prepareEvents();

    for (int i = 0; i < events.size(); i++ )
    {
      SweepLineEvent ev = (SweepLineEvent) events.get(i);
      if (ev.isInsert()) {
        processOverlaps(i, ev.getDeleteEventIndex(), ev, si);
      }
      if (si.isDone()) {
        break;
      }
    }
  }

  private void processOverlaps(int start, int end, SweepLineEvent ev0, SegmentIntersector si)
  {
    MonotoneChain mc0 = (MonotoneChain) ev0.getObject();
    /**
     * Since we might need to test for self-intersections,
     * include current INSERT event object in list of event objects to test.
     * Last index can be skipped, because it must be a Delete event.
     */
    for (int i = start; i < end; i++ ) {
      SweepLineEvent ev1 = (SweepLineEvent) events.get(i);
      if (ev1.isInsert()) {
        MonotoneChain mc1 = (MonotoneChain) ev1.getObject();
        // don't compare edges in same group, if labels are present
        if (! ev0.isSameLabel(ev1)) {
          mc0.computeIntersections(mc1, si);
          nOverlaps++;
        }
      }
    }
  }
}
