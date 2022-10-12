package org.locationtech.jts.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;

import test.jts.GeometryTestCase;

class OverlayNGTestCase extends GeometryTestCase {
  
  protected OverlayNGTestCase(String name) {
    super(name);
  }

  protected void checkIntersection(String wktA, String wktB, String wktExpected) {
    checkOverlay(wktA, wktB, INTERSECTION, wktExpected);
  }
  
  protected void checkUnion(String wktA, String wktB, String wktExpected) {
    checkOverlay(wktA, wktB, UNION, wktExpected);
  }
  
  protected void checkOverlay(String wktA, String wktB, int overlayOp, String wktExpected) {
    Geometry a = read(wktA);
    Geometry b = read(wktB);
    PrecisionModel pm = new PrecisionModel();
    Geometry actual = OverlayNG.overlay(a, b, overlayOp, pm);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
}
