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

import org.locationtech.jts.geom.Coordinate;

/**
 * A planar graph of {@link OverlayEdge}s, representing
 * the topology resulting from an overlay operation.
 * Each source {@link Edge} is represented
 * by two OverlayEdges, with opposite orientation.
 * A single {@link OverlayLabel} is created for each symmetric pair of OverlayEdges.
 * 
 * @author mdavis
 *
 */
class OverlayGraph {
  
  private List<OverlayEdge> edges = new ArrayList<OverlayEdge>();
  private Map<Coordinate, OverlayEdge> nodeMap = new HashMap<Coordinate, OverlayEdge>();
  
  /**
   * Creates a new graph for a set of noded, labelled {@link Edge}s.
   * 
   * @param edges the edges on which to build the graph
   */
  public OverlayGraph(Collection<Edge> edges) {
    build(edges);
  }

  /**
   * Gets the set of edges in this graph.
   * Only one of each symmetric pair of OverlayEdges is included. 
   * The opposing edge can be found by using {@link OverlayEdge#sym()}.
   * 
   * @return the collection of representative edges in this graph
   */
  public Collection<OverlayEdge> getEdges() 
  {
    return edges;
  }
  
  /**
   * Gets the collection of edges representing the nodes in this graph.
   * For each star of edges originating at a node
   * a single representative edge is included.
   * The other edges around the node can be found by following the next and prev links.
   * 
   * @return the collection of representative node edges
   */
  public Collection<OverlayEdge> getNodeEdges()
  {
    return nodeMap.values();
  }

  /**
   * Gets an edge originating at the given node point.
   * 
   * @param nodePt the node coordinate to query
   * @return an edge originating at the point, or null if none exists
   */
  public OverlayEdge getNodeEdge(Coordinate nodePt) {
    return nodeMap.get(nodePt);
  }
  
  /**
   * Gets the representative edges marked as being in the result area.
   * 
   * @return the result area edges
   */
  public List<OverlayEdge> getResultAreaEdges() {
    List<OverlayEdge> resultEdges = new ArrayList<OverlayEdge>();
    for (OverlayEdge edge : getEdges()) {
      if (edge.isInResultArea()) {
        resultEdges.add(edge);
      }
    } 
    return resultEdges;
  }
  
  private void build(Collection<Edge> edges) {
    for (Edge e : edges) {
      addEdge(e);
    }
  }
  
  /**
   * Adds an edge between the coordinates orig and dest
   * to this graph.
   * Only valid edges can be added (in particular, zero-length segments cannot be added)
   * 
   * @param orig the edge origin location
   * @param dest the edge destination location.
   * @return the created edge
   * @return null if the edge was invalid and not added
   * 
   * @see #isValidEdge(Coordinate, Coordinate)
   */
  private OverlayEdge addEdge(Edge edge) {
    //if (! isValidEdge(orig, dest)) return null;
    OverlayEdge e = createEdges(edge.getCoordinates(), edge.createLabel());
    //Debug.println("added edge: " + e);
    insert(e);
    insert((OverlayEdge) e.sym());
    return e;
  }
  
  private OverlayEdge createEdges(Coordinate[] pts, OverlayLabel lbl)
  {
    OverlayEdge e0 = OverlayEdge.createEdge(pts, lbl, true);
    OverlayEdge e1 = OverlayEdge.createEdge(pts, lbl, false);
    e0.link(e1);
    return e0;
  }

  /**
   * Tests if the given coordinates form a valid edge (with non-zero length).
   * 
   * @param orig the start coordinate
   * @param dest the end coordinate
   * @return true if the edge formed is valid
   */
  private static boolean isValidEdge(Coordinate orig, Coordinate dest) {
    int cmp = dest.compareTo(orig);
    return cmp != 0;
  }

  private void insert(OverlayEdge e) {
    edges.add(e);
    OverlayEdge nodeEdge = (OverlayEdge) nodeMap.get(e.orig());
    if (nodeEdge != null) {
      nodeEdge.insert(e);
    }
    else {
      // add edge origin to node map
      // (sym is also added in separate call)
      nodeMap.put(e.orig(), e);
    }
  }

}
