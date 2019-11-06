package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class RingClipperTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(RingClipperTest.class);
  }

  public RingClipperTest(String name) { super(name); }
  
  public void xtestLineCorner3() {
    checkClip(
        "LINESTRING (80 120, 90 160, 120 190, 170 210)",
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "LINESTRING (160 200, 100 140, 100 170)"
        );
  }

  public void testClipCompletely() {
    checkClip(
        "POLYGON ((2 9, 7 27, 26 34, 45 10, 26 9, 17 -7, 14 4, 2 9))",
        new Envelope(10,20,10,20),
        "LINESTRING (10 20, 20 20, 20 10, 10 10, 10 20)"
        );
  }

  public void testInside() {
    checkClip(
        "POLYGON ((12 13, 13 17, 18 17, 15 16, 17 12, 14 14, 12 13))",
        new Envelope(10,20,10,20),
        "LINESTRING (12 13, 13 17, 18 17, 15 16, 17 12, 14 14, 12 13)"
        );
  }

  public void testStarClipped() {
    checkClip(
        "POLYGON ((7 15, 12 18, 15 23, 18 18, 24 15, 18 12, 15 7, 12 12, 7 15))",
        new Envelope(10,20,10,20),
        "LINESTRING (10 16.8, 12 18, 13.2 20, 16.8 20, 18 18, 20 17, 20 13, 18 12, 16.8 10, 13.2 10, 12 12, 10 13.2, 10 16.8)"
        );
  }

  private void checkClip(String wkt, String wktBox, String wktExpected) {
    Geometry box = read(wktBox);
    Envelope clipEnv = box.getEnvelopeInternal();
    checkClip(wkt, clipEnv, wktExpected);
  }

  private void checkClip(String wkt, Envelope clipEnv, String wktExpected) {
    Geometry line = read(wkt);
    Geometry expected = read(wktExpected);
    
    RingClipper clipper = new RingClipper(clipEnv);
    Coordinate[] pts = clipper.clip(line.getCoordinates());
    
    LineString result = line.getFactory().createLineString(pts);
    checkEqual(expected, result);
  }
}
