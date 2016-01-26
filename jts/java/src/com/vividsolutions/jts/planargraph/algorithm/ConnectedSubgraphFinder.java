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

package com.vividsolutions.jts.planargraph.algorithm;

import java.util.*;
import com.vividsolutions.jts.planargraph.*;

/**
 * Finds all connected {@link Subgraph}s of a {@link PlanarGraph}.
 * <p>
 * <b>Note:</b> uses the <code>isVisited</code> flag on the nodes.
 */
public class ConnectedSubgraphFinder
{

  private PlanarGraph graph;

  public ConnectedSubgraphFinder(PlanarGraph graph) {
    this.graph = graph;
  }

  public List getConnectedSubgraphs()
  {
    List subgraphs = new ArrayList();

    GraphComponent.setVisited(graph.nodeIterator(), false);
    for (Iterator i = graph.edgeIterator(); i.hasNext(); ) {
      Edge e = (Edge) i.next();
      Node node = e.getDirEdge(0).getFromNode();
      if (! node.isVisited()) {
        subgraphs.add(findSubgraph(node));
      }
    }
    return subgraphs;
  }

  private Subgraph findSubgraph(Node node)
  {
    Subgraph subgraph = new Subgraph(graph);
    addReachable(node, subgraph);
    return subgraph;
  }

  /**
   * Adds all nodes and edges reachable from this node to the subgraph.
   * Uses an explicit stack to avoid a large depth of recursion.
   *
   * @param node a node known to be in the subgraph
   */
  private void addReachable(Node startNode, Subgraph subgraph)
  {
    Stack nodeStack = new Stack();
    nodeStack.add(startNode);
    while (! nodeStack.empty()) {
      Node node = (Node) nodeStack.pop();
      addEdges(node, nodeStack, subgraph);
    }
  }

  /**
   * Adds the argument node and all its out edges to the subgraph.
   * @param node the node to add
   * @param nodeStack the current set of nodes being traversed
   */
  private void addEdges(Node node, Stack nodeStack, Subgraph subgraph)
  {
    node.setVisited(true);
    for (Iterator i = ((DirectedEdgeStar) node.getOutEdges()).iterator(); i.hasNext(); ) {
      DirectedEdge de = (DirectedEdge) i.next();
      subgraph.add(de.getEdge());
      Node toNode = de.getToNode();
      if (! toNode.isVisited()) nodeStack.push(toNode);
    }
  }

}
