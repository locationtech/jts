package org.locationtech.jts.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class CoordinateListTest extends TestCase {
  public static void main(String args[]) {
    TestRunner.run(CoordinateListTest.class);
  }

  public CoordinateListTest(String name) { super(name); }

  public void testForward() {
    checkValue(coordList(0,0,1,1,2,2).toCoordinateArray(true), 
        0,0,1,1,2,2);
  }
  
  public void testReverse() {
    checkValue(coordList(0,0,1,1,2,2).toCoordinateArray(false), 
        2,2,1,1,0,0);
  }
  
  public void testReverseEmpty() {
    checkValue(coordList().toCoordinateArray(false) );
  }

  public void testClone() {
    CoordinateList clone = (CoordinateList) coordList(0,0,1,1,2,2).clone();
    checkValue(clone.toCoordinateArray(true), 0,0,1,1,2,2);    
  }
  
  //======================================
  
  private void checkValue(Coordinate[] coordArray, double ... ords) {
    
    assertEquals( "list has wrong length", coordArray.length, ords.length / 2);
    
    for (int i = 0 ; i < coordArray.length; i += 2) {
      Coordinate pt = coordArray[i];
      assertEquals(pt.getX(), ords[2 * i]);
      assertEquals(pt.getY(), ords[2 * i + 1]);
    }
  }

  private CoordinateList coordList(double ... ords) {
    CoordinateList cl = new CoordinateList();
    for (int i = 0 ; i < ords.length; i += 2) {
      cl.add(new Coordinate(ords[i], ords[i+1]), false);
    }
    return cl;
  }

}
