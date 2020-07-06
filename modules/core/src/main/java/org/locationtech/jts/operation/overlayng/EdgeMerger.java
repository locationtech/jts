/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.Debug;

/**
 * Performs merging on the noded edges of the input geometries.
 * Merging takes place on edges which are coincident 
 * (i.e. have the same coordinate list, modulo direction).
 * The following situations can occur:
 * <ul>
 * <li>Coincident edges from different input geometries have their labels combined
 * <li>Coincident edges from the same area geometry indicate a topology collapse.
 * In this case the topology locations are "summed" to provide a final
 * assignment of side location
 * <li>Coincident edges from the same linear geometry can simply be merged 
 * using the same ON location
 * </ul>
 * 
 * The merging attempts to preserve the direction of linear
 * edges if possible (which is the case if there is 
 * no other coincident edge, or if all coincident edges have the same direction).
 * This ensures that the overlay output line direction will be as consistent
 * as possible with input lines.
 * 
 * @author mdavis
 *
 */
class EdgeMerger {

  public static List<Edge> merge(List<Edge> edges) {
    EdgeMerger merger = new EdgeMerger(edges);
    return merger.merge();
  }

  private Collection<Edge> edges;
  private Map<EdgeKey, Edge> edgeMap = new HashMap<EdgeKey, Edge>();
  
  public EdgeMerger(List<Edge> edges) {
    this.edges = edges;
  }
  
  public ArrayList<Edge> merge() {
    for (Edge edge : edges) {
      EdgeKey edgeKey = EdgeKey.create(edge);
      Edge baseEdge = edgeMap.get(edgeKey);
      if (baseEdge == null) {
        // this is the first (and maybe only) edge for this line
        edgeMap.put(edgeKey, edge);
        //Debug.println("edge added: " + edge);
        //Debug.println(edge.toLineString());
      }
      else {
        // found an existing edge
        
        // Assert: edges are identical (up to direction)
        // this is a fast (but incomplete) sanity check
        Assert.isTrue(baseEdge.size() == edge.size(),
            "Merge of edges of different sizes - probable noding error.");
        
        baseEdge.merge(edge);
        //Debug.println("edge merged: " + existing);
        //Debug.println(edge.toLineString());
      }
    }
    return new ArrayList<Edge>(edgeMap.values());
  }

}
