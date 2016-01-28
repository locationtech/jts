
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

import java.util.*;

/**
 * Computes all intersections between segments in a set of {@link SegmentString}s.
 * Intersections found are represented as {@link SegmentNode}s and added to the
 * {@link SegmentString}s in which they occur.
 * As a final step in the noding a new set of segment strings split
 * at the nodes may be returned.
 *
 * @version 1.7
 */
public interface Noder
{

  /**
   * Computes the noding for a collection of {@link SegmentString}s.
   * Some Noders may add all these nodes to the input SegmentStrings;
   * others may only add some or none at all.
   *
   * @param segStrings a collection of {@link SegmentString}s to node
   */
  void computeNodes(Collection segStrings);

  /**
   * Returns a {@link Collection} of fully noded {@link SegmentString}s.
   * The SegmentStrings have the same context as their parent.
   *
   * @return a Collection of SegmentStrings
   */
  Collection getNodedSubstrings();

}
