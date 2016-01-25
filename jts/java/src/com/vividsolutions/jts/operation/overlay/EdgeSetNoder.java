

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
package com.vividsolutions.jts.operation.overlay;

import java.util.*;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.geomgraph.*;
import com.vividsolutions.jts.geomgraph.index.*;
import com.vividsolutions.jts.util.*;

/**
 * Nodes a set of edges.
 * Takes one or more sets of edges and constructs a
 * new set of edges consisting of all the split edges created by
 * noding the input edges together
 * @version 1.7
 */
public class EdgeSetNoder {

  private LineIntersector li;
  private List inputEdges = new ArrayList();

  public EdgeSetNoder(LineIntersector li) {
    this.li = li;
  }

  public void addEdges(List edges)
  {
    inputEdges.addAll(edges);
  }

  public List getNodedEdges()
  {
    EdgeSetIntersector esi = new SimpleMCSweepLineIntersector();
    SegmentIntersector si = new SegmentIntersector(li, true, false);
    esi.computeIntersections(inputEdges, si, true);
//Debug.println("has proper int = " + si.hasProperIntersection());

    List splitEdges = new ArrayList();
    for (Iterator i = inputEdges.iterator(); i.hasNext(); ) {
      Edge e = (Edge) i.next();
      e.getEdgeIntersectionList().addSplitEdges(splitEdges);
    }
    return splitEdges;
  }
}
