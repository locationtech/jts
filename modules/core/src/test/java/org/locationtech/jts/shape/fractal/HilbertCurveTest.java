package org.locationtech.jts.shape.fractal;

import static org.locationtech.jts.shape.fractal.HilbertCurve.*;
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
    assertEquals( size( 0 ), 1);
    assertEquals( size( 1 ), 4);
    assertEquals( size( 2 ), 16);
    assertEquals( size( 3 ), 64);
    assertEquals( size( 4 ), 256);
    assertEquals( size( 5 ), 1024);
    assertEquals( size( 6 ), 4096);
  }
  
  public void testOrder() {
    assertEquals( order( 4 ), 1);
    
    assertEquals( order( 5 ), 2);
    assertEquals( order( 13 ), 2);
    assertEquals( order( 15 ), 2);
    assertEquals( order( 16 ), 2);
    
    assertEquals( order( 17 ), 3);
    assertEquals( order( 63 ), 3);
    assertEquals( order( 64 ), 3);
    
    assertEquals( order( 65 ), 4);
    assertEquals( order( 255 ), 4);
    assertEquals( order( 255 ), 4);
    assertEquals( order( 256 ), 4);
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
    Coordinate p = decode(order, index);
    //System.out.println(p);
    assertEquals( (int) p.getX(), x);
    assertEquals( (int) p.getY(), y);
  }
  
  private void checkDecodeEncode(int order) {
    int n = size(order);
    for (int i = 0; i < n; i++) {
      checkDecodeEncode(order, i);
    }
  }

  private void checkDecodeEncode(int order, int index) {
    Coordinate p = decode(order, index);
    int encode = encode(order, (int) p.getX(), (int) p.getY() );
    assertEquals( index, encode);
  }

}
