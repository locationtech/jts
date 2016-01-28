
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

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.planargraph.Edge;

/**
 * An edge of a {@link LineMergeGraph}. The <code>marked</code> field indicates
 * whether this Edge has been logically deleted from the graph.
 *
 * @version 1.7
 */
public class LineMergeEdge extends Edge {
  private LineString line;
  /**
   * Constructs a LineMergeEdge with vertices given by the specified LineString.
   */
  public LineMergeEdge(LineString line) {
    this.line = line;
  }
  /**
   * Returns the LineString specifying the vertices of this edge.
   */
  public LineString getLine() {
    return line;
  }
}
