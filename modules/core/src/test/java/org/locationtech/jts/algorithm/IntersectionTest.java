package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;

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
}
