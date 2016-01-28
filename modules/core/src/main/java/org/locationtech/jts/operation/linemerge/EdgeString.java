
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
package org.locationtech.jts.operation.linemerge;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.*;

/**
 * A sequence of {@link LineMergeDirectedEdge}s forming one of the lines that will
 * be output by the line-merging process.
 *
 * @version 1.7
 */
public class EdgeString {
  private GeometryFactory factory;
  private List directedEdges = new ArrayList();
  private Coordinate[] coordinates = null;
  /**
   * Constructs an EdgeString with the given factory used to convert this EdgeString
   * to a LineString
   */
  public EdgeString(GeometryFactory factory) {
    this.factory = factory;
  }

  /**
   * Adds a directed edge which is known to form part of this line.
   */
  public void add(LineMergeDirectedEdge directedEdge) {
    directedEdges.add(directedEdge);
  }

  private Coordinate[] getCoordinates() {
    if (coordinates == null) {
      int forwardDirectedEdges = 0;
      int reverseDirectedEdges = 0;
      CoordinateList coordinateList = new CoordinateList();
      for (Iterator i = directedEdges.iterator(); i.hasNext();) {
        LineMergeDirectedEdge directedEdge = (LineMergeDirectedEdge) i.next();
        if (directedEdge.getEdgeDirection()) {
          forwardDirectedEdges++;
        }
        else {
          reverseDirectedEdges++;
        }
        coordinateList.add(((LineMergeEdge) directedEdge.getEdge()).getLine()
                            .getCoordinates(), false,
          directedEdge.getEdgeDirection());
      }
      coordinates = coordinateList.toCoordinateArray();
      if (reverseDirectedEdges > forwardDirectedEdges) {
        CoordinateArrays.reverse(coordinates);
      }
    }

    return coordinates;
  }

  /**
   * Converts this EdgeString into a LineString.
   */
  public LineString toLineString() {
    return factory.createLineString(getCoordinates());
  }
}
