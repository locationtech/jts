/*
 * Copyright (c) 2023 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Position;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.Debug;

class RelateNode {

  private Coordinate nodePt;
  
  /**
   * A list of the edges around the node in CCW order,
   * ordered by their CCW angle with the positive X-axis.
   */
  private ArrayList<RelateEdge> edges = new ArrayList<RelateEdge>();

  public RelateNode(Coordinate pt) {
    this.nodePt = pt;
  }

  public Coordinate getCoordinate() {
    return nodePt;
  }
  
  public List<RelateEdge> getEdges() {
    return edges;
  }
  

  public void addEdges(List<NodeSection> nss) {
    for (NodeSection ns : nss) {
      addEdges(ns);
    }
  }
  
  public void addEdges(NodeSection ns) {
  //Debug.println("Adding NS: " + ns);
    switch (ns.dimension()) {
    case Dimension.L: 
      addLineEdge(ns.isA(), ns.getVertex(0));
      addLineEdge(ns.isA(), ns.getVertex(1));
      break;
    case Dimension.A:
      //-- assumes node edges have CW orientation (as per JTS norm)
      //-- entering edge - interior on L
      RelateEdge e0 = addAreaEdge(ns.isA(), ns.getVertex(0), false);
      //-- exiting edge - interior on R
      RelateEdge e1 = addAreaEdge(ns.isA(), ns.getVertex(1), true);

      int index0 = edges.indexOf(e0);
      int index1 = edges.indexOf(e1);
      updateEdgesInArea(ns.isA(), index0, index1);
      updateIfAreaPrev(ns.isA(), index0);
      updateIfAreaNext(ns.isA(), index1);
    }
  }

  private void updateEdgesInArea(boolean isA, int indexFrom, int indexTo) {
    int index = nextIndex(edges, indexFrom);
    while (index != indexTo) {
      RelateEdge edge = edges.get(index);
      edge.setAreaInterior(isA);
      index = nextIndex(edges, index);
    }
  }
  
  private void updateIfAreaPrev(boolean isA, int index) {
    int indexPrev = prevIndex(edges, index);
    RelateEdge edgePrev = edges.get(indexPrev);
    if (edgePrev.isInterior(isA, Position.LEFT)) {
      RelateEdge edge = edges.get(index);
      edge.setAreaInterior(isA);
    }
  }
  
  private void updateIfAreaNext(boolean isA, int index) {
    int indexNext = nextIndex(edges, index);
    RelateEdge edgeNext = edges.get(indexNext);
    if (edgeNext.isInterior(isA, Position.RIGHT)) {
      RelateEdge edge = edges.get(index);
      edge.setAreaInterior(isA);
    }
  }

  private RelateEdge addLineEdge(boolean isA, Coordinate dirPt) {
    return addEdge(isA, dirPt, Dimension.L, false);
  }
  
  private RelateEdge addAreaEdge(boolean isA, Coordinate dirPt, boolean isForward) {
    return addEdge(isA, dirPt, Dimension.A, isForward);
  }
  
  /**
   * Adds or merges an edge to the node.
   * 
   * @param isA
   * @param dirPt
   * @param dim dimension of the geometry element containing the edge
   * @param isForward the direction of the edge
   * 
   * @return the created or merged edge for this point
   */
  private RelateEdge addEdge(boolean isA, Coordinate dirPt, int dim, boolean isForward) {
    //-- check for well-formed edge - skip null or zero-len input
    if (dirPt == null)
      return null;
    if (nodePt.equals2D(dirPt))
      return null;
    
    int insertIndex = -1;
    for (int i = 0; i < edges.size(); i++) {
      RelateEdge e = edges.get(i);
      int comp = e.compareToEdge(dirPt);
      if (comp == 0) {
        e.merge(isA, dirPt, dim, isForward);
        return e;
      }
      if (comp == 1 ) {
        //-- found further edge, so insert a new edge at this position
        insertIndex = i;
        break;
      }
    }
    //-- add a new edge
    RelateEdge e = RelateEdge.create(this, dirPt, isA, dim, isForward);
    if (insertIndex < 0) {
      //-- add edge at end of list
      edges.add(e);
    }
    else {
      //-- add edge before higher edge found
      edges.add(insertIndex, e);
    }
    return e;
  }
  
  /**
   * Computes the final topology for the edges around this node.
   * Although nodes lie on the boundary of areas or the interior of lines,
   * in a mixed GC they may also lie in the interior of an area.
   * This changes the locations of the sides and line to Interior.
   * 
   * @param isAreaInteriorA true if the node is in the interior of A
   * @param isAreaInteriorB true if the node is in the interior of B
   */
  public void finish(boolean isAreaInteriorA, boolean isAreaInteriorB) {

//Debug.println("finish Node.");
//Debug.println("Before: " + this);
    
    finishNode(RelateGeometry.GEOM_A, isAreaInteriorA);
    finishNode(RelateGeometry.GEOM_B, isAreaInteriorB);
//Debug.println("After: " + this);
  }
  
  private void finishNode(boolean isA, boolean isAreaInterior) {
    if (isAreaInterior) {
      RelateEdge.setAreaInterior(edges, isA);
    }
    else {
      int startIndex = RelateEdge.findKnownEdgeIndex(edges, isA);
      //-- only interacting nodes are finished, so this should never happen
      //Assert.isTrue(startIndex >= 0l, "Node at "+ nodePt + "does not have AB interaction");
      propagateSideLocations(isA, startIndex);
    }
  } 
  
  private void propagateSideLocations(boolean isA, int startIndex) {
    int currLoc = edges.get(startIndex).location(isA, Position.LEFT);
    //-- edges are stored in CCW order
    int index = nextIndex(edges, startIndex);
    while (index != startIndex) {
      RelateEdge e = edges.get(index);
      e.setUnknownLocations(isA, currLoc);
      currLoc = e.location(isA,  Position.LEFT);
      index = nextIndex(edges, index);
    }
  }

  private static int prevIndex(ArrayList<RelateEdge> list, int index) {
    if (index > 0)
      return index - 1;
    //-- index == 0
    return list.size() - 1;
  }
  
  private static int nextIndex(List<RelateEdge> list, int i) {
    if (i >= list.size() - 1) {
      return 0;
    }
    return i + 1;
  }
  
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("Node[" + WKTWriter.toPoint(nodePt) + "]:");
    buf.append("\n");
    for (RelateEdge e : edges) {
      buf.append(e.toString());
      buf.append("\n");
    }
    return buf.toString();
  }

  public boolean hasExteriorEdge(boolean isA) {
    for (RelateEdge e : edges) {
      if (Location.EXTERIOR == e.location(isA, Position.LEFT)
          || Location.EXTERIOR == e.location(isA, Position.RIGHT)) {
        return true;
      }
    }
    return false;
  }
}
