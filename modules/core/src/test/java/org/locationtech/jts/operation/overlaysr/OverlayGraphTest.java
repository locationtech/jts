package org.locationtech.jts.operation.overlaysr;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.util.Assert;

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
    OverlayGraph graph = new OverlayGraph();
    OverlayEdge e1 = addEdge(graph, 50, 39, 35, 42, 37, 30);
    addEdge(graph, 50, 39, 50, 60, 20, 60);
    addEdge(graph, 50, 39, 68, 35);
    checkCCW(e1);
  }

  private void checkCCW(OverlayEdge e) {
    boolean isCCW = e.isCCWAtOrigin();
    assertTrue(isCCW);
    //, "Found non-CCW edges around node at " + this.toStringNode()
  }

  private OverlayEdge addEdge(OverlayGraph graph, double... ord) {
    SegmentString ss = createSegmentString(toCoordinates(ord));
    return graph.addEdge(ss);
  }

  private SegmentString createSegmentString(Coordinate[] pts) {
    return new BasicSegmentString(pts, new OverlayLabel());
  }

  private Coordinate[] toCoordinates(double[] ord) {
    Coordinate[] pts = new Coordinate[ord.length / 2];
    for (int i = 0; i < pts.length; i++) {
      pts[i] = new Coordinate(ord[2*i], ord[2*i+1]);
    }
    return pts;
  }
}
