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

package org.locationtech.jts.offsetcurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.locationtech.jts.edgegraph.HalfEdge;
import org.locationtech.jts.edgegraph.MarkHalfEdge;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryComponentFilter;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;



/**
 * Dissolves the linear components 
 * from a collection of {@link Geometry}s
 * into a set of maximal-length {@link Linestring}s
 * in which every unique segment appears once only.
 * The output linestrings run between node vertices
 * of the input, which are vertices which have
 * either degree 1, or degree 3 or greater.
 * <p>
 * Use cases for dissolving linear components
 * include generalization 
 * (in particular, simplifying polygonal coverages), 
 * and visualization 
 * (in particular, avoiding symbology conflicts when
 * depicting shared polygon boundaries).
 * <p>
 * This class does <b>not</b> node the input lines.
 * If there are line segments crossing in the input, 
 * they will still cross in the output.
 * 
 * @author Martin Davis
 *
 */
public class ShortestPath 
{
  /**
   * Finds the shortest path through a Linear geometry
   * from start to end.
   * 
   */
  public static Geometry findPath(Geometry g, Coordinate start, Coordinate end)
  {
    ShortestPath d = new ShortestPath();
    d.add(g);
    return d.getResult(start, end);
  }
  
  private Geometry result;
  private GeometryFactory factory;
  
  //TODO: Is an EdgeGraph the most efficient structure for line sets which probably contain many long sequences of segments?
  private LineEdgeGraph graph;
  private List lines = new ArrayList();
  private Node startNode;
  private Node endNode;
  //private Set<Node> unvisited = new HashSet();
  private HashMap<Coordinate, Node> nodeMap = new HashMap<Coordinate, Node>();

  public ShortestPath()
  {
    graph = new LineEdgeGraph();
  }
  
  /**
   * Adds a {@link Geometry} to be dissolved. 
   * Any number of geometries may be added by calling this method multiple times.
   * Any type of Geometry may be added.  The constituent linework will be
   * extracted to be dissolved.
   * 
   * @param geometry geometry to be line-merged
   */  
  public void add(Geometry geometry) {
    geometry.apply(new GeometryComponentFilter() {
      public void filter(Geometry component) {
        if (component instanceof LineString) {
          add((LineString)component);
        }
      }      
    });
  }
  /**
   * Adds a collection of Geometries to be processed. May be called multiple times.
   * Any dimension of Geometry may be added; the constituent linework will be
   * extracted.
   * 
   * @param geometries the geometries to be line-merged
   */
  public void add(Collection geometries) 
  {
    for (Iterator i = geometries.iterator(); i.hasNext(); ) {
      Geometry geometry = (Geometry) i.next();
      add(geometry);
    }
  }
  
  //TODO: factor out the code to convert Linestrings to an EdgeGraph, since it's quite common
  private void add(LineString lineString) {
    if (factory == null) {
      this.factory = lineString.getFactory();
    }
    CoordinateSequence seq = lineString.getCoordinateSequence();
    boolean doneStart = false;
    for (int i = 1; i < seq.size(); i++) {
      HalfEdge e =  graph.addEdge(seq.getCoordinate(i-1), seq.getCoordinate(i));
      // skip zero-length edges
      if (e == null) continue;
    }
  }
  
  /**
   * Gets the dissolved result as a MultiLineString.
   * 
   * @return the dissolved lines
   */
  public Geometry getResult(Coordinate start, Coordinate end)
  {
    if (result == null)
      computeResult(start, end);
    return result;
  }

  private void computeResult(Coordinate start, Coordinate end) {
    
    buildNodes(start, end);
    findShortestPath();
    List<Node> path = tracePath();
    result = buildLine(path);
  }

  /**
   * Extract shortest path by backtracing shortest link pointers on nodes
   * @return
   */
  private List<Node> tracePath() {
    List<Node> path = new ArrayList<Node>();
    Node node = endNode;
    path.add(endNode);
    while (node != startNode) {
      node = node.getNearestOnPath();
      path.add(node);
    }
    return path;
  }

  private Geometry buildLine(List<Node> path) {
    CoordinateList coords = new CoordinateList();
    for (Node n : path) {
      coords.add(n.getCoordinate(), false);
    }
    return factory.createLineString(coords.toCoordinateArray());
  }

  private List<Node> findShortestPath() {
    
    List<Node> path = new ArrayList<Node>();
    Set<Node> uncommitted = new HashSet<Node>();
    uncommitted.addAll(nodeMap.values());

    Node currentNode = startNode;
    
    boolean done = false;
    while (! done) {
      currentNode.setMark(true);
      uncommitted.remove(currentNode);
      updateNeighbours(currentNode, uncommitted);
      //uncommitted.addAll(neighbours);
      
      // TODO: linear search - speed up with priority queue?
      currentNode = nearest(uncommitted);
      if (currentNode == null) {
        break;
      }
      if (currentNode.getCoordinate().equals2D(endNode.getCoordinate())) {
        break;
      }
      // TODO: check if nearest in front has distance = infinity?  Means graph is disconnected
    }
    return path;
  }

  private void updateNeighbours(Node currentNode, Set<Node> uncommitted) {
    //List<Node> neighbours = new ArrayList<Node>();
    HalfEdge start = currentNode.edge();
    double distCurr = currentNode.getDistance();
    HalfEdge next = start;
    do {
      Node n = findNode(next.dest());
      if (uncommitted.contains(n)) { 
        double distToNodeFromCurr = currentNode.getCoordinate().distance(n.getCoordinate());
        double dist = distToNodeFromCurr + distCurr;
        if (dist < n.getDistance()) {
          n.setDistance(dist);
          n.setNearestOnPath(currentNode);
        }
        //neighbours.add(n);
      }
      next = next.oNext();
    } while (next != start);
    //return neighbours;
    
  }

  private Node findNode(Coordinate dest) {
    return nodeMap.get(dest);
  }

  private Node nearest(Collection<Node> nodes) {
    Node nearest = null;
    for (Node n : nodes) {
      if (nearest == null || n.getDistance() < nearest.getDistance()) {
        nearest = n;
      }
    }
    return nearest;
  }

  private HashMap<Coordinate, Node> buildNodes(Coordinate start, Coordinate end) {
    Collection edges = graph.getVertexEdges();
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      HalfEdge e = (HalfEdge) i.next();
      Node node = new Node(e);
      node.setDistance(Double.POSITIVE_INFINITY);
      nodeMap.put(node.getCoordinate(), node);
      if (node.getCoordinate().equals2D(start)) {
        startNode = node;
        node.setDistance(0);
      }
      else if (node.getCoordinate().equals2D(end)) {
        endNode = node;
      }
    }
    return nodeMap;
  }





}
