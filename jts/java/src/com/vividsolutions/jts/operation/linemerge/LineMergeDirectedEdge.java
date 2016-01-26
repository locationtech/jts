
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
package com.vividsolutions.jts.operation.linemerge;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.planargraph.DirectedEdge;
import com.vividsolutions.jts.planargraph.Node;
import com.vividsolutions.jts.util.Assert;

/**
 * A {@link com.vividsolutions.jts.planargraph.DirectedEdge} of a 
 * {@link LineMergeGraph}. 
 *
 * @version 1.7
 */
public class LineMergeDirectedEdge extends DirectedEdge {
  /**
   * Constructs a LineMergeDirectedEdge connecting the <code>from</code> node to the
   * <code>to</code> node.
   *
   * @param directionPt
   *                  specifies this DirectedEdge's direction (given by an imaginary
   *                  line from the <code>from</code> node to <code>directionPt</code>)
   * @param edgeDirection
   *                  whether this DirectedEdge's direction is the same as or
   *                  opposite to that of the parent Edge (if any)
   */  
  public LineMergeDirectedEdge(Node from, Node to, Coordinate directionPt,
    boolean edgeDirection) {
    super(from, to, directionPt, edgeDirection);
  }

  /**
   * Returns the directed edge that starts at this directed edge's end point, or null
   * if there are zero or multiple directed edges starting there.  
   * @return the directed edge
   */
  public LineMergeDirectedEdge getNext() {
    if (getToNode().getDegree() != 2) {
      return null;
    }
    if (getToNode().getOutEdges().getEdges().get(0) == getSym()) {
      return (LineMergeDirectedEdge) getToNode().getOutEdges().getEdges().get(1);
    }
    Assert.isTrue(getToNode().getOutEdges().getEdges().get(1) == getSym());

    return (LineMergeDirectedEdge) getToNode().getOutEdges().getEdges().get(0);
  }
}
