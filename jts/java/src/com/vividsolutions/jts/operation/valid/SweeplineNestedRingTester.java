
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
package com.vividsolutions.jts.operation.valid;

import java.util.*;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geomgraph.*;
import com.vividsolutions.jts.index.sweepline.*;
import com.vividsolutions.jts.util.*;

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
