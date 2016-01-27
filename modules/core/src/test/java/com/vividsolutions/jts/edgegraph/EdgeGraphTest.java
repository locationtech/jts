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

package com.vividsolutions.jts.edgegraph;

import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import test.jts.junit.GeometryUtils;

import com.vividsolutions.jts.edgegraph.EdgeGraph;
import com.vividsolutions.jts.edgegraph.EdgeGraphBuilder;
import com.vividsolutions.jts.edgegraph.HalfEdge;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.ParseException;

public class EdgeGraphTest extends TestCase {
  public static void main(String args[]) {
    TestRunner.run(EdgeGraphTest.class);
  }


  public EdgeGraphTest(String name) { super(name); }

  public void testNode() throws Exception
  {
    EdgeGraph graph = build("MULTILINESTRING((0 0, 1 0), (0 0, 0 1), (0 0, -1 0))");
    checkEdgeRing(graph, new Coordinate(0, 0), 
        new Coordinate[] { new Coordinate(1, 0),
      new Coordinate(0, 1), new Coordinate(-1, 0)
        });
    checkEdge(graph, new Coordinate(0, 0), new Coordinate(1, 0));
  }

  private void checkEdgeRing(EdgeGraph graph, Coordinate p,
      Coordinate[] dest) {
    HalfEdge e = graph.findEdge(p, dest[0]);
    HalfEdge onext = e;
    int i = 0;
    do {
      assertTrue(onext.dest().equals2D(dest[i++]));
      onext = onext.oNext();
    } while (onext != e);
   
  }


  private void checkEdge(EdgeGraph graph, Coordinate p0, Coordinate p1) {
    HalfEdge e = graph.findEdge(p0, p1);
    assertNotNull(e);
  }


  private EdgeGraph build(String wkt) throws ParseException {
    return build(new String[] { wkt });
  }

  private EdgeGraph build(String[] wkt) throws ParseException {
    List geoms = GeometryUtils.readWKT(wkt);
    return EdgeGraphBuilder.build(geoms);
  }

}
