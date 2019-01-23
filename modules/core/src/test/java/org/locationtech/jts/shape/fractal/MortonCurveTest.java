package org.locationtech.jts.shape.fractal;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;

public class MortonCurveTest 
extends TestCase
{
  public MortonCurveTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(MortonCurveTest.class);
  }
  
  public void testSize() {
    assertEquals( MortonCurve.size( 2 ), 16);
    assertEquals( MortonCurve.size( 4 ), 256);
  }
  
  public void testDecode() {
    checkDecode(0, 0, 0);
    checkDecode(1, 1, 0);
    checkDecode(24, 4, 2);
    checkDecode(124, 14, 6);
    checkDecode(255, 15, 15);
  }

  private void checkDecode(int index, int x, int y) {
    Coordinate p = MortonCurve.decode(index);
    //System.out.println(p);
    assertEquals( (int) p.getX(), x);
    assertEquals( (int) p.getY(), y);
  }
}
