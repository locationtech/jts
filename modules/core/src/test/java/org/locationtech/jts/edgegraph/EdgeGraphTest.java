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

package org.locationtech.jts.edgegraph;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.ParseException;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import test.jts.junit.GeometryUtils;


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
