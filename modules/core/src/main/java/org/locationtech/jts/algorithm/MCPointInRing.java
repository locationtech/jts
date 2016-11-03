
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
package org.locationtech.jts.algorithm;

import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.index.bintree.Bintree;
import org.locationtech.jts.index.bintree.Interval;
import org.locationtech.jts.index.chain.MonotoneChain;
import org.locationtech.jts.index.chain.MonotoneChainBuilder;
import org.locationtech.jts.index.chain.MonotoneChainSelectAction;


/**
 * Implements {@link PointInRing}
 * using {@link MonotoneChain}s and a {@link Bintree} index to
 * increase performance.
 *
 * @version 1.7
 * 
 * @see IndexedPointInAreaLocator for more general functionality
 */
public class MCPointInRing   implements PointInRing {

  class MCSelecter extends MonotoneChainSelectAction
  {
    Coordinate p;

    public MCSelecter(Coordinate p)
    {
      this.p = p;
    }

    public void select(LineSegment ls)
    {
      testLineSegment(p, ls);
    }
  }

  private LinearRing ring;
  private Bintree tree;
  private int crossings = 0;  // number of segment/ray crossings

  public MCPointInRing(LinearRing ring)
  {
    this.ring = ring;
    buildIndex();
  }

  private void buildIndex()
  {
    //Envelope env = ring.getEnvelopeInternal();
    tree = new Bintree();

    Coordinate[] pts = CoordinateArrays.removeRepeatedPoints(ring.getCoordinates());
    List mcList = MonotoneChainBuilder.getChains(pts);

    for (int i = 0; i < mcList.size(); i++) {
      MonotoneChain mc = (MonotoneChain) mcList.get(i);
      Envelope mcEnv = mc.getEnvelope();
      interval.min = mcEnv.getMinY();
      interval.max = mcEnv.getMaxY();
      tree.insert(interval, mc);
    }
  }

  private Interval interval = new Interval();

  public boolean isInside(Coordinate pt)
  {
    crossings = 0;

    // test all segments intersected by ray from pt in positive x direction
    Envelope rayEnv = new Envelope(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, pt.y, pt.y);

    interval.min = pt.y;
    interval.max = pt.y;
    List segs = tree.query(interval);
//System.out.println("query size = " + segs.size());

    MCSelecter mcSelecter = new MCSelecter(pt);
    for (Iterator i = segs.iterator(); i.hasNext(); ) {
      MonotoneChain mc = (MonotoneChain) i.next();
      testMonotoneChain(rayEnv, mcSelecter, mc);
    }

    /*
     *  p is inside if number of crossings is odd.
     */
    if ((crossings % 2) == 1) {
      return true;
    }
    return false;
  }


  private void testMonotoneChain(Envelope rayEnv, MCSelecter mcSelecter, MonotoneChain mc)
  {
    mc.select(rayEnv, mcSelecter);
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
