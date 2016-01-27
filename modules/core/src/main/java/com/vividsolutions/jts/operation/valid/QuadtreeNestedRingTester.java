
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
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.util.*;

/**
 * Tests whether any of a set of {@link LinearRing}s are
 * nested inside another ring in the set, using a {@link Quadtree}
 * index to speed up the comparisons.
 *
 * @version 1.7
 */
public class QuadtreeNestedRingTester
{

  private GeometryGraph graph;  // used to find non-node vertices
  private List rings = new ArrayList();
  private Envelope totalEnv = new Envelope();
  private Quadtree quadtree;
  private Coordinate nestedPt;

  public QuadtreeNestedRingTester(GeometryGraph graph)
  {
    this.graph = graph;
  }

  public Coordinate getNestedPoint() { return nestedPt; }

  public void add(LinearRing ring)
  {
    rings.add(ring);
    totalEnv.expandToInclude(ring.getEnvelopeInternal());
  }

  public boolean isNonNested()
  {
    buildQuadtree();

    for (int i = 0; i < rings.size(); i++) {
      LinearRing innerRing = (LinearRing) rings.get(i);
      Coordinate[] innerRingPts = innerRing.getCoordinates();

      List results = quadtree.query(innerRing.getEnvelopeInternal());
//System.out.println(results.size());
      for (int j = 0; j < results.size(); j++) {
        LinearRing searchRing = (LinearRing) results.get(j);
        Coordinate[] searchRingPts = searchRing.getCoordinates();

        if (innerRing == searchRing)
          continue;

        if (! innerRing.getEnvelopeInternal().intersects(searchRing.getEnvelopeInternal()))
          continue;

        Coordinate innerRingPt = IsValidOp.findPtNotNode(innerRingPts, searchRing, graph);
        Assert.isTrue(innerRingPt != null, "Unable to find a ring point not a node of the search ring");
        //Coordinate innerRingPt = innerRingPts[0];

        boolean isInside = CGAlgorithms.isPointInRing(innerRingPt, searchRingPts);
        if (isInside) {
          nestedPt = innerRingPt;
          return false;
        }
      }
    }
    return true;
  }

  private void buildQuadtree()
  {
    quadtree = new Quadtree();

    for (int i = 0; i < rings.size(); i++) {
      LinearRing ring = (LinearRing) rings.get(i);
      Envelope env = ring.getEnvelopeInternal();
      quadtree.insert(env, ring);
    }
  }
}
