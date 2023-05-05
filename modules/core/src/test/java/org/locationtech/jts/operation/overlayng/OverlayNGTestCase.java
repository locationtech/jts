package org.locationtech.jts.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNG.DIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.SYMDIFFERENCE;
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
  
  static Geometry difference(Geometry a, Geometry b) {
    PrecisionModel pm = new PrecisionModel();
    return OverlayNG.overlay(a, b, DIFFERENCE, pm);
  }
  
  static Geometry symDifference(Geometry a, Geometry b) {
    PrecisionModel pm = new PrecisionModel();
    return OverlayNG.overlay(a, b, SYMDIFFERENCE, pm);
  }
  
  static Geometry intersection(Geometry a, Geometry b) {
    PrecisionModel pm = new PrecisionModel();
    return OverlayNG.overlay(a, b, INTERSECTION, pm);
  }
  
  static Geometry union(Geometry a, Geometry b) {
    PrecisionModel pm = new PrecisionModel();
    return OverlayNG.overlay(a, b, UNION, pm);
  }
  
  public static Geometry difference(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, DIFFERENCE, pm);
  }
  
  public static Geometry symDifference(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, SYMDIFFERENCE, pm);
  }
  
  public static Geometry intersection(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, INTERSECTION, pm);
  }
  
  public static Geometry union(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, UNION, pm);
  }
  

}
