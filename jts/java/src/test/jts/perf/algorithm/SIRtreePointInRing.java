
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package test.jts.perf.algorithm;

import java.util.*;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.strtree.*;

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
