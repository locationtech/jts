/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.Collection;

import org.locationtech.jts.edgegraph.HalfEdge;
import org.locationtech.jts.geom.Coordinate;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlayGraphTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(OverlayGraphTest.class);
  }

  public OverlayGraphTest(String name) { super(name); }
  
  
  public void testTriangle() {
    
    Coordinate[] line1 = createLine(0, 0, 10, 10);
    Coordinate[] line2 = createLine(10, 10, 0, 10);
    Coordinate[] line3 = createLine(0, 10, 0, 0);
    
    OverlayGraph graph = createGraph(line1, line2, line3);
    
    OverlayEdge e1 = findEdge( graph, 0, 0, 10, 10);
    OverlayEdge e2 = findEdge( graph, 10, 10, 0, 10);
    OverlayEdge e3 = findEdge( graph, 0, 10, 0, 0);
        
    checkNodeValid( e1 );
    checkNodeValid( e2 );
    checkNodeValid( e3 );
    
    checkNext( e1, e2 );
    checkNext( e2, e3 );
    checkNext( e3, e1 );
    
    OverlayEdge e1sym = findEdge( graph, 10, 10, 0, 0 );
    OverlayEdge e2sym = findEdge( graph, 0, 10, 10, 10 );
    OverlayEdge e3sym = findEdge( graph, 0, 0, 0, 10 );
    
    assertEquals(e1sym, e1.sym());
    assertEquals(e2sym, e2.sym());
    assertEquals(e3sym, e3.sym());
    
    checkNext( e1sym, e3sym );
    checkNext( e2sym, e1sym );
    checkNext( e3sym, e2sym );
  }
  
  public void testStar() {
    
    OverlayGraph graph = new OverlayGraph();
    
    OverlayEdge e1 = addEdge( graph, 5, 5, 0, 0);
    OverlayEdge e2 = addEdge( graph, 5, 5, 0, 9);
    OverlayEdge e3 = addEdge( graph, 5, 5, 9, 9);
        
    checkNodeValid( e1 );
    
    checkNext( e1, e1.symOE() );
    checkNext( e2, e2.symOE() );
    checkNext( e3, e3.symOE() );
    
    checkPrev( e1, e2.symOE() );
    checkPrev( e2, e3.symOE() );
    checkPrev( e3, e1.symOE() );
  }
  
  /**
   * This test produced an error using the old HalfEdge sorting algorithm
   * (in {@link HalfEdge#insert(HalfEdge)}).
   */
  public void testCCWAfterInserts() {
    Coordinate[] e1 = createLine(50, 39, 35, 42, 37, 30);
    Coordinate[] e2 = createLine(50, 39, 50, 60, 20, 60);
    Coordinate[] e3 = createLine(50, 39, 68, 35);
 
    OverlayGraph graph = createGraph(e1, e2, e3);
    OverlayEdge node = graph.getNodeEdge(new Coordinate(50, 39));
    checkNodeValid(node);
  }

  public void testCCWAfterInserts2() {
    Coordinate[] e1 = createLine(50, 200, 0, 200);
    Coordinate[] e2 = createLine(50, 200, 190, 50, 50, 50);
    Coordinate[] e3 = createLine(50, 200, 200, 200, 0, 200);
    
    OverlayGraph graph = createGraph(e1, e2, e3);
    OverlayEdge node = graph.getNodeEdge(new Coordinate(50, 200));
    checkNodeValid(node);
  }
  
  private void checkNext(OverlayEdge e, OverlayEdge eNext) {
    assertEquals(eNext, e.next());
  }
  
  private void checkPrev(OverlayEdge e, OverlayEdge ePrev) {
    assertEquals(ePrev, e.prev());
  }
  
  private void checkNodeValid(OverlayEdge e) {
    boolean isNodeValid = e.isEdgesSorted();
    assertTrue("Found non-sorted edges around node " + e.toStringNode(), isNodeValid); 
  }

  private static OverlayEdge findEdge(OverlayGraph graph, double orgx, double orgy, double destx, double desty) {
    Collection<OverlayEdge> edges = graph.getEdges();
    for (OverlayEdge e : edges) {
      if (isEdgeOrgDest(e, orgx, orgy, destx, desty)) {
        return e;
      }
      if (isEdgeOrgDest(e.symOE(), orgx, orgy, destx, desty)) {
        return e.symOE();
      }
    }
    return null;
  }
  
  private static boolean isEdgeOrgDest(OverlayEdge e, double orgx, double orgy, double destx, double desty) {
    if (! isEqual(e.orig(), orgx, orgy)) return false;
    if (! isEqual(e.dest(), destx, desty)) return false;
    return true;
  }

  private static boolean isEqual(Coordinate p, double x, double y) {
    return p.getX() == x && p.getY() == y;
  }

  private OverlayGraph createGraph(Coordinate[]... edges) {
    OverlayGraph graph = new OverlayGraph();
    for (Coordinate[] e : edges) {
      graph.addEdge(e, new OverlayLabel());
    }
    return graph;
  }
  
  private OverlayEdge addEdge(OverlayGraph graph, double x1, double y1, double x2, double y2) {
    Coordinate[] pts = new Coordinate[] {
        new Coordinate(x1, y1), new Coordinate(x2, y2)
    };
    return graph.addEdge(pts, new OverlayLabel());
  }
  
  private Coordinate[] createLine(double... ord) {
    Coordinate[] pts = toCoordinates(ord);
    return pts;
  }

  private Coordinate[] toCoordinates(double[] ord) {
    Coordinate[] pts = new Coordinate[ord.length / 2];
    for (int i = 0; i < pts.length; i++) {
      pts[i] = new Coordinate(ord[2*i], ord[2*i+1]);
    }
    return pts;
  }
}
;