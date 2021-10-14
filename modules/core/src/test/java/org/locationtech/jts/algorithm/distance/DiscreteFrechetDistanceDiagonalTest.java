package org.locationtech.jts.algorithm.distance;

import junit.framework.TestCase;

public class DiscreteFrechetDistanceDiagonalTest extends TestCase {
  
  public DiscreteFrechetDistanceDiagonalTest(String name) {
    super(name);
  }
  
  public void test1x1() {
    checkDiagonal(1,1, xy(0,0));
  }

  public void test2x2() {
    checkDiagonal(2,2, xy(0,0,1,1));
  }

  public void test3x3() {
    checkDiagonal(3,3, xy(0,0, 1,1, 2,2));
  }

  public void test3x4() {
    checkDiagonal(3,4, xy(0, 0, 1, 1, 1, 2, 2, 3));
  }

  public void test3x5() {
    checkDiagonal(3,5, xy(0,0, 0,1, 1,2, 1,3, 2,4));
  }

  public void test3x6() {
    checkDiagonal(3,6, xy(0,0, 0,1, 1,2, 1,3, 2,4, 2,5));
  }

  public void test6x2() {
    checkDiagonal(6,2, xy(0,0, 1,0, 2,0, 3,1, 4,1, 5,1));
  }

  public void test2x6() {
    checkDiagonal(2,6, xy(0,0, 0,1, 0,2, 1,3, 1,4, 1,5));
  }

  private void checkDiagonal(int cols, int rows, int[] xyExpected) {
    int[] xy = DiscreteFrechetDistance.bresenhamDiagonal(cols, rows);
    assertEquals(xyExpected.length, xy.length);
    for (int i = 0 ; i < xy.length; i++) {
      assertEquals(xyExpected[i], xy[i]);
    }
  }

  private static int[] xy(int... ord) {
    return ord;
  }
}
