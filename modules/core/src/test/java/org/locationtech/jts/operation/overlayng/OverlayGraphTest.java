/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.operation.overlayng.Edge;
import org.locationtech.jts.operation.overlayng.HalfEdge;
import org.locationtech.jts.operation.overlayng.OverlayEdge;
import org.locationtech.jts.operation.overlayng.OverlayGraph;
import org.locationtech.jts.operation.overlayng.OverlayLabel;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlayGraphTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(OverlayGraphTest.class);
  }

  public OverlayGraphTest(String name) { super(name); }
  
  /**
   * This test produced an error using the old HalfEdge sorting algorithm
   * (in {@link HalfEdge#insert(HalfEdge)}).
   */
  public void testCCWAfterInserts() {
    Edge e1 = createEdge(50, 39, 35, 42, 37, 30);
    Edge e2 = createEdge(50, 39, 50, 60, 20, 60);
    Edge e3 = createEdge(50, 39, 68, 35);
 
    OverlayGraph graph = new OverlayGraph(createEdgeList(e1, e2, e3));
    OverlayEdge node = graph.getNodeEdge(new Coordinate(50, 39));
    checkNodeValid(node);
  }



  public void testCCWAfterInserts2() {
    Edge e1 = createEdge(50, 200, 0, 200);
    Edge e2 = createEdge(50, 200, 190, 50, 50, 50);
    Edge e3 = createEdge(50, 200, 200, 200, 0, 200);
    
    OverlayGraph graph = new OverlayGraph(createEdgeList(e1, e2, e3));
    OverlayEdge node = graph.getNodeEdge(new Coordinate(50, 200));
    checkNodeValid(node);
  }
  
  private void checkNodeValid(OverlayEdge e) {
    boolean isNodeValid = e.isEdgesSorted();
    assertTrue("Found non-sorted edges around node " + e.toStringNode(), isNodeValid); 
  }

  private Collection<Edge> createEdgeList(Edge... edges) {
    List<Edge> edgeList = new ArrayList<Edge>();
    for (Edge e : edges) {
      edgeList.add(e);
    }
    return edgeList;
  }
  
  private Edge createEdge(double... ord) {
    Coordinate[] pts = toCoordinates(ord);
    return new Edge(pts, new OverlayLabel());
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