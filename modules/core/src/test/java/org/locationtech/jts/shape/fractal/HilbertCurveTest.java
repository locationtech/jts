package org.locationtech.jts.shape.fractal;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;

public class HilbertCurveTest 
extends TestCase
{
  public HilbertCurveTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(HilbertCurveTest.class);
  }
  
  public void testSize() {
    assertEquals( HilbertCurve.size( 2 ), 16);
    assertEquals( HilbertCurve.size( 4 ), 256);
    assertEquals( HilbertCurve.size( 5 ), 1024);
  }
  
  public void testDecode() {
    checkDecode(4,0, 0, 0);
    checkDecode(4, 1, 1, 0);
    
    checkDecode(4, 24, 6, 2);
    checkDecode(4, 255, 15, 0);
    
    checkDecode(5, 124, 8, 6);
  }

  public void testDecodeEncode() {
    checkDecodeEncode(5);
  }
  
  private void checkDecode(int order, int index, int x, int y) {
    Coordinate p = HilbertCurve.decode(order, index);
    System.out.println(p);
    assertEquals( (int) p.getX(), x);
    assertEquals( (int) p.getY(), y);
  }
  
  private void checkDecodeEncode(int order) {
    int n = HilbertCurve.size(order);
    for (int i = 0; i < n; i++) {
      checkDecodeEncode(order, i);
    }
  }

  private void checkDecodeEncode(int order, int index) {
    Coordinate p = HilbertCurve.decode(order, index);
    int encode = HilbertCurve.encode(order, (int) p.getX(), (int) p.getY() );
    assertEquals( index, encode);
  }

}
