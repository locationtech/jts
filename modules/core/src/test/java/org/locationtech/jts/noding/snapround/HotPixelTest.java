package org.locationtech.jts.noding.snapround;

import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class HotPixelTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(HotPixelTest.class);
  }

  public HotPixelTest(String name) { super(name); }
  
  
  public void testBelow() {
    checkIntersects(false, 1, 1, 100, 
        1, 0.98, 3, 0.5);
  }

  public void testAbove() {
    checkIntersects(false, 1, 1, 100, 
        1, 1.011, 3, 1.5);
  }

  public void testRightEdgeVerticalTouchAbove() {
    checkIntersects(false, 1.2, 1.2, 10, 
        1.25, 1.25, 1.25, 2);
  }

  public void testRightEdgeVerticalTouchBelow() {
    checkIntersects(false, 1.2, 1.2, 10, 
        1.25, 0, 1.25, 1.15);
  }

  public void testRightEdgeVerticalOverlap() {
    checkIntersects(false, 1.2, 1.2, 10, 
        1.25, 1.2, 1.25, 0);
  }

  public void testLeftEdgeVerticalTouchAbove() {
    checkIntersects(false, 1.2, 1.2, 10, 
        1.15, 1.25, 1.15, 2);
  }

  public void testLeftEdgeVerticalOverlap() {
    checkIntersects(true, 1.2, 1.2, 10, 
        1.15, 1.2, 1.15, 0);
  }

  public void testLeftEdgeVerticalTouchBelow() {
    checkIntersects(true, 1.2, 1.2, 10, 
        1.15, 0, 1.15, 1.15);
  }

  public void testLeftEdgeCrossRight() {
    checkIntersects(true, 1.2, 1.2, 10, 
        0, 1.19, 2, 1.21);
  }

  public void testLeftEdgeCrossTop() {
    checkIntersects(true, 1.2, 1.2, 10, 
        0.8, 0.8, 1.3, 1.39);
  }

  //================================================
  
  private void checkIntersects(boolean expected, 
      double x, double y, double scale, 
      double x1, double y1, double x2, double y2) {
    RobustLineIntersector li = new RobustLineIntersector();
    HotPixel hp = new HotPixel(new Coordinate(x,y), scale, li);
    Coordinate p1 = new Coordinate(x1,y1);
    Coordinate p2 = new Coordinate(x2,y2);
    boolean actual = hp.intersects(p1, p2);
    assertEquals(expected, actual);
  }
}
