package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class RingClipperTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(RingClipperTest.class);
  }

  public RingClipperTest(String name) { super(name); }
  
  public void testLineCorner3() {
    checkClip(
        "LINESTRING (80 120, 90 160, 120 190, 170 210)",
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "LINESTRING (160 200, 100 140, 100 170)"
        );
  }

  private void checkClip(String wkt, String wktBox, String wktExpected) {
    Geometry line = read(wkt);
    Geometry box = read(wktBox);
    Geometry expected = read(wktExpected);
    
    RingClipper clipper = new RingClipper(box.getEnvelopeInternal());
    Coordinate[] pts = clipper.clip(line.getCoordinates());
    
    LineString result = line.getFactory().createLineString(pts);
    checkEqual(expected, result);
  }
}
