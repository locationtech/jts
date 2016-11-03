
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;

/**
 * A node in a {@link PlanarGraph}is a location where 0 or more {@link Edge}s
 * meet. A node is connected to each of its incident Edges via an outgoing
 * DirectedEdge. Some clients using a <code>PlanarGraph</code> may want to
 * subclass <code>Node</code> to add their own application-specific
 * data and methods.
 *
 * @version 1.7
 */
public class Node
    extends GraphComponent
{
  /**
   * Returns all Edges that connect the two nodes (which are assumed to be different).
   */
  public static Collection getEdgesBetween(Node node0, Node node1)
  {
    List edges0 = DirectedEdge.toEdges(node0.getOutEdges().getEdges());
    Set commonEdges = new HashSet(edges0);
    List edges1 = DirectedEdge.toEdges(node1.getOutEdges().getEdges());
    commonEdges.retainAll(edges1);
    return commonEdges;
  }

  /** The location of this Node */
  protected Coordinate pt;

  /** The collection of DirectedEdges that leave this Node */
  protected DirectedEdgeStar deStar;

  /**
   * Constructs a Node with the given location.
   */
  public Node(Coordinate pt)
  {
    this(pt, new DirectedEdgeStar());
  }

  /**
   * Constructs a Node with the given location and collection of outgoing DirectedEdges.
   */
  public Node(Coordinate pt, DirectedEdgeStar deStar)
  {
    this.pt = pt;
    this.deStar = deStar;
  }

  /**
   * Returns the location of this Node.
   */
  public Coordinate getCoordinate() { return pt; }

  /**
   * Adds an outgoing DirectedEdge to this Node.
   */
  public void addOutEdge(DirectedEdge de)
  {
    deStar.add(de);
  }

  /**
   * Returns the collection of DirectedEdges that leave this Node.
   */
  public DirectedEdgeStar getOutEdges() { return deStar; }
  /**
   * Returns the number of edges around this Node.
   */
  public int getDegree() { return deStar.getDegree(); }
  /**
   * Returns the zero-based index of the given Edge, after sorting in ascending order
   * by angle with the positive x-axis.
   */
  public int getIndex(Edge edge)
  {
    return deStar.getIndex(edge);
  }

  /**
   * Removes a {@link DirectedEdge} incident on this node.
   * Does not change the state of the directed edge.
   */
  public void remove(DirectedEdge de)
  {
    deStar.remove(de);
  }

  /**
   * Removes this node from its containing graph.
   */
  void remove() {
    pt = null;
  }


  /**
   * Tests whether this node has been removed from its containing graph
   *
   * @return <code>true</code> if this node is removed
   */
  public boolean isRemoved()
  {
    return pt == null;
  }

}
