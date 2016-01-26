


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
package com.vividsolutions.jts.geomgraph.index;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geomgraph.*;


/**
 * @version 1.7
 */
public class SweepLineSegment {

  Edge edge;
  Coordinate[] pts;
  int ptIndex;

  public SweepLineSegment(Edge edge,  int ptIndex) {
    this.edge = edge;
    this.ptIndex = ptIndex;
    pts = edge.getCoordinates();
  }

  public double getMinX()
  {
    double x1 = pts[ptIndex].x;
    double x2 = pts[ptIndex + 1].x;
    return x1 < x2 ? x1 : x2;
  }
  public double getMaxX()
  {
    double x1 = pts[ptIndex].x;
    double x2 = pts[ptIndex + 1].x;
    return x1 > x2 ? x1 : x2;
  }
  public void computeIntersections(SweepLineSegment ss, SegmentIntersector si)
  {
    si.addIntersections(edge, ptIndex, ss.edge, ss.ptIndex);
  }

}
