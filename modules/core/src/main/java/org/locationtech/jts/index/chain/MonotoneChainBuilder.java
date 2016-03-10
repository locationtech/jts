

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
package org.locationtech.jts.index.chain;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geomgraph.Quadrant;

/**
 * Constructs {@link MonotoneChain}s
 * for sequences of {@link Coordinate}s.
 *
 * @version 1.7
 */
public class MonotoneChainBuilder {

  public static int[] toIntArray(List list)
  {
    int[] array = new int[list.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = ((Integer) list.get(i)).intValue();
    }
    return array;
  }

  public static List getChains(Coordinate[] pts)
  {
    return getChains(pts, null);
  }

  /**
   * Return a list of the {@link MonotoneChain}s
   * for the given list of coordinates.
   */
  public static List getChains(Coordinate[] pts, Object context)
  {
    List mcList = new ArrayList();
    int[] startIndex = getChainStartIndices(pts);
    for (int i = 0; i < startIndex.length - 1; i++) {
      MonotoneChain mc = new MonotoneChain(pts, startIndex[i], startIndex[i + 1], context);
      mcList.add(mc);
    }
    return mcList;
  }

  /**
   * Return an array containing lists of start/end indexes of the monotone chains
   * for the given list of coordinates.
   * The last entry in the array points to the end point of the point array,
   * for use as a sentinel.
   */
  public static int[] getChainStartIndices(Coordinate[] pts)
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
   * Finds the index of the last point in a monotone chain
   * starting at a given point.
   * Any repeated points (0-length segments) will be included
   * in the monotone chain returned.
   * 
   * @return the index of the last point in the monotone chain 
   * starting at <code>start</code>.
   */
  private static int findChainEnd(Coordinate[] pts, int start)
  {
  	int safeStart = start;
  	// skip any zero-length segments at the start of the sequence
  	// (since they cannot be used to establish a quadrant)
  	while (safeStart < pts.length - 1 && pts[safeStart].equals2D(pts[safeStart + 1])) {
  		safeStart++;
  	}
  	// check if there are NO non-zero-length segments
  	if (safeStart >= pts.length - 1) {
  		return pts.length - 1;
  	}
    // determine overall quadrant for chain (which is the starting quadrant)
    int chainQuad = Quadrant.quadrant(pts[safeStart], pts[safeStart + 1]);
    int last = start + 1;
    while (last < pts.length) {
    	// skip zero-length segments, but include them in the chain
    	if (! pts[last - 1].equals2D(pts[last])) {
        // compute quadrant for next possible segment in chain
    		int quad = Quadrant.quadrant(pts[last - 1], pts[last]);
      	if (quad != chainQuad) break;
    	}
      last++;
    }
    return last - 1;
  }


  public MonotoneChainBuilder() {
  }
}
