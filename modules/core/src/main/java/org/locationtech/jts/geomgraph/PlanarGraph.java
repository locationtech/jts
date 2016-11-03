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

/**
 * @version 1.7
 */
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Location;

/**
 * The computation of the <code>IntersectionMatrix</code> relies on the use of a structure
 * called a "topology graph".  The topology graph contains nodes and edges
 * corresponding to the nodes and line segments of a <code>Geometry</code>. Each
 * node and edge in the graph is labeled with its topological location relative to
 * the source geometry.
 * <P>
 * Note that there is no requirement that points of self-intersection be a vertex.
 * Thus to obtain a correct topology graph, <code>Geometry</code>s must be
 * self-noded before constructing their graphs.
 * <P>
 * Two fundamental operations are supported by topology graphs:
 * <UL>
 *   <LI>Computing the intersections between all the edges and nodes of a single graph
 *   <LI>Computing the intersections between the edges and nodes of two different graphs
 * </UL>
 *
 * @version 1.7
 */
public class PlanarGraph
{
  /**
   * For nodes in the Collection, link the DirectedEdges at the node that are in the result.
   * This allows clients to link only a subset of nodes in the graph, for
   * efficiency (because they know that only a subset is of interest).
   */
  public static void linkResultDirectedEdges(Collection nodes)
  {
    for (Iterator nodeit = nodes.iterator(); nodeit.hasNext(); ) {
      Node node = (Node) nodeit.next();
      ((DirectedEdgeStar) node.getEdges()).linkResultDirectedEdges();
    }
  }

  protected List edges        = new ArrayList();
  protected NodeMap nodes;
  protected List edgeEndList  = new ArrayList();

  public PlanarGraph(NodeFactory nodeFact) {
    nodes = new NodeMap(nodeFact);
  }

  public PlanarGraph() {
    nodes = new NodeMap(new NodeFactory());
  }

  public Iterator getEdgeIterator() { return edges.iterator(); }
  public Collection getEdgeEnds() { return edgeEndList; }

  public boolean isBoundaryNode(int geomIndex, Coordinate coord)
  {
    Node node = nodes.find(coord);
    if (node == null) return false;
    Label label = node.getLabel();
    if (label != null && label.getLocation(geomIndex) == Location.BOUNDARY) return true;
    return false;
  }
  protected void insertEdge(Edge e)
  {
    edges.add(e);
  }
  public void add(EdgeEnd e)
  {
    nodes.add(e);
    edgeEndList.add(e);
  }

  public Iterator getNodeIterator() { return nodes.iterator(); }
  public Collection getNodes() { return nodes.values(); }
  public Node addNode(Node node) { return nodes.addNode(node); }
  public Node addNode(Coordinate coord) { return nodes.addNode(coord); }
  /**
   * @return the node if found; null otherwise
   */
  public Node find(Coordinate coord) { return nodes.find(coord); }

  /**
   * Add a set of edges to the graph.  For each edge two DirectedEdges
   * will be created.  DirectedEdges are NOT linked by this method.
   */
  public void addEdges(List edgesToAdd)
  {
    // create all the nodes for the edges
    for (Iterator it = edgesToAdd.iterator(); it.hasNext(); ) {
      Edge e = (Edge) it.next();
      edges.add(e);

      DirectedEdge de1 = new DirectedEdge(e, true);
      DirectedEdge de2 = new DirectedEdge(e, false);
      de1.setSym(de2);
      de2.setSym(de1);

      add(de1);
      add(de2);
    }
  }

  /**
   * Link the DirectedEdges at the nodes of the graph.
   * This allows clients to link only a subset of nodes in the graph, for
   * efficiency (because they know that only a subset is of interest).
   */
  public void linkResultDirectedEdges()
  {
    for (Iterator nodeit = nodes.iterator(); nodeit.hasNext(); ) {
      Node node = (Node) nodeit.next();
      ((DirectedEdgeStar) node.getEdges()).linkResultDirectedEdges();
    }
  }
  /**
   * Link the DirectedEdges at the nodes of the graph.
   * This allows clients to link only a subset of nodes in the graph, for
   * efficiency (because they know that only a subset is of interest).
   */
  public void linkAllDirectedEdges()
  {
    for (Iterator nodeit = nodes.iterator(); nodeit.hasNext(); ) {
      Node node = (Node) nodeit.next();
      ((DirectedEdgeStar) node.getEdges()).linkAllDirectedEdges();
    }
  }
  /**
   * Returns the EdgeEnd which has edge e as its base edge
   * (MD 18 Feb 2002 - this should return a pair of edges)
   *
   * @return the edge, if found
   *    <code>null</code> if the edge was not found
   */
  public EdgeEnd findEdgeEnd(Edge e)
  {
    for (Iterator i = getEdgeEnds().iterator(); i.hasNext(); ) {
      EdgeEnd ee = (EdgeEnd) i.next();
      if (ee.getEdge() == e)
        return ee;
    }
    return null;
  }

  /**
   * Returns the edge whose first two coordinates are p0 and p1
   *
   * @return the edge, if found
   *    <code>null</code> if the edge was not found
   */
  public Edge findEdge(Coordinate p0, Coordinate p1)
  {
    for (int i = 0; i < edges.size(); i++) {
      Edge e = (Edge) edges.get(i);
      Coordinate[] eCoord = e.getCoordinates();
      if (p0.equals(eCoord[0]) && p1.equals(eCoord[1]) )
        return e;
    }
    return null;
  }
  /**
   * Returns the edge which starts at p0 and whose first segment is
   * parallel to p1
   *
   * @return the edge, if found
   *    <code>null</code> if the edge was not found
   */
  public Edge findEdgeInSameDirection(Coordinate p0, Coordinate p1)
  {
    for (int i = 0; i < edges.size(); i++) {
      Edge e = (Edge) edges.get(i);

      Coordinate[] eCoord = e.getCoordinates();
      if (matchInSameDirection(p0, p1, eCoord[0], eCoord[1]) )
        return e;

      if (matchInSameDirection(p0, p1, eCoord[eCoord.length - 1], eCoord[eCoord.length - 2]) )
        return e;
    }
    return null;
  }

  /**
   * The coordinate pairs match if they define line segments lying in the same direction.
   * E.g. the segments are parallel and in the same quadrant
   * (as opposed to parallel and opposite!).
   */
  private boolean matchInSameDirection(Coordinate p0, Coordinate p1, Coordinate ep0, Coordinate ep1)
  {
    if (! p0.equals(ep0))
      return false;

    if (CGAlgorithms.computeOrientation(p0, p1, ep1) == CGAlgorithms.COLLINEAR
         && Quadrant.quadrant(p0, p1) == Quadrant.quadrant(ep0, ep1) )
      return true;
    return false;
  }

  public void printEdges(PrintStream out)
  {
    out.println("Edges:");
    for (int i = 0; i < edges.size(); i++) {
      out.println("edge " + i + ":");
      Edge e = (Edge) edges.get(i);
      e.print(out);
      e.eiList.print(out);
    }
  }
  void debugPrint(Object o)
  {
    System.out.print(o);
  }
  void debugPrintln(Object o)
  {
    System.out.println(o);
  }

}
