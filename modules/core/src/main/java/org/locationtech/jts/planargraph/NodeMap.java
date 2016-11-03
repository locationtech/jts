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

package org.locationtech.jts.planargraph;


import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.geom.Coordinate;


/**
 * A map of {@link Node}s, indexed by the coordinate of the node.
 *
 * @version 1.7
 */
public class NodeMap

{

  private Map nodeMap = new TreeMap();
  
  /**
   * Constructs a NodeMap without any Nodes.
   */
  public NodeMap() {
  }

  /**
   * Adds a node to the map, replacing any that is already at that location.
   * @return the added node
   */
  public Node add(Node n)
  {
    nodeMap.put(n.getCoordinate(), n);
    return n;
  }

  /**
   * Removes the Node at the given location, and returns it (or null if no Node was there).
   */
  public Node remove(Coordinate pt)
  {
    return (Node) nodeMap.remove(pt);
  }

  /**
   * Returns the Node at the given location, or null if no Node was there.
   */
  public Node find(Coordinate coord)  {    return (Node) nodeMap.get(coord);  }

  /**
   * Returns an Iterator over the Nodes in this NodeMap, sorted in ascending order
   * by angle with the positive x-axis.
   */
  public Iterator iterator()
  {
    return nodeMap.values().iterator();
  }
  /**
   * Returns the Nodes in this NodeMap, sorted in ascending order
   * by angle with the positive x-axis.
   */
  public Collection values()
  {
    return nodeMap.values();
  }

}
