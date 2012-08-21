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

}
