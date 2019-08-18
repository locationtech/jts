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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.Debug;

public class OverlayGraph {
  
  private List<OverlayEdge> edges = new ArrayList<OverlayEdge>();
  private Map<Coordinate, OverlayEdge> nodeMap = new HashMap<Coordinate, OverlayEdge>();
  private InputGeometry inputGeometry;
  
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
    OverlayEdge e0 = OverlayEdge.createEdge(pts, lbl, true);
    OverlayEdge e1 = OverlayEdge.createEdge(pts, lbl, false);
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
   * Computes the topological labelling for the edges in the graph.
   * 
   */
  public void computeLabelling(InputGeometry inputGeom) {
    this.inputGeometry = inputGeom;
    // compute labelling using a Left-Right sweepline, to keep things deterministic
    // MD - not technically needed, so skip to improve performance
    //List<OverlayEdge> nodes = sortedNodes();
    
    Collection<OverlayEdge> nodes = getNodeEdges();
    labelAreaNodeEdges(nodes);
    labelConnectedLinearEdges();
    
    /**
     * At this point collapsed edges with unknown location
     * must be disconnected
     * from the area edges of the parent.
     * They can be located based on their parent ring role (shell or hole).
     * 
     * Note that this can NOT be done via a PIP location check,
     * because that is done against the unreduced input geometry,
     * which may give an invalid result due to topology collapse.
     * 
     * The labelling is propagated to other connected edges, 
     * since there may be NOT_PART edges which are connected, 
     * and they need to be labelled in the same way.
     */
    //TODO: is there a way to avoid scanning all edges in these steps?
    labelCollapsedEdges();
    labelConnectedLinearEdges();
    
    labelDisconnectedEdges();
  }

  /*
  private List<OverlayEdge> sortedNodes() {
    List<OverlayEdge> edges = new ArrayList<OverlayEdge>(getNodeEdges());
    edges.sort(OverlayEdge.nodeComparator());
    return edges;
  }
*/
  /**
   * There can be edges which have unknown location
   * but are connected to a Line edge with known location.
   * In this case line location is propagated to the connected edges.
   */
  private void labelConnectedLinearEdges() {
    //TODO: can these be merged to avoid two scans?
    propagateLineLocations(0);
    propagateLineLocations(1);
  }

  private void propagateLineLocations(int geomIndex) {
    // find L edges
    List<OverlayEdge> lineEdges = findLinearEdgesWithLocation(geomIndex);
    Deque<OverlayEdge> edgeStack = new ArrayDeque<OverlayEdge>(lineEdges);
    
    propagateLineLocations(geomIndex, edgeStack);
  }
  
  private void propagateLineLocations(int geomIndex, Deque<OverlayEdge> edgeStack) {
    // traverse line edges, labelling unknown ones that are connected
    while (! edgeStack.isEmpty()) {
      OverlayEdge lineEdge = edgeStack.removeFirst();
      // assert: lineEdge.getLabel().isLine(geomIndex);
      
      // for any edges around origin with unknown location for this geomIndex,
      // mark them as Exterior
      // add those edges to stack to continue traversal
      OverlayNode.propagateLineLocation(lineEdge, geomIndex, edgeStack, inputGeometry);
    }
  }
  
  
  /**
   * Finds all OverlayEdges which are labelled as L dimension.
   * 
   * @param geomIndex
   * @return list of L edges
   */
  private List<OverlayEdge> findLinearEdgesWithLocation(int geomIndex) {
    List<OverlayEdge> lineEdges = new ArrayList<OverlayEdge>();
    for (OverlayEdge edge : edges) {
      OverlayLabel lbl = edge.getLabel();
      if (lbl.isLinear(geomIndex)
          && ! lbl.isLineLocationUnknown(geomIndex)) {
        lineEdges.add(edge);
      }
    }
    return lineEdges;
  }

  private void labelAreaNodeEdges(Collection<OverlayEdge> nodes) {
    for (OverlayEdge nodeEdge : nodes) {
      OverlayNode.labelAreaNodeEdges(nodeEdge);
    }
  }

  private void labelCollapsedEdges() {
    for (OverlayEdge edge : edges) {
      if (edge.getLabel().isLineLocationUnknown(0)) {
        labelCollapsedEdge(edge, 0);
      }
      if (edge.getLabel().isLineLocationUnknown(1)) {
        labelCollapsedEdge(edge, 1);
      }
    }
  }

