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
  
  public void xtestIntersectionSmoke() {
    Geometry a = read("POLYGON ((0 6, 4 6, 4 2, 0 2, 0 6))");
    Geometry b = read("POLYGON ((1 0, 2 5, 3 0, 1 0))");
    Geometry expected = read("POLYGON ((3 2, 1 2, 2 5, 3 2))");
    Geometry actual = intersection(a, b, 1);
    
    checkEqual(expected, actual);
    
  }
  
  public void xtestIntersection2spikes() {
    Geometry a = read("POLYGON ((0 100, 40 100, 40 0, 0 0, 0 100))");
    Geometry b = read("POLYGON ((70 80, 10 80, 60 50, 11 20, 69 11, 70 80))");
    Geometry expected = read("POLYGON ((3 2, 1 2, 2 5, 3 2))");
    Geometry actual = intersection(a, b, 1);
    
    checkEqual(expected, actual);
    
  }
  
  public void xtestUnion2spikes() {
    Geometry a = read("POLYGON ((0 100, 40 100, 40 0, 0 0, 0 100))");
    Geometry b = read("POLYGON ((70 80, 10 80, 60 50, 11 20, 69 11, 70 80))");
    Geometry expected = read("POLYGON ((0 100, 40 100, 40 80, 70 80, 69 11, 40 16, 40 0, 0 0, 0 100), (40 62, 40 38, 60 50, 40 62))");
    Geometry actual = union(a, b, 1);
    
    checkEqual(expected, actual);
    
  }
  
  public void testIntersectionTriBox() {
    Geometry a = read("POLYGON ((68 35, 35 42, 40 9, 68 35))");
    Geometry b = read("POLYGON ((20 60, 50 60, 50 30, 20 30, 20 60))");
    Geometry expected = read("POLYGON ((0 100, 40 100, 40 80, 70 80, 69 11, 40 16, 40 0, 0 0, 0 100), (40 62, 40 38, 60 50, 40 62))");
    Geometry actual = intersection(a, b, 1);
    
    checkEqual(expected, actual);
  }
  
  public void xtestUnionSmoke() {
    Geometry a = read("POLYGON ((0 6, 4 6, 4 2, 0 2, 0 6))");
    Geometry b = read("POLYGON ((1 0, 2 5, 3 0, 1 0))");
    Geometry expected = read("POLYGON ((3 2, 1 2, 2 5, 3 2))");
    Geometry actual = union(a, b, 1);
    
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
