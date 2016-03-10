
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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.planargraph.DirectedEdge;
import org.locationtech.jts.planargraph.Edge;
import org.locationtech.jts.planargraph.Node;
import org.locationtech.jts.planargraph.PlanarGraph;

/**
 * A planar graph of edges that is analyzed to sew the edges together. The 
 * <code>marked</code> flag on @{link com.vividsolutions.planargraph.Edge}s 
 * and @{link com.vividsolutions.planargraph.Node}s indicates whether they have been
 * logically deleted from the graph.
 *
 * @version 1.7
 */
public class LineMergeGraph extends PlanarGraph 
{
  /**
   * Adds an Edge, DirectedEdges, and Nodes for the given LineString representation
   * of an edge. 
   * Empty lines or lines with all coordinates equal are not added.
   * 
   * @param lineString the linestring to add to the graph
   */
  public void addEdge(LineString lineString) {
    if (lineString.isEmpty()) { return; }
    
    Coordinate[] coordinates = CoordinateArrays.removeRepeatedPoints(lineString.getCoordinates());
    
    // don't add lines with all coordinates equal
    if (coordinates.length <= 1) return;
    
    Coordinate startCoordinate = coordinates[0];
    Coordinate endCoordinate = coordinates[coordinates.length - 1];
    Node startNode = getNode(startCoordinate);
    Node endNode = getNode(endCoordinate);
    DirectedEdge directedEdge0 = new LineMergeDirectedEdge(startNode, endNode,
        coordinates[1], true);
    DirectedEdge directedEdge1 = new LineMergeDirectedEdge(endNode, startNode,
        coordinates[coordinates.length - 2], false);
    Edge edge = new LineMergeEdge(lineString);
    edge.setDirectedEdges(directedEdge0, directedEdge1);
    add(edge);
  }

  private Node getNode(Coordinate coordinate) {
    Node node = findNode(coordinate);
    if (node == null) {
      node = new Node(coordinate);
      add(node);
    }

    return node;
  }
}
