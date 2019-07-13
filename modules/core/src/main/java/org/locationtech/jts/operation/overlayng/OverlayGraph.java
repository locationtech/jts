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
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.util.Debug;

public class OverlayGraph {
  
  private List<OverlayEdge> edges = new ArrayList<OverlayEdge>();
  private Map<Coordinate, OverlayEdge> nodeMap = new HashMap<Coordinate, OverlayEdge>();
  
  public OverlayGraph(Collection<Edge> edges) {
    build(edges);
  }

  /**
   * Gets the set of edges in this graph.
   * 
   * @return the collection of edges in this graph
   */
  public Collection<OverlayEdge> getEdges() 
  {
    return edges;
  }
  
  /**
   * Gets the collection of edges representing the nodes in this graph.
   * 
   * @return the collection of node edges
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
    OverlayEdge e = createEdges(edge.getCoordinates(), edge.getMergedLabel());
    //Debug.println("added edge: " + e);
    insert(e);
    insert((OverlayEdge) e.sym());
    return e;
  }
  
  private OverlayEdge createEdges(Coordinate[] pts, OverlayLabel lbl)
  {
    OverlayEdge e0 = OverlayEdge.createEdge(pts, lbl.copy(), true);
    OverlayEdge e1 = OverlayEdge.createEdge(pts, lbl.copyFlip(), false);
    e0.init(e1);
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
  
  /**
   * Computes a full topological labelling for all edges and nodes in the graph.
   */
  public void computeLabelling() {
    // compute labelling using a Left-Right sweepline, to keep things deterministic
    // MD - not technically needed, so skip to improve performance
    //List<OverlayEdge> nodes = sortedNodes();
    Collection<OverlayEdge> nodes = getNodeEdges();
    computeLabelling(nodes);
    mergeSymLabels(nodes);
  }

  /*
  private List<OverlayEdge> sortedNodes() {
    List<OverlayEdge> edges = new ArrayList<OverlayEdge>(getNodeEdges());
    edges.sort(OverlayEdge.nodeComparator());
    return edges;
  }
*/
  
  private void computeLabelling(Collection<OverlayEdge> nodes) {
    for (OverlayEdge nodeEdge : nodes) {
      OverlayNode.computeLabelling(nodeEdge);
    }
  }

  private void mergeSymLabels(Collection<OverlayEdge> nodes) {
    for (OverlayEdge node : nodes) {
      OverlayNode.mergeSymLabels(node);
    }
  }

  public void labelIncompleteEdges(InputGeometry inputGeom) {
    for (OverlayEdge edge : edges) {
      //Debug.println("\n------  checking for Incomplete edge " + edge);
      if (edge.getLabel().isUnknownLineLocation(0)) {
        labelIncompleteEdge(edge, 0, inputGeom);
      }
      if (edge.getLabel().isUnknownLineLocation(1)) {
        labelIncompleteEdge(edge, 1, inputGeom);
      }
    }
  }

  private void labelIncompleteEdge(OverlayEdge edge, int geomIndex, InputGeometry inputGeom) {
    //Debug.println("\n------  labelIncompleteEdge - geomIndex= " + geomIndex);
    //Debug.print("BEFORE: " + edge.toStringNode());
    int geomDim = inputGeom.getDimension(geomIndex);
    if (geomDim == OverlayLabel.DIM_AREA) {
      // TODO: locate in the result area, not original geometry, in case of collapse
      int loc = inputGeom.locatePoint(geomIndex, edge.orig());
      edge.getLabel().setLocationInArea(geomIndex, loc);
    }
    edge.mergeSymLabels();
    //Debug.print("AFTER: " + edge.toStringNode());
  }

  
  public void markResultAreaEdges(int overlayOpCode) {
    for (OverlayEdge edge : getEdges()) {
      markInResultArea(edge, overlayOpCode);
    }
  }

  public void markInResultArea(OverlayEdge e, int overlayOpCode) {
    OverlayLabel label = e.getLabel();
    if (label.isArea()
        //&& ! label.isInteriorArea()
        && OverlayNG.isResultOfOp(
              label.getLocationSideOrLine(0, Position.RIGHT),
              label.getLocationSideOrLine(1, Position.RIGHT),
              overlayOpCode)) {
      e.markInResult();  
    }
  }
  
  public void removeDuplicateResultAreaEdges() {
    for (OverlayEdge edge : getEdges()) {
      if ( edge.isInResult()  && edge.symOE().isInResult() ) {
        edge.removeFromResult();
        edge.symOE().removeFromResult();      
      }
    }
  }

  public List<OverlayEdge> getResultAreaEdges() {
    List<OverlayEdge> resultEdges = new ArrayList<OverlayEdge>();
    for (OverlayEdge edge : getEdges()) {
      if (edge.isInResult()) {
        resultEdges.add(edge);
      }
    } 
    return resultEdges;
  }


}
