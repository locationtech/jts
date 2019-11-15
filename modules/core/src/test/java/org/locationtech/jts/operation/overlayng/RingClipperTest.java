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

  public void testEmptyEnv() {
    checkClip(
        "POLYGON ((2 9, 7 27, 26 34, 45 10, 26 9, 17 -7, 14 4, 2 9))",
        new Envelope(),
        "LINESTRING EMPTY"
        );
  }

  public void testPointEnv() {
    checkClip(
        "POLYGON ((2 9, 7 27, 26 34, 45 10, 26 9, 17 -7, 14 4, 2 9))",
        new Envelope(10,10,10,10),
        "LINESTRING EMPTY"
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
        new Envelope(10,20, 10,20),
        "LINESTRING (10 16.8, 12 18, 13.2 20, 16.8 20, 18 18, 20 17, 20 13, 18 12, 16.8 10, 13.2 10, 12 12, 10 13.2, 10 16.8)"
        );
  }

  public void testWrapPartial() {
    checkClip(
        "POLYGON ((30 60, 60 60, 40 80, 40 110, 110 110, 110 80, 90 60, 120 60, 120 120, 30 120, 30 60))",
        new Envelope(50,100, 50,100),
        "LINESTRING (50 60, 60 60, 50 70, 50 100, 100 100, 100 70, 90 60, 100 60, 100 100, 50 100, 50 60)"
        );
  }

  public void testWrapAllSides() {
    checkClip(
        "POLYGON ((30 80, 60 80, 60 90, 40 90, 40 110, 110 110, 110 40, 40 40, 40 59, 60 59, 60 70, 30 70, 30 30, 120 30, 120 120, 30 120, 30 80))",
        new Envelope(50,100, 50,100),
        "LINESTRING (50 80, 60 80, 60 90, 50 90, 50 100, 100 100, 100 50, 50 50, 50 59, 60 59, 60 70, 50 70, 50 50, 100 50, 100 100, 50 100, 50 80)"
        );
  }

  public void testWrapOverlap() {
    checkClip(
        "POLYGON ((30 80, 60 80, 60 90, 40 90, 40 110, 110 110, 110 40, 40 40, 40 59, 30 70, 20 100, 10 100, 10 30, 120 30, 120 120, 30 120, 30 80))",
        new Envelope(50,100, 50,100),
        "LINESTRING (50 80, 60 80, 60 90, 50 90, 50 100, 100 100, 100 50, 50 50, 100 50, 100 100, 50 100, 50 80)"
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
