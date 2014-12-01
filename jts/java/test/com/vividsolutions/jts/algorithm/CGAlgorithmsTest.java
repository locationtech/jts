package com.vividsolutions.jts.algorithm;

import com.vividsolutions.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class CGAlgorithmsTest
extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(CGAlgorithmsTest.class);
  }

  public CGAlgorithmsTest(String name) { super(name); }

  public void testDistancePointLinePerpendicular() {
    assertEquals(0.5, CGAlgorithms.distancePointLinePerpendicular(
        new Coordinate(0.5, 0.5), new Coordinate(0,0), new Coordinate(1,0)), 0.000001);
    assertEquals(0.5, CGAlgorithms.distancePointLinePerpendicular(
        new Coordinate(3.5, 0.5), new Coordinate(0,0), new Coordinate(1,0)), 0.000001);
    assertEquals(0.707106, CGAlgorithms.distancePointLinePerpendicular(
        new Coordinate(1,0), new Coordinate(0,0), new Coordinate(1,1)), 0.000001);
  }

  public void testDistancePointLine() {
    assertEquals(0.5, CGAlgorithms.distancePointLine(
        new Coordinate(0.5, 0.5), new Coordinate(0,0), new Coordinate(1,0)), 0.000001);
    assertEquals(1.0, CGAlgorithms.distancePointLine(
        new Coordinate(2, 0), new Coordinate(0,0), new Coordinate(1,0)), 0.000001);
  }

  public void testDistanceLineLineDisjointCollinear() {
    assertEquals(1.999699, CGAlgorithms.distanceLineLine(
        new Coordinate(0,0), new Coordinate(9.9, 1.4), 
        new Coordinate(11.88, 1.68), new Coordinate(21.78, 3.08)), 0.000001);
  }

  public void testOrientationIndexRobust() throws Exception 
  { 
    Coordinate p0 = new Coordinate(219.3649559090992, 140.84159161824724); 
    Coordinate p1 = new Coordinate(168.9018919682399, -5.713787599646864); 
    Coordinate p = new Coordinate(186.80814046338352, 46.28973405831556); 
    int orient = CGAlgorithms.orientationIndex(p0, p1, p); 
    int orientInv = CGAlgorithms.orientationIndex(p1, p0, p); 
    assert(orient != orientInv); 
  } 
}
