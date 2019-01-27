package org.locationtech.jts.shape.fractal;

import org.locationtech.jts.geom.Coordinate;

import static org.locationtech.jts.shape.fractal.MortonCode.*;

import junit.framework.TestCase;

public class MortonCodeTest 
extends TestCase
{
  public MortonCodeTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(MortonCodeTest.class);
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
  
  public void testLevel() {
    assertEquals( level( 1 ), 0);
    
    assertEquals( level( 2 ), 1);
    assertEquals( level( 3 ), 1);
    assertEquals( level( 4 ), 1);
    
    assertEquals( level( 5 ), 2);
    assertEquals( level( 13 ), 2);
    assertEquals( level( 15 ), 2);
    assertEquals( level( 16 ), 2);
    
    assertEquals( level( 17 ), 3);
    assertEquals( level( 63 ), 3);
    assertEquals( level( 64 ), 3);
    
    assertEquals( level( 65 ), 4);
    assertEquals( level( 255 ), 4);
    assertEquals( level( 255 ), 4);
    assertEquals( level( 256 ), 4);
  }
  
  public void testDecode() {
    checkDecode(0, 0, 0);    
    checkDecode(1, 1, 0);
    checkDecode(2, 0, 1);
    checkDecode(3, 1, 1);
    checkDecode(4, 2, 0);
    
    checkDecode(24, 4, 2);
    checkDecode(124, 14, 6);
    checkDecode(255, 15, 15);
  }


  public void testDecodeEncode() {
    checkDecodeEncodeForLevel(4);
    checkDecodeEncodeForLevel(5);
  }
  
  private void checkDecode(int index, int x, int y) {
    Coordinate p = decode(index);
    //System.out.println(p);
    assertEquals( (int) p.getX(), x);
    assertEquals( (int) p.getY(), y);
  }
  
  private void checkDecodeEncodeForLevel(int level) {
    int n = size(level);
    for (int i = 0; i < n; i++) {
      checkDecodeEncode(level, i);
    }
  }

  private void checkDecodeEncode(int level, int index) {
    Coordinate p = decode(index);
    int encode = encode((int) p.getX(), (int) p.getY() );
    assertEquals( index, encode);
  }
}
