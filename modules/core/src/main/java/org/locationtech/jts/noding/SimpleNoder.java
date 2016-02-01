
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
package org.locationtech.jts.noding;

import java.util.Collection;
import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;

/**
 * Nodes a set of {@link SegmentString}s by
 * performing a brute-force comparison of every segment to every other one.
 * This has n^2 performance, so is too slow for use on large numbers
 * of segments.
 *
 * @version 1.7
 */
public class SimpleNoder
    extends SinglePassNoder
{

  private Collection nodedSegStrings;

  public SimpleNoder() {
  }

  public Collection getNodedSubstrings()
  {
    return  NodedSegmentString.getNodedSubstrings(nodedSegStrings);
  }

  public void computeNodes(Collection inputSegStrings)
  {
    this.nodedSegStrings = inputSegStrings;
    for (Iterator i0 = inputSegStrings.iterator(); i0.hasNext(); ) {
      SegmentString edge0 = (SegmentString) i0.next();
      for (Iterator i1 = inputSegStrings.iterator(); i1.hasNext(); ) {
        SegmentString edge1 = (SegmentString) i1.next();
        computeIntersects(edge0, edge1);
      }
    }
  }

  private void computeIntersects(SegmentString e0, SegmentString e1)
  {
    Coordinate[] pts0 = e0.getCoordinates();
    Coordinate[] pts1 = e1.getCoordinates();
    for (int i0 = 0; i0 < pts0.length - 1; i0++) {
      for (int i1 = 0; i1 < pts1.length - 1; i1++) {
        segInt.processIntersections(e0, i0, e1, i1);
      }
    }
  }

}
