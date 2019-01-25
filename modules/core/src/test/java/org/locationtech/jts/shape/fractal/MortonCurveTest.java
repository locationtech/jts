package org.locationtech.jts.shape.fractal;

import org.locationtech.jts.geom.Coordinate;

import static org.locationtech.jts.shape.fractal.MortonCurve.*;

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
    checkDecode(0, 0, 0);
    checkDecode(1, 1, 0);
    checkDecode(24, 4, 2);
    checkDecode(124, 14, 6);
    checkDecode(255, 15, 15);
  }


  public void testDecodeEncode() {
    checkDecodeEncode(5);
  }
  
  private void checkDecode(int index, int x, int y) {
    Coordinate p = decode(index);
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
    Coordinate p = decode(index);
    int encode = encode((int) p.getX(), (int) p.getY() );
    assertEquals( index, encode);
  }
}
