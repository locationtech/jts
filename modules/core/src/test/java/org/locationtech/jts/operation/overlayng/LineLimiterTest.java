package org.locationtech.jts.operation.overlayng;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class LineLimiterTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(LineLimiterTest.class);
  }

  public LineLimiterTest(String name) { super(name); }

  public void testEmptyEnv() {
    checkLimit(
        "LINESTRING (5 15, 5 25, 25 25, 25 5, 5 5)",
        new Envelope(),
        "MULTILINESTRING EMPTY"
        );
  }

  public void testPointEnv() {
    checkLimit(
        "LINESTRING (5 15, 5 25, 25 25, 25 5, 5 5)",
        new Envelope(10,10,10,10),
        "MULTILINESTRING EMPTY"
        );
  }

  public void testNonIntersecting() {
    checkLimit(
        "LINESTRING (5 15, 5 25, 25 25, 25 5, 5 5)",
        new Envelope(10,20,10,20),
        "MULTILINESTRING EMPTY"
        );
  }

  public void testPartiallyInside() {
    checkLimit(
        "LINESTRING (4 17, 8 14, 12 18, 15 15)",
        new Envelope(10,20,10,20),
        "LINESTRING (8 14, 12 18, 15 15)"
        );
  }
  
  public void testCrossing() {
    checkLimit(
        "LINESTRING (5 17, 8 14, 12 18, 15 15, 18 18, 22 14, 25 18)",
        new Envelope(10,20,10,20),
        "LINESTRING (8 14, 12 18, 15 15, 18 18, 22 14)"
        );
  }
  
  public void testCrossesTwice() {
    checkLimit(
        "LINESTRING (7 17, 23 17, 23 13, 7 13)",
        new Envelope(10,20,10,20),
        "MULTILINESTRING ((7 17, 23 17), (23 13, 7 13))"
        );
  }
  
  public void testDiamond() {
    checkLimit(
        "LINESTRING (8 15, 15 22, 22 15, 15 8, 8 15)",
        new Envelope(10,20,10,20),
        "LINESTRING (8 15, 15 8, 22 15, 15 22, 8 15)"
        );
  }
  
  public void testOctagon() {
    checkLimit(
        "LINESTRING (9 12, 12 9, 18 9, 21 12, 21 18, 18 21, 12 21, 9 18, 9 13)",
        new Envelope(10,20,10,20),
        "MULTILINESTRING ((9 12, 12 9), (18 9, 21 12), (21 18, 18 21), (12 21, 9 18))"
        );
  }

  private void checkLimit(String wkt, String wktBox, String wktExpected) {
    Geometry box = read(wktBox);
    Envelope clipEnv = box.getEnvelopeInternal();
    checkLimit(wkt, clipEnv, wktExpected);
  }

  private void checkLimit(String wkt, Envelope clipEnv, String wktExpected) {
    Geometry line = read(wkt);
    Geometry expected = read(wktExpected);
    
    LineLimiter limiter = new LineLimiter(clipEnv);
    List<Coordinate[]> sections = limiter.limit(line.getCoordinates());
    
    Geometry result = toLines(sections, line.getFactory());
    checkEqual(expected, result);
  }
  
  private static Geometry toLines(List<Coordinate[]> sections, GeometryFactory factory) {
    LineString[] lines = new LineString[sections.size()];
    int i = 0;
    for (Coordinate[] pts : sections) {
      lines[i++] = factory.createLineString(pts);
    }
    if (lines.length == 1) return lines[0];
    return factory.createMultiLineString(lines);
  }
}
