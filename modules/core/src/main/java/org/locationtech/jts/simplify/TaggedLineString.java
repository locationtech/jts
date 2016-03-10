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

package org.locationtech.jts.simplify;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;

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
