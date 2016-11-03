
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
package org.locationtech.jts.operation.relate;

/**
 * @version 1.7
 */
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geomgraph.Edge;
import org.locationtech.jts.geomgraph.EdgeEnd;
import org.locationtech.jts.geomgraph.EdgeIntersection;
import org.locationtech.jts.geomgraph.GeometryGraph;
import org.locationtech.jts.geomgraph.Node;
import org.locationtech.jts.geomgraph.NodeMap;

/**
 * Implements the simple graph of Nodes and EdgeEnd which is all that is
 * required to determine topological relationships between Geometries.
 * Also supports building a topological graph of a single Geometry, to
 * allow verification of valid topology.
 * <p>
 * It is <b>not</b> necessary to create a fully linked
 * PlanarGraph to determine relationships, since it is sufficient
 * to know how the Geometries interact locally around the nodes.
 * In fact, this is not even feasible, since it is not possible to compute
 * exact intersection points, and hence the topology around those nodes
 * cannot be computed robustly.
 * The only Nodes that are created are for improper intersections;
 * that is, nodes which occur at existing vertices of the Geometries.
 * Proper intersections (e.g. ones which occur between the interior of line segments)
 * have their topology determined implicitly, without creating a Node object
 * to represent them.
 *
 * @version 1.7
 */
public class RelateNodeGraph {

  private NodeMap nodes = new NodeMap(new RelateNodeFactory());

  public RelateNodeGraph() {
  }

  public Iterator getNodeIterator() { return nodes.iterator(); }

  public void build(GeometryGraph geomGraph)
  {
      // compute nodes for intersections between previously noded edges
    computeIntersectionNodes(geomGraph, 0);
    /**
     * Copy the labelling for the nodes in the parent Geometry.  These override
     * any labels determined by intersections.
     */
    copyNodesAndLabels(geomGraph, 0);

    /**
     * Build EdgeEnds for all intersections.
     */
    EdgeEndBuilder eeBuilder = new EdgeEndBuilder();
    List eeList = eeBuilder.computeEdgeEnds(geomGraph.getEdgeIterator());
    insertEdgeEnds(eeList);

//Debug.println("==== NodeList ===");
//Debug.print(nodes);
  }

  /**
   * Insert nodes for all intersections on the edges of a Geometry.
   * Label the created nodes the same as the edge label if they do not already have a label.
   * This allows nodes created by either self-intersections or
   * mutual intersections to be labelled.
   * Endpoint nodes will already be labelled from when they were inserted.
   * <p>
   * Precondition: edge intersections have been computed.
   */
  public void computeIntersectionNodes(GeometryGraph geomGraph, int argIndex)
  {
    for (Iterator edgeIt = geomGraph.getEdgeIterator(); edgeIt.hasNext(); ) {
      Edge e = (Edge) edgeIt.next();
      int eLoc = e.getLabel().getLocation(argIndex);
      for (Iterator eiIt = e.getEdgeIntersectionList().iterator(); eiIt.hasNext(); ) {
        EdgeIntersection ei = (EdgeIntersection) eiIt.next();
        RelateNode n = (RelateNode) nodes.addNode(ei.coord);
        if (eLoc == Location.BOUNDARY)
          n.setLabelBoundary(argIndex);
        else {
          if (n.getLabel().isNull(argIndex))
            n.setLabel(argIndex, Location.INTERIOR);
        }
//Debug.println(n);
      }
    }
  }

    /**
     * Copy all nodes from an arg geometry into this graph.
     * The node label in the arg geometry overrides any previously computed
     * label for that argIndex.
     * (E.g. a node may be an intersection node with
     * a computed label of BOUNDARY,
     * but in the original arg Geometry it is actually
     * in the interior due to the Boundary Determination Rule)
     */
  public void copyNodesAndLabels(GeometryGraph geomGraph, int argIndex)
  {
    for (Iterator nodeIt = geomGraph.getNodeIterator(); nodeIt.hasNext(); ) {
      Node graphNode = (Node) nodeIt.next();
      Node newNode = nodes.addNode(graphNode.getCoordinate());
      newNode.setLabel(argIndex, graphNode.getLabel().getLocation(argIndex));
//node.print(System.out);
    }
  }

  public void insertEdgeEnds(List ee)
  {
    for (Iterator i = ee.iterator(); i.hasNext(); ) {
      EdgeEnd e = (EdgeEnd) i.next();
      nodes.add(e);
    }
  }


}
