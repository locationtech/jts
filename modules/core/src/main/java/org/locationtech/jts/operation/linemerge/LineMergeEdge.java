
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
