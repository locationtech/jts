/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.edgegraph;

import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.ParseException;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import test.jts.util.IOUtil;


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
    checkNodeValid(graph, new Coordinate(0, 0), new Coordinate(1, 0));
    checkEdge(graph, new Coordinate(0, 0), new Coordinate(1, 0));
    
    checkNextPrev(graph);
    
    checkNext(graph, 1, 0, 0, 0, 0, 1);
    checkNext(graph, 0, 1, 0, 0, -1, 0);
    checkNext(graph, -1, 0, 0, 0, 1, 0);
    
    checkNextPrev(graph, 1, 0, 0, 0);
    checkNextPrev(graph, 0, 1, 0, 0);
    checkNextPrev(graph, -1, 0, 0, 0);
    
    assertTrue( findEdge(graph, 0, 0, 1, 0).degree() == 3 );
  }

  public void testRingGraph() throws Exception {
    EdgeGraph graph = build("MULTILINESTRING ((10 10, 10 90), (10 90, 90 90), (90 90, 90 10), (90 10, 10 10))");
    HalfEdge e = findEdge(graph, 10, 10, 10, 90);
    HalfEdge eNext = findEdge(graph, 10, 90, 90, 90);
    assertTrue(e.next() == eNext);
    assertTrue(eNext.prev() == e);
    
    HalfEdge eSym = findEdge(graph, 10, 90, 10, 10);
    assertTrue(e.sym() == eSym);
    assertTrue(e.orig().equals2D(new Coordinate(10, 10)));
    assertTrue(e.dest().equals2D(new Coordinate(10, 90)));

    checkNextPrev(graph);
  }
  
  public void testSingleEdgeGraph() throws Exception {
    EdgeGraph graph = build("LINESTRING (10 10, 20 20)");    
    checkNextPrev(graph);
  }
  
  /**
   * This test produced an error using the original buggy sorting algorithm
   * (in {@link HalfEdge#insert(HalfEdge)}).
   */
  public void testCCWAfterInserts() {
    EdgeGraph graph = new EdgeGraph();
    HalfEdge e1 = addEdge(graph, 50, 39, 35, 42);
    addEdge(graph, 50, 39, 50, 60);
    addEdge(graph, 50, 39, 68, 35);
    checkNodeValid(e1);
  }

  public void testCCWAfterInserts2() {
    EdgeGraph graph = new EdgeGraph();
    HalfEdge e1 = addEdge(graph, 50, 200, 0, 200);
    addEdge(graph, 50, 200, 190, 50);
    addEdge(graph, 50, 200, 200, 200);
    checkNodeValid(e1);
  }

  //==================================================
  
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

  private void checkNodeValid(EdgeGraph graph, Coordinate p0, Coordinate p1) {
    HalfEdge e = graph.findEdge(p0, p1);
    boolean isNodeValid = e.isEdgesSorted();
    assertTrue("Found non-sorted edges around node " + e, isNodeValid); 
  }


  private void checkNodeValid(HalfEdge e) {
    boolean isNodeValid = e.isEdgesSorted();
    assertTrue("Found non-sorted edges around node " + e, isNodeValid); 
  }
  
  private void checkNextPrev(EdgeGraph graph) {
    Collection<HalfEdge> edges = graph.getVertexEdges();
    for (HalfEdge e: edges) {
      assertTrue(e.next().prev() == e);
    }
  }


 
  private void checkNext(EdgeGraph graph, double x1, double y1, double x2, double y2, double x3, double y3) {
    HalfEdge e1 = findEdge(graph, x1, y1, x2, y2);
    HalfEdge e2 = findEdge(graph, x2, y2, x3, y3);
    assertTrue(e1.next() == e2);
    assertTrue(e2.prev() == e1);
  }
  
  private void checkNextPrev(EdgeGraph graph, double x1, double y1, double x2, double y2) {
    HalfEdge e = findEdge(graph, x1, y1, x2, y2);
    assertTrue(e.next().prev() == e);
  }

  private HalfEdge findEdge(EdgeGraph graph, double x1, double y1, double x2, double y2) {
    return graph.findEdge(new Coordinate(x1, y1), new Coordinate(x2, y2));
  }
  
  private EdgeGraph build(String wkt) throws ParseException {
    return build(new String[] { wkt });
  }

  private EdgeGraph build(String[] wkt) throws ParseException {
    List geoms = IOUtil.readWKT(wkt);
    return EdgeGraphBuilder.build(geoms);
  }

  private HalfEdge addEdge(EdgeGraph graph, double p0x, double p0y, double p1x, double p1y) {
    return graph.addEdge(new Coordinate(p0x, p0y), new Coordinate(p1x, p1y));
  }

}
