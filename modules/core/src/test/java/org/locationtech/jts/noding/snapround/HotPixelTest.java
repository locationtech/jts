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

  public void testRightSideVerticalTouchAbove() {
    checkIntersects(false, 1.2, 1.2, 10, 
        1.25, 1.25, 1.25, 2);
  }

  public void testRightSideVerticalTouchBelow() {
    checkIntersects(false, 1.2, 1.2, 10, 
        1.25, 0, 1.25, 1.15);
  }

  public void testRightSideVerticalOverlap() {
    checkIntersects(false, 1.2, 1.2, 10, 
        1.25, 0, 1.25, 1.5);
  }

  //-----------------------------

  public void testTopSideHorizontalTouchRight() {
    checkIntersects(false, 1.2, 1.2, 10, 
        1.25, 1.25, 2, 1.25);
  }

  public void testTopSideHorizontalTouchLeft() {
    checkIntersects(false, 1.2, 1.2, 10, 
        0, 1.25, 1.15, 1.25);
  }

  public void testTopSideHorizontalOverlap() {
    checkIntersects(false, 1.2, 1.2, 10, 
        0, 1.25, 1.9, 1.25);
  }
  
  //-----------------------------

  public void testLeftSideVerticalTouchAbove() {
    checkIntersects(false, 1.2, 1.2, 10, 
        1.15, 1.25, 1.15, 2);
  }

  public void testLeftSideVerticalOverlap() {
    checkIntersects(true, 1.2, 1.2, 10, 
        1.15, 0, 1.15, 1.8);
  }

  public void testLeftSideVerticalTouchBelow() {
    checkIntersects(true, 1.2, 1.2, 10, 
        1.15, 0, 1.15, 1.15);
  }

  public void testLeftSideCrossRight() {
    checkIntersects(true, 1.2, 1.2, 10, 
        0, 1.19, 2, 1.21);
  }

  public void testLeftSideCrossTop() {
    checkIntersects(true, 1.2, 1.2, 10, 
        0.8, 0.8, 1.3, 1.39);
  }

  public void testLeftSideCrossBottom() {
    checkIntersects(true, 1.2, 1.2, 10, 
        1, 1.5, 1.3, 0.9 );
  }

  //-----------------------------
  
  public void testBottomSideHorizontalTouchRight() {
    checkIntersects(false, 1.2, 1.2, 10, 
        1.25, 1.15, 2, 1.15);
  }

  public void testBottomSideHorizontalTouchLeft() {
    checkIntersects(true, 1.2, 1.2, 10, 
        0, 1.15, 1.15, 1.15);
  }

  public void testBottomSideHorizontalOverlapLeft() {
    checkIntersects(true, 1.2, 1.2, 10, 
        0, 1.15, 1.2, 1.15);
  }

  public void testBottomSideHorizontalOverlap() {
    checkIntersects(true, 1.2, 1.2, 10, 
        0, 1.15, 1.9, 1.15);
  }

  public void testBottomSideHorizontalOverlapRight() {
    checkIntersects(true, 1.2, 1.2, 10, 
        1.2, 1.15, 1.4, 1.15);
  }
  
  public void testBottomSideCrossRight() {
    checkIntersects(true, 1.2, 1.2, 10, 
        1.1, 1, 1.4, 1.4);
  }

  public void testBottomSideCrossTop() {
    checkIntersects(true, 1.2, 1.2, 10, 
        1.1, 0.9, 1.3, 1.6);
  }
  
  //-----------------------------
  
  public void testDiagonalDown() {
    checkIntersects(true, 1.2, 1.2, 10, 
        0.9, 1.5, 1.4, 1 );
  }
  
  public void testDiagonalUp() {
    checkIntersects(true, 1.2, 1.2, 10, 
        0.9, 0.9, 1.5, 1.5 );
  }
  
  //-----------------------------
  // Test segments entering through a corner and terminating inside pixel
  
  public void testCornerULEndInside() {
    checkIntersects(true, 1, 1, 10, 
        0.7, 1.3, 0.98, 1.02 );
  }
  
  public void testCornerLLEndInside() {
    checkIntersects(true, 1, 1, 10, 
        0.8, 0.8, 0.98, 0.98 );
  }
  
  public void testCornerURStartInside() {
    checkIntersects(true, 1, 1, 10, 
        1.02, 1.02, 1.3, 1.3 );
  }
  
  public void testCornerLRStartInside() {
    checkIntersects(true, 1, 1, 10, 
        1.02, 0.98, 1.3, 0.7 );
  }
  
  //-----------------------------
  // Test segments tangent to a corner
  
  public void testCornerLLTangent() {
    checkIntersects(true, 1, 1, 10, 
        0.9, 1, 1, 0.9 );
  }

  public void testCornerLLTangentNoTouch() {
    checkIntersects(false, 1, 1, 10, 
        0.9, 0.9, 1, 0.9 );
  }

  public void testCornerULTangent() {
    // does not intersect due to open top
    checkIntersects(false, 1, 1, 10, 
        0.9, 1, 1, 1.1 );
  }

  public void testCornerURTangent() {
    // does not intersect due to open top
    checkIntersects(false, 1, 1, 10, 
        1, 1.1, 1.1, 1 );
  }

  public void testCornerLRTangent() {
    // does not intersect due to open right side
    checkIntersects(false, 1, 1, 10, 
        1, 0.9, 1.1, 1 );
  }

  public void testCornerULTouchEnd() {
    // does not intersect due to bounding box check for open top
    checkIntersects(false, 1, 1, 10, 
        0.9, 1.1, 0.95, 1.05 );
  }


  //================================================
  
  private void checkIntersects(boolean expected, 
      double x, double y, double scale, 
      double x1, double y1, double x2, double y2) {
    HotPixel hp = new HotPixel(new Coordinate(x,y), scale);
    Coordinate p1 = new Coordinate(x1,y1);
    Coordinate p2 = new Coordinate(x2,y2);
    boolean actual = hp.intersects(p1, p2);
    assertEquals(expected, actual);
  }
}
