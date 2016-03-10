
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
package test.jts.perf.algorithm;

import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.PointInRing;
import org.locationtech.jts.algorithm.RobustDeterminant;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.index.strtree.SIRtree;

/**
 * Implements {@link PointInRing}
 * using a {@link SIRtree} index to
 * increase performance.
 *
 * @version 1.7
 * @deprecated use MCPointInRing instead
 */
public class SIRtreePointInRing implements PointInRing {

  private LinearRing ring;
  private SIRtree sirTree;
  private int crossings = 0;  // number of segment/ray crossings

  public SIRtreePointInRing(LinearRing ring)
  {
    this.ring = ring;
    buildIndex();
  }

  private void buildIndex()
  {
    Envelope env = ring.getEnvelopeInternal();
    sirTree = new SIRtree();

    Coordinate[] pts = ring.getCoordinates();
    for (int i = 1; i < pts.length; i++) {
      if (pts[i-1].equals(pts[i])) { continue; } //Optimization suggested by MD. [Jon Aquino]
      LineSegment seg = new LineSegment(pts[i - 1], pts[i]);
      sirTree.insert(seg.p0.y, seg.p1.y, seg);
    }
  }

  public boolean isInside(Coordinate pt)
  {
    crossings = 0;

    // test all segments intersected by vertical ray at pt

    List segs = sirTree.query(pt.y);
//System.out.println("query size = " + segs.size());

    for (Iterator i = segs.iterator(); i.hasNext(); ) {
      LineSegment seg = (LineSegment) i.next();
      testLineSegment(pt, seg);
    }

    /*
     *  p is inside if number of crossings is odd.
     */
    if ((crossings % 2) == 1) {
      return true;
    }
    return false;
  }

  private void testLineSegment(Coordinate p, LineSegment seg) {
    double xInt;  // x intersection of segment with ray
    double x1;    // translated coordinates
    double y1;
    double x2;
    double y2;

    /*
     *  Test if segment crosses ray from test point in positive x direction.
     */
    Coordinate p1 = seg.p0;
    Coordinate p2 = seg.p1;
    x1 = p1.x - p.x;
    y1 = p1.y - p.y;
    x2 = p2.x - p.x;
    y2 = p2.y - p.y;

    if (((y1 > 0) && (y2 <= 0)) ||
        ((y2 > 0) && (y1 <= 0))) {
        /*
         *  segment straddles x axis, so compute intersection.
         */
      xInt = RobustDeterminant.signOfDet2x2(x1, y1, x2, y2) / (y2 - y1);
        //xsave = xInt;
        /*
         *  crosses ray if strictly positive intersection.
         */
      if (0.0 < xInt) {
        crossings++;
      }
    }
  }
}
