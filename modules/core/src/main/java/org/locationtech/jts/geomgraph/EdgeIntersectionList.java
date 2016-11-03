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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.geom.Coordinate;

/**
 * A list of edge intersections along an {@link Edge}.
 * Implements splitting an edge with intersections
 * into multiple resultant edges.
 *
 * @version 1.7
 */
public class EdgeIntersectionList
{
  // a Map <EdgeIntersection, EdgeIntersection>
  private Map nodeMap = new TreeMap();
  Edge edge;  // the parent edge

  public EdgeIntersectionList(Edge edge)
  {
    this.edge = edge;
  }

  /**
   * Adds an intersection into the list, if it isn't already there.
   * The input segmentIndex and dist are expected to be normalized.
   * @return the EdgeIntersection found or added
   */
  public EdgeIntersection add(Coordinate intPt, int segmentIndex, double dist)
  {
    EdgeIntersection eiNew = new EdgeIntersection(intPt, segmentIndex, dist);
    EdgeIntersection ei = (EdgeIntersection) nodeMap.get(eiNew);
    if (ei != null) {
      return ei;
    }
    nodeMap.put(eiNew, eiNew);
    return eiNew;
  }

  /**
   * Returns an iterator of {@link EdgeIntersection}s
   *
   * @return an Iterator of EdgeIntersections
   */
  public Iterator iterator() { return nodeMap.values().iterator(); }

  /**
   * Tests if the given point is an edge intersection
   *
   * @param pt the point to test
   * @return true if the point is an intersection
   */
  public boolean isIntersection(Coordinate pt)
  {
    for (Iterator it = iterator(); it.hasNext(); ) {
      EdgeIntersection ei = (EdgeIntersection) it.next();
      if (ei.coord.equals(pt))
       return true;
    }
    return false;
  }

  /**
   * Adds entries for the first and last points of the edge to the list
   */
  public void addEndpoints()
  {
    int maxSegIndex = edge.pts.length - 1;
    add(edge.pts[0], 0, 0.0);
    add(edge.pts[maxSegIndex], maxSegIndex, 0.0);
  }

  /**
   * Creates new edges for all the edges that the intersections in this
   * list split the parent edge into.
   * Adds the edges to the input list (this is so a single list
   * can be used to accumulate all split edges for a Geometry).
   *
   * @param edgeList a list of EdgeIntersections
   */
  public void addSplitEdges(List edgeList)
  {
    // ensure that the list has entries for the first and last point of the edge
    addEndpoints();

    Iterator it = iterator();
    // there should always be at least two entries in the list
    EdgeIntersection eiPrev = (EdgeIntersection) it.next();
    while (it.hasNext()) {
      EdgeIntersection ei = (EdgeIntersection) it.next();
      Edge newEdge = createSplitEdge(eiPrev, ei);
      edgeList.add(newEdge);

      eiPrev = ei;
    }
  }
  /**
   * Create a new "split edge" with the section of points between
   * (and including) the two intersections.
   * The label for the new edge is the same as the label for the parent edge.
   */
  Edge createSplitEdge(EdgeIntersection ei0, EdgeIntersection ei1)
  {
//Debug.print("\ncreateSplitEdge"); Debug.print(ei0); Debug.print(ei1);
    int npts = ei1.segmentIndex - ei0.segmentIndex + 2;

    Coordinate lastSegStartPt = edge.pts[ei1.segmentIndex];
    // if the last intersection point is not equal to the its segment start pt,
    // add it to the points list as well.
    // (This check is needed because the distance metric is not totally reliable!)
    // The check for point equality is 2D only - Z values are ignored
    boolean useIntPt1 = ei1.dist > 0.0 || ! ei1.coord.equals2D(lastSegStartPt);
    if (! useIntPt1) {
      npts--;
    }

    Coordinate[] pts = new Coordinate[npts];
    int ipt = 0;
    pts[ipt++] = new Coordinate(ei0.coord);
    for (int i = ei0.segmentIndex + 1; i <= ei1.segmentIndex; i++) {
      pts[ipt++] = edge.pts[i];
    }
    if (useIntPt1) pts[ipt] = ei1.coord;
    return new Edge(pts, new Label(edge.label));
  }

  public void print(PrintStream out)
  {
    out.println("Intersections:");
    for (Iterator it = iterator(); it.hasNext(); ) {
      EdgeIntersection ei = (EdgeIntersection) it.next();
      ei.print(out);
    }
  }
}
