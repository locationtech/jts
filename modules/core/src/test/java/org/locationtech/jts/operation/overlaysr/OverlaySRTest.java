package org.locationtech.jts.operation.overlaysr;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.overlay.OverlayOp;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlaySRTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(OverlaySRTest.class);
  }

  public OverlaySRTest(String name) { super(name); }
  
  public void testIntersectionSmoke() {
    Geometry a = read("POLYGON ((0 6, 4 6, 4 2, 0 2, 0 6))");
    Geometry b = read("POLYGON ((1 0, 2 5, 3 0, 1 0))");
    Geometry expected = read("POLYGON ((3 2, 1 2, 2 5, 3 2))");
    Geometry actual = intersection(a, b, 1);
    
    checkEqual(expected, actual);
    
  }
  
  public static Geometry union(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlaySR.overlayOp(a, b, pm, OverlayOp.UNION);
  }
  
  public static Geometry intersection(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlaySR.overlayOp(a, b, pm, OverlayOp.INTERSECTION);
  }
}
