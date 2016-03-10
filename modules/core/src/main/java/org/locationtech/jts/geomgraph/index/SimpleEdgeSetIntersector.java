


/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geomgraph.index;

import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geomgraph.Edge;

/**
 * Finds all intersections in one or two sets of edges,
 * using the straightforward method of
 * comparing all segments.
 * This algorithm is too slow for production use, but is useful for testing purposes.
 * @version 1.7
 */
public class SimpleEdgeSetIntersector
  extends EdgeSetIntersector
{
  // statistics information
  int nOverlaps;

  public SimpleEdgeSetIntersector() {
  }

  public void computeIntersections(List edges, SegmentIntersector si, boolean testAllSegments)
  {
    nOverlaps = 0;

    for (Iterator i0 = edges.iterator(); i0.hasNext(); ) {
      Edge edge0 = (Edge) i0.next();
      for (Iterator i1 = edges.iterator(); i1.hasNext(); ) {
        Edge edge1 = (Edge) i1.next();
        if (testAllSegments || edge0 != edge1)
          computeIntersects(edge0, edge1, si);
      }
    }
  }


  public void computeIntersections(List edges0, List edges1, SegmentIntersector si)
  {
    nOverlaps = 0;

    for (Iterator i0 = edges0.iterator(); i0.hasNext(); ) {
      Edge edge0 = (Edge) i0.next();
      for (Iterator i1 = edges1.iterator(); i1.hasNext(); ) {
        Edge edge1 = (Edge) i1.next();
        computeIntersects(edge0, edge1, si);
      }
    }
  }

  /**
   * Performs a brute-force comparison of every segment in each Edge.
   * This has n^2 performance, and is about 100 times slower than using
   * monotone chains.
   */
  private void computeIntersects(Edge e0, Edge e1, SegmentIntersector si)
  {
   Coordinate[] pts0 = e0.getCoordinates();
    Coordinate[] pts1 = e1.getCoordinates();
    for (int i0 = 0; i0 < pts0.length - 1; i0++) {
      for (int i1 = 0; i1 < pts1.length - 1; i1++) {
        si.addIntersections(e0, i0, e1, i1);
      }
    }
  }
}
