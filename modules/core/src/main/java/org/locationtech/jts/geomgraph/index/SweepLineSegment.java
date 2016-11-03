


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
package org.locationtech.jts.geomgraph.index;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geomgraph.Edge;


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
