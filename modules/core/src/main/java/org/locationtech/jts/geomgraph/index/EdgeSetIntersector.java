


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

/**
 * @version 1.7
 */
import java.util.List;

/**
 * An EdgeSetIntersector computes all the intersections between the
 * edges in the set.  It adds the computed intersections to each edge
 * they are found on.  It may be used in two scenarios:
 * <ul>
 * <li>determining the internal intersections between a single set of edges
 * <li>determining the mutual intersections between two different sets of edges
 * </ul>
 * It uses a {@link SegmentIntersector} to compute the intersections between
 * segments and to record statistics about what kinds of intersections were found.
 *
 * @version 1.7
 */
public abstract class EdgeSetIntersector 
{
  public EdgeSetIntersector() {
  }

  /**
   * Computes all self-intersections between edges in a set of edges,
   * allowing client to choose whether self-intersections are computed.
   *
   * @param edges a list of edges to test for intersections
   * @param si the SegmentIntersector to use
   * @param testAllSegments true if self-intersections are to be tested as well
   */
  abstract public void computeIntersections(List edges, SegmentIntersector si, boolean testAllSegments);

  /**
   * Computes all mutual intersections between two sets of edges.
   */
  abstract public void computeIntersections(List edges0, List edges1, SegmentIntersector si);



}