  private void labelCollapsedEdge(OverlayEdge edge, int geomIndex) {
    //Debug.println("\n------  labelCollapsedEdge - geomIndex= " + geomIndex);
    //Debug.print("BEFORE: " + edge.toStringNode());
    OverlayLabel label = edge.getLabel();
    if (! label.isCollapse(geomIndex)) return;
      /**
       * This must be a collapsed edge which is disconnected
       * from any area edges (e.g. a fully collapsed shell or hole).
       * It can be labelled according to its parent source ring role. 
       */
    label.setLocationCollapse(geomIndex);
    //Debug.print("AFTER: " + edge.toStringNode());
  }

  private void labelDisconnectedEdges() {
    for (OverlayEdge edge : edges) {
      //Debug.println("\n------  checking for Disconnected edge " + edge);
      if (edge.getLabel().isLineLocationUnknown(0)) {
        labelDisconnectedEdge(edge, 0);
      }
      if (edge.getLabel().isLineLocationUnknown(1)) {
        labelDisconnectedEdge(edge, 1);
      }
    }
  }

  /**
   * Labels edges which are disconnected from other
   * edges which could provide location information.
   * The location is determined by checking 
   * if the edge lies inside the given input area (if any)
   * @param edge
   * @param geomIndex
   */
  private void labelDisconnectedEdge(OverlayEdge edge, int geomIndex) {    
    /**
     * Only area geometries provide a location for edges
     */
    if (! inputGeometry.isArea(geomIndex)) return;
    
    //Debug.println("\n------  labelDisconnectedEdge - geomIndex= " + geomIndex);
    //Debug.print("BEFORE: " + edge.toStringNode());
    
    OverlayLabel label = edge.getLabel();
    // TODO: ??? locate in the result area, not original geometry, in case of collapse 
    /**
     * This must be an edge which is disconnected from  
     * the given input geometry area.
     */
    int edgeLoc = locateEdge(geomIndex, edge);
    label.setLocationAll(geomIndex, edgeLoc);
    //Debug.print("AFTER: " + edge.toStringNode());
  }

  /**
   * Determines the {@link Location} for an edge within an Area geometry
   * via point location.
   * 
   * @param geomIndex the parent geometry index
   * @param edge the edge to locate
   * @return the location of the edge
   */
  private int locateEdge(int geomIndex, OverlayEdge edge) {
    /*
     * To improve the robustness of the point location,
     * check both ends of the edge.
     * Edge is only labelled INTERIOR if both ends are.
     */
    int loc1 = inputGeometry.locatePoint(geomIndex, edge.orig());
    int loc2 = inputGeometry.locatePoint(geomIndex, edge.dest());
    boolean bothInterior = loc1 == Location.INTERIOR && loc2 == Location.INTERIOR;
    int edgeLoc = bothInterior ? Location.INTERIOR : Location.EXTERIOR;
    return edgeLoc;
  }

  public void markResultAreaEdges(int overlayOpCode) {
    for (OverlayEdge edge : getEdges()) {
      markInResultArea(edge, overlayOpCode);
    }
  }

  public void markInResultArea(OverlayEdge e, int overlayOpCode) {
    OverlayLabel label = e.getLabel();
    if ( //isResultAreaEdge(label, overlayOpCode)
        label.isBoundaryEither()
        && OverlayNG.isResultOfOp(
              label.getLocationBoundaryOrLine(0, Position.RIGHT, e.isForward()),
              label.getLocationBoundaryOrLine(1, Position.RIGHT, e.isForward()),
              overlayOpCode)) {
      e.markInResult();  
    }
    Debug.println("markInResultArea: " + e);
  }
  
  /*
   // MD - too restrictive.  L edges can be in result boundary (e.g. collapsed hole)
  private boolean isResultAreaEdge(OverlayLabel label, int overlayOpCode) {
    switch (overlayOpCode) {
    case OverlayOp.INTERSECTION:
      return label.isAreaBoundaryOrNotPart(0) && label.isAreaBoundaryOrNotPart(1);
    case OverlayOp.UNION:
      return label.isAreaBoundaryOrNotPart(0) || label.isAreaBoundaryOrNotPart(1);
    case OverlayOp.DIFFERENCE:
      return label.isAreaBoundaryOrNotPart(0);
    case OverlayOp.SYMDIFFERENCE:
      return label.isAreaBoundaryOrNotPart(0) || label.isAreaBoundaryOrNotPart(1);
    }
    return false;
  }
  */
  
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
