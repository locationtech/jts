package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.io.WKTReader;

public class IntersectionTest extends TestCase {
  private static final double MAX_ABS_ERROR = 1e-5;

  public static void main(String args[]) {
    TestRunner.run(IntersectionTest.class);
  }

  public IntersectionTest(String name) { super(name); }
  
  public void testSimple() {
    checkIntersection(
        0,0,  10,10,
        0,10, 10,0,
        5,5);
  }

  public void testCollinear() {
    checkIntersectionNull(
        0,0,  10,10,
        20,20, 30, 30 );
  }

  public void testParallel() {
    checkIntersectionNull(
        0,0,  10,10,
        10,0, 20,10 );
  }

  // See JTS GitHub issue #464
  public void testAlmostCollinear() {
    checkIntersection(
        35613471.6165017, 4257145.306132293, 35613477.7705378, 4257160.528222711,
        35613477.77505724, 4257160.539653536, 35613479.85607389, 4257165.92369170,
        35613477.772841461, 4257160.5339209242 );
  }
  
  // same as above but conditioned manually
  public void testAlmostCollinearCond() {
    checkIntersection(
        1.6165017, 45.306132293, 7.7705378, 60.528222711,
        7.77505724, 60.539653536, 9.85607389, 65.92369170,
        7.772841461, 60.5339209242 );
  }

  //------------------------------------------------------------
  
  public void testLineSegCross() {
    checkIntersectionLineSegment( 0, 0, 0, 1,     -1, 9, 1, 9,     0, 9 );
    checkIntersectionLineSegment( 0, 0, 0, 1,     -1, 2, 1, 4,     0, 3 );
  }

  public void testLineSegTouch() {
    checkIntersectionLineSegment( 0, 0, 0, 1,     -1, 9, 0, 9,     0, 9 );
    checkIntersectionLineSegment( 0, 0, 0, 1,      0, 2, 1, 4,     0, 2 );
  }

  public void testLineSegCollinear() {
    checkIntersectionLineSegment( 0, 0, 0, 1,     0, 9, 0, 8,     0, 9 );
  }

  public void testLineSegNone() {
    checkIntersectionLineSegmentNull( 0, 0, 0, 1,    2, 9,  1, 9 );
    checkIntersectionLineSegmentNull( 0, 0, 0, 1,   -2, 9, -1, 9 );
    checkIntersectionLineSegmentNull( 0, 0, 0, 1,    2, 9,  1, 9 );
  }

  public void testIntersectionXY() throws Exception {
    // intersection with dim 3 x dim3
    WKTReader reader = new WKTReader();
    Geometry poly1 = reader.read("POLYGON((0 0 0, 0 10000 2, 10000 10000 2, 10000 0 0, 0 0 0))");
    Geometry clipArea = reader.read("POLYGON((0 0, 0 2500, 2500 2500, 2500 0, 0 0))");
    Geometry clipped1 = poly1.intersection(clipArea);

    // intersection with dim 3 x dim 2
    GeometryFactory gf = poly1.getFactory();
    CoordinateSequenceFactory csf = gf.getCoordinateSequenceFactory();
    double xmin = 0.0;
    double xmax = 2500.0;
    double ymin = 0.0;
    double ymax = 2500.0;

    CoordinateSequence cs = csf.create(5,2);
    cs.setOrdinate(0, 0, xmin);
    cs.setOrdinate(0, 1, ymin);
    cs.setOrdinate(1, 0, xmin);
    cs.setOrdinate(1, 1, ymax);
    cs.setOrdinate(2, 0, xmax);
    cs.setOrdinate(2, 1, ymax);
    cs.setOrdinate(3, 0, xmax);
    cs.setOrdinate(3, 1, ymin);
    cs.setOrdinate(4, 0, xmin);
    cs.setOrdinate(4, 1, ymin);

    LinearRing bounds = gf.createLinearRing(cs);

    Polygon fence = gf.createPolygon(bounds, null);
    Geometry clipped2 = poly1.intersection(fence);

    assertTrue(clipped1.equals(clipped2));
  }

  //==================================================
  
  private void checkIntersection(double p1x, double p1y, double p2x, double p2y, 
      double q1x, double q1y, double q2x, double q2y, 
      double expectedx, double expectedy) {
    Coordinate p1 = new Coordinate(p1x, p1y);
    Coordinate p2 = new Coordinate(p2x, p2y);
    Coordinate q1 = new Coordinate(q1x, q1y);
    Coordinate q2 = new Coordinate(q2x, q2y);
    //Coordinate actual = CGAlgorithmsDD.intersection(p1, p2, q1, q2);
    Coordinate actual = Intersection.intersection(p1, p2, q1, q2);
    Coordinate expected = new Coordinate( expectedx, expectedy );
    double dist = actual.distance(expected);
    //System.out.println("Expected: " + expected + "  Actual: " + actual + "  Dist = " + dist);
    assertTrue(dist <= MAX_ABS_ERROR);
  }
  
  private void checkIntersectionNull(double p1x, double p1y, double p2x, double p2y, 
      double q1x, double q1y, double q2x, double q2y) {
    Coordinate p1 = new Coordinate(p1x, p1y);
    Coordinate p2 = new Coordinate(p2x, p2y);
    Coordinate q1 = new Coordinate(q1x, q1y);
    Coordinate q2 = new Coordinate(q2x, q2y);
    Coordinate actual = Intersection.intersection(p1, p2, q1, q2);
    assertTrue(actual == null);
  }
  
  private void checkIntersectionLineSegment(double p1x, double p1y, double p2x, double p2y, 
      double q1x, double q1y, double q2x, double q2y, 
      double expectedx, double expectedy) {
    Coordinate p1 = new Coordinate(p1x, p1y);
    Coordinate p2 = new Coordinate(p2x, p2y);
    Coordinate q1 = new Coordinate(q1x, q1y);
    Coordinate q2 = new Coordinate(q2x, q2y);
    //Coordinate actual = CGAlgorithmsDD.intersection(p1, p2, q1, q2);
    Coordinate actual = Intersection.lineSegment(p1, p2, q1, q2);
    Coordinate expected = new Coordinate( expectedx, expectedy );
    double dist = actual.distance(expected);
    //System.out.println("Expected: " + expected + "  Actual: " + actual + "  Dist = " + dist);
    assertTrue(dist <= MAX_ABS_ERROR);
  }
  
  private void checkIntersectionLineSegmentNull(double p1x, double p1y, double p2x, double p2y, 
      double q1x, double q1y, double q2x, double q2y) {
    Coordinate p1 = new Coordinate(p1x, p1y);
    Coordinate p2 = new Coordinate(p2x, p2y);
    Coordinate q1 = new Coordinate(q1x, q1y);
    Coordinate q2 = new Coordinate(q2x, q2y);
    Coordinate actual = Intersection.lineSegment(p1, p2, q1, q2);
    assertTrue(actual == null);
  }
}
