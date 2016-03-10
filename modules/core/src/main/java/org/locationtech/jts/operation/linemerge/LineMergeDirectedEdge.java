
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
package org.locationtech.jts.operation.linemerge;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.planargraph.DirectedEdge;
import org.locationtech.jts.planargraph.Node;
import org.locationtech.jts.util.Assert;

/**
 * A {@link org.locationtech.jts.planargraph.DirectedEdge} of a 
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
