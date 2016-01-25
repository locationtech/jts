


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
package com.vividsolutions.jts.geomgraph;

import java.io.PrintStream;
import java.util.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.noding.OrientedCoordinateArray;
import com.vividsolutions.jts.noding.SegmentString;

/**
 * A EdgeList is a list of Edges.  It supports locating edges
 * that are pointwise equals to a target edge.
 * @version 1.7
 */
public class EdgeList
{
  private List edges = new ArrayList();
  /**
   * An index of the edges, for fast lookup.
   *
   */
  private Map ocaMap = new TreeMap();

  public EdgeList() {
  }

  /**
   * Insert an edge unless it is already in the list
   */
  public void add(Edge e)
  {
    edges.add(e);
    OrientedCoordinateArray oca = new OrientedCoordinateArray(e.getCoordinates());
    ocaMap.put(oca, e);
  }

  public void addAll(Collection edgeColl)
  {
    for (Iterator i = edgeColl.iterator(); i.hasNext(); ) {
      add((Edge) i.next());
    }
  }

  public List getEdges() { return edges; }

  /**
   * If there is an edge equal to e already in the list, return it.
   * Otherwise return null.
   * @return  equal edge, if there is one already in the list
   *          null otherwise
   */
  public Edge findEqualEdge(Edge e)
  {
    OrientedCoordinateArray oca = new OrientedCoordinateArray(e.getCoordinates());
    // will return null if no edge matches
    Edge matchEdge = (Edge) ocaMap.get(oca);
    return matchEdge; 
  }
  
  public Iterator iterator() { return edges.iterator(); }

  public Edge get(int i) { return (Edge) edges.get(i); }

  /**
   * If the edge e is already in the list, return its index.
   * @return  index, if e is already in the list
   *          -1 otherwise
   */
  public int findEdgeIndex(Edge e)
  {
    for (int i = 0; i < edges.size(); i++) {
      if ( ((Edge) edges.get(i)).equals(e) ) return i;
    }
    return -1;
  }

  public void print(PrintStream out)
  {
    out.print("MULTILINESTRING ( ");
    for (int j = 0; j < edges.size(); j++) {
      Edge e = (Edge) edges.get(j);
      if (j > 0) out.print(",");
      out.print("(");
      Coordinate[] pts = e.getCoordinates();
      for (int i = 0; i < pts.length; i++) {
        if (i > 0) out.print(",");
        out.print(pts[i].x + " " + pts[i].y);
      }
      out.println(")");
    }
    out.print(")  ");
  }


}
