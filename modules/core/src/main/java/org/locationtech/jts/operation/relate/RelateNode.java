


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
package org.locationtech.jts.operation.relate;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geomgraph.EdgeEndStar;
import org.locationtech.jts.geomgraph.Node;

/**
 * Represents a node in the topological graph used to compute spatial relationships.
 *
 * @version 1.7
 */
public class RelateNode
  extends Node
{

  public RelateNode(Coordinate coord, EdgeEndStar edges)
  {
    super(coord, edges);
  }

  /**
   * Update the IM with the contribution for this component.
   * A component only contributes if it has a labelling for both parent geometries
   */
  protected void computeIM(IntersectionMatrix im)
  {
    im.setAtLeastIfValid(label.getLocation(0), label.getLocation(1), 0);
  }
  /**
   * Update the IM with the contribution for the EdgeEnds incident on this node.
   */
  void updateIMFromEdges(IntersectionMatrix im)
  {
    ((EdgeEndBundleStar) edges).updateIM(im);
  }

}
