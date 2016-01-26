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

package com.vividsolutions.jts.simplify;

import java.util.*;
import com.vividsolutions.jts.geom.*;

/**
 * Represents a {@link LineString} which can be modified to a simplified shape.  
 * This class provides an attribute which specifies the minimum allowable length
 * for the modified result.
 * 
 * @version 1.7
 */
class TaggedLineString
{

  private LineString parentLine;
  private TaggedLineSegment[] segs;
  private List resultSegs = new ArrayList();
  private int minimumSize;

  public TaggedLineString(LineString parentLine) {
    this(parentLine, 2);
  }

  public TaggedLineString(LineString parentLine, int minimumSize) {
    this.parentLine = parentLine;
    this.minimumSize = minimumSize;
    init();
  }

  public int getMinimumSize()  {    return minimumSize;  }
  public LineString getParent() { return parentLine; }
  public Coordinate[] getParentCoordinates() { return parentLine.getCoordinates(); }
  public Coordinate[] getResultCoordinates() { return extractCoordinates(resultSegs); }

  public int getResultSize()
  {
    int resultSegsSize = resultSegs.size();
    return resultSegsSize == 0 ? 0 : resultSegsSize + 1;
  }

  public TaggedLineSegment getSegment(int i) { return segs[i]; }

  private void init()
  {
    Coordinate[] pts = parentLine.getCoordinates();
    segs = new TaggedLineSegment[pts.length - 1];
    for (int i = 0; i < pts.length - 1; i++) {
      TaggedLineSegment seg
               = new TaggedLineSegment(pts[i], pts[i + 1], parentLine, i);
      segs[i] = seg;
    }
  }

  public TaggedLineSegment[] getSegments() { return segs; }

  public void addToResult(LineSegment seg)
  {
    resultSegs.add(seg);
  }

  public LineString asLineString()
  {
    return parentLine.getFactory().createLineString(extractCoordinates(resultSegs));
  }

  public LinearRing asLinearRing() {
    return parentLine.getFactory().createLinearRing(extractCoordinates(resultSegs));
  }

  private static Coordinate[] extractCoordinates(List segs)
  {
    Coordinate[] pts = new Coordinate[segs.size() + 1];
    LineSegment seg = null;
    for (int i = 0; i < segs.size(); i++) {
      seg = (LineSegment) segs.get(i);
      pts[i] = seg.p0;
    }
    // add last point
    pts[pts.length - 1] = seg.p1;
    return pts;
  }


}
