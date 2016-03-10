
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
package org.locationtech.jts.operation.valid;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geomgraph.GeometryGraph;
import org.locationtech.jts.index.sweepline.SweepLineIndex;
import org.locationtech.jts.index.sweepline.SweepLineInterval;
import org.locationtech.jts.index.sweepline.SweepLineOverlapAction;
import org.locationtech.jts.util.Assert;

/**
 * Tests whether any of a set of {@link LinearRing}s are
 * nested inside another ring in the set, using a {@link SweepLineIndex}
 * index to speed up the comparisons.
 *
 * @version 1.7
 */
public class SweeplineNestedRingTester
{

  private GeometryGraph graph;  // used to find non-node vertices
  private List rings = new ArrayList();
  //private Envelope totalEnv = new Envelope();
  private SweepLineIndex sweepLine;
  private Coordinate nestedPt = null;

  public SweeplineNestedRingTester(GeometryGraph graph)
  {
    this.graph = graph;
  }

  public Coordinate getNestedPoint() { return nestedPt; }

  public void add(LinearRing ring)
  {
    rings.add(ring);
  }

  public boolean isNonNested()
  {
    buildIndex();

    OverlapAction action = new OverlapAction();

    sweepLine.computeOverlaps(action);
    return action.isNonNested;
  }

  private void buildIndex()
  {
    sweepLine = new SweepLineIndex();

    for (int i = 0; i < rings.size(); i++) {
      LinearRing ring = (LinearRing) rings.get(i);
      Envelope env = ring.getEnvelopeInternal();
      SweepLineInterval sweepInt = new SweepLineInterval(env.getMinX(), env.getMaxX(), ring);
      sweepLine.add(sweepInt);
    }
  }

  private boolean isInside(LinearRing innerRing, LinearRing searchRing)
  {
    Coordinate[] innerRingPts = innerRing.getCoordinates();
    Coordinate[] searchRingPts = searchRing.getCoordinates();

    if (! innerRing.getEnvelopeInternal().intersects(searchRing.getEnvelopeInternal()))
      return false;

    Coordinate innerRingPt = IsValidOp.findPtNotNode(innerRingPts, searchRing, graph);
    Assert.isTrue(innerRingPt != null, "Unable to find a ring point not a node of the search ring");

    boolean isInside = CGAlgorithms.isPointInRing(innerRingPt, searchRingPts);
    if (isInside) {
      nestedPt = innerRingPt;
      return true;
    }
    return false;
  }


  class OverlapAction
    implements SweepLineOverlapAction
  {
    boolean isNonNested = true;

  public void overlap(SweepLineInterval s0, SweepLineInterval s1)
  {
    LinearRing innerRing = (LinearRing) s0.getItem();
    LinearRing searchRing = (LinearRing) s1.getItem();
    if (innerRing == searchRing) return;

    if (isInside(innerRing, searchRing))
      isNonNested = false;
  }

  }
}
