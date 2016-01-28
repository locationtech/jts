


/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package org.locationtech.jts.geomgraph.index;

import java.util.*;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geomgraph.Quadrant;


/**
 * MonotoneChains are a way of partitioning the segments of an edge to
 * allow for fast searching of intersections.
 * Specifically, a sequence of contiguous line segments
 * is a monotone chain iff all the vectors defined by the oriented segments
 * lies in the same quadrant.
 * <p>
 * Monotone Chains have the following useful properties:
 * <ol>
 * <li>the segments within a monotone chain will never intersect each other
 * <li>the envelope of any contiguous subset of the segments in a monotone chain
 * is simply the envelope of the endpoints of the subset.
 * </ol>
 * Property 1 means that there is no need to test pairs of segments from within
 * the same monotone chain for intersection.
 * Property 2 allows
 * binary search to be used to find the intersection points of two monotone chains.
 * For many types of real-world data, these properties eliminate a large number of
 * segment comparisons, producing substantial speed gains.
 * <p>
 * Note that due to the efficient intersection test, there is no need to limit the size
 * of chains to obtain fast performance.
 *
 * @version 1.7
 */
public class MonotoneChainIndexer {

  public static int[] toIntArray(List list)
  {
    int[] array = new int[list.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = ((Integer) list.get(i)).intValue();
    }
    return array;
  }

  public MonotoneChainIndexer() {
  }

  public int[] getChainStartIndices(Coordinate[] pts)
  {
    // find the startpoint (and endpoints) of all monotone chains in this edge
    int start = 0;
    List startIndexList = new ArrayList();
    startIndexList.add(new Integer(start));
    do {
      int last = findChainEnd(pts, start);
      startIndexList.add(new Integer(last));
      start = last;
    } while (start < pts.length - 1);
    // copy list to an array of ints, for efficiency
    int[] startIndex = toIntArray(startIndexList);
    return startIndex;
  }

  /**
   * @return the index of the last point in the monotone chain
   */
  private int findChainEnd(Coordinate[] pts, int start)
  {
    // determine quadrant for chain
    int chainQuad = Quadrant.quadrant(pts[start], pts[start + 1]);
    int last = start + 1;
    while (last < pts.length ) {
      //if (last - start > 100) break;
      // compute quadrant for next possible segment in chain
      int quad = Quadrant.quadrant(pts[last - 1], pts[last]);
      if (quad != chainQuad) break;
      last++;
    }
    return last - 1;
  }



}
