/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlaysr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * One constraint that is maintained is that the direction of linear
 * edges should be preserved if possible (which is the case if there is 
 * no other coincident edge, or if all coincident edges have the same direction).
 * This ensures that the overlay output line direction will be as consistent
 * as possible with input lines.
 * 
 * @author mdavis
 *
 */
public class EdgeMerger {

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
    for (Edge ss : edges) {
      EdgeKey edgeKey = EdgeKey.create(ss);
      Edge existing = edgeMap.get(edgeKey);
      if (existing == null) {
        edgeMap.put(edgeKey, ss);
      }
      else {
        // TODO: check that edges are identical (up to direction)
        mergeLabel(existing, ss);
      }
    }
    return new ArrayList<Edge>(edgeMap.values());
  }
  
  private void mergeLabel(Edge target, Edge edge) {
    OverlayLabel lblTarget = target.getLabel();
    OverlayLabel lblToMerge = edge.getLabel();
    boolean relDir = relativeDirection(target, edge);
    if (relDir) {
      lblTarget.merge(lblToMerge);
    }
    else {
      lblTarget.mergeFlip(lblToMerge);
    }
  }

  /**
   * Compares two coincident edges to determine
   * whether they have the same or opposite direction.
   * 
   * @param edge1 an edge
   * @param edge2 an edge
   * @return true if the edges have the same direction, false if not
   */
  private static boolean relativeDirection(Edge edge1, Edge edge2) {
    // assert: the edges match (have the same coordinates up to direction)
    if (! edge1.getCoordinate(0).equals2D(edge2.getCoordinate(0)))
      return false;
    if (! edge1.getCoordinate(1).equals2D(edge2.getCoordinate(1)))
      return false;
    return true;
  }

}
