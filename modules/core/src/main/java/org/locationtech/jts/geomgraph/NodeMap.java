


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
package org.locationtech.jts.geomgraph;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Location;

/**
 * A map of nodes, indexed by the coordinate of the node
 * @version 1.7
 */
public class NodeMap

{
  //Map nodeMap = new HashMap();
  Map nodeMap = new TreeMap();
  NodeFactory nodeFact;

  public NodeMap(NodeFactory nodeFact) {
    this.nodeFact = nodeFact;
  }

  /**
   * Factory function - subclasses can override to create their own types of nodes
   */
   /*
  protected Node createNode(Coordinate coord)
  {
    return new Node(coord);
  }
  */
  /**
   * This method expects that a node has a coordinate value.
   */
  public Node addNode(Coordinate coord)
  {
    Node node = (Node) nodeMap.get(coord);
    if (node == null) {
      node = nodeFact.createNode(coord);
      nodeMap.put(coord, node);
    }
    return node;
  }

  public Node addNode(Node n)
  {
    Node node = (Node) nodeMap.get(n.getCoordinate());
    if (node == null) {
      nodeMap.put(n.getCoordinate(), n);
      return n;
    }
    node.mergeLabel(n);
    return node;
  }

  /**
   * Adds a node for the start point of this EdgeEnd
   * (if one does not already exist in this map).
   * Adds the EdgeEnd to the (possibly new) node.
   */
  public void add(EdgeEnd e)
  {
    Coordinate p = e.getCoordinate();
    Node n = addNode(p);
    n.add(e);
  }
  /**
   * @return the node if found; null otherwise
   */
  public Node find(Coordinate coord)  {    return (Node) nodeMap.get(coord);  }

  public Iterator iterator()
  {
    return nodeMap.values().iterator();
  }
  public Collection values()
  {
    return nodeMap.values();
  }

  public Collection getBoundaryNodes(int geomIndex)
  {
    Collection bdyNodes = new ArrayList();
    for (Iterator i = iterator(); i.hasNext(); ) {
      Node node = (Node) i.next();
      if (node.getLabel().getLocation(geomIndex) == Location.BOUNDARY)
        bdyNodes.add(node);
    }
    return bdyNodes;
  }

  public void print(PrintStream out)
  {
    for (Iterator it = iterator(); it.hasNext(); )
    {
      Node n = (Node) it.next();
      n.print(out);
    }
  }
}
