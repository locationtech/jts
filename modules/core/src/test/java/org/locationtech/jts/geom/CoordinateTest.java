/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class CoordinateTest extends TestCase
{
  public CoordinateTest(String name)
  {
    super(name);
  }

  public static void main(String args[]) {
    TestRunner.run(CoordinateTest.class);
  }
  
  public void testConstructor3D() 
  {
    Coordinate c = new Coordinate(350.2, 4566.8, 5266.3);
    assertEquals(c.x, 350.2);
    assertEquals(c.y, 4566.8);
    assertEquals(c.z, 5266.3);
  }
  
  public void testConstructor2D() 
  {
    Coordinate c = new Coordinate(350.2, 4566.8);
    assertEquals(c.x, 350.2);
    assertEquals(c.y, 4566.8);
    assertEquals(c.z, Coordinate.NULL_ORDINATE);
  }
  public void testDefaultConstructor() 
  {
    Coordinate c = new Coordinate();
    assertEquals(c.x, 0.0);
    assertEquals(c.y, 0.0);
    assertEquals(c.z, Coordinate.NULL_ORDINATE);
  }
  public void testCopyConstructor3D() 
  {
    Coordinate orig = new Coordinate(350.2, 4566.8, 5266.3);
    Coordinate c = new Coordinate(orig);
    assertEquals(c.x, 350.2);
    assertEquals(c.y, 4566.8);
    assertEquals(c.z, 5266.3);
  }
  public void testSetCoordinate() 
  {
    Coordinate orig = new Coordinate(350.2, 4566.8, 5266.3);
    Coordinate c = new Coordinate();
    c.setCoordinate(orig);
    assertEquals(c.x, 350.2);
    assertEquals(c.y, 4566.8);
    assertEquals(c.z, 5266.3);
  }
  public void testGetOrdinate() 
  {
    Coordinate c = new Coordinate(350.2, 4566.8, 5266.3);
    assertEquals(c.getOrdinate(Coordinate.X), 350.2);
    assertEquals(c.getOrdinate(Coordinate.Y), 4566.8);
    assertEquals(c.getOrdinate(Coordinate.Z), 5266.3);
  }
  public void testSetOrdinate() 
  {
    Coordinate c = new Coordinate();
    c.setOrdinate(Coordinate.X, 111);
    c.setOrdinate(Coordinate.Y, 222);
    c.setOrdinate(Coordinate.Z, 333);
    assertEquals(c.getOrdinate(Coordinate.X), 111.0);
    assertEquals(c.getOrdinate(Coordinate.Y), 222.0);
    assertEquals(c.getOrdinate(Coordinate.Z), 333.0);
  }
  public void testEquals()
  {
    Coordinate c1 = new Coordinate(1,2,3);
    String s = "Not a coordinate";
    assertTrue(! c1.equals(s));
    
    Coordinate c2 = new Coordinate(1,2,3);
    assertTrue(c1.equals2D(c2));

    Coordinate c3 = new Coordinate(1,22,3);
    assertTrue(! c1.equals2D(c3));
  }
  public void testEquals2D()
  {
    Coordinate c1 = new Coordinate(1,2,3);
    Coordinate c2 = new Coordinate(1,2,3);
    assertTrue(c1.equals2D(c2));
    
    Coordinate c3 = new Coordinate(1,22,3);
    assertTrue(! c1.equals2D(c3));
  }
  public void testEquals3D()
  {
    Coordinate c1 = new Coordinate(1,2,3);
    Coordinate c2 = new Coordinate(1,2,3);
    assertTrue(c1.equals3D(c2));
    
    Coordinate c3 = new Coordinate(1,22,3);
    assertTrue(! c1.equals3D(c3));
  }
  public void testEquals2DWithinTolerance() 
  {
    Coordinate c = new Coordinate(100.0, 200.0, 50.0);
    Coordinate aBitOff = new Coordinate(100.1, 200.1, 50.0);
    assertTrue(c.equals2D(aBitOff, 0.2));
  }

  public void testEqualsInZ() {
    
    Coordinate c = new Coordinate(100.0, 200.0, 50.0);
    Coordinate withSameZ = new Coordinate(100.1, 200.1, 50.1);
    assertTrue(c.equalInZ(withSameZ, 0.2));
  }

  public void testCompareTo() 
  {
    Coordinate lowest = new Coordinate(10.0, 100.0, 50.0);
    Coordinate highest = new Coordinate(20.0, 100.0, 50.0);
    Coordinate equalToHighest = new Coordinate(20.0, 100.0, 50.0);
    Coordinate higherStill = new Coordinate(20.0, 200.0, 50.0);
    
    assertEquals(-1, lowest.compareTo(highest));
    assertEquals(1, highest.compareTo(lowest));
    assertEquals(-1, highest.compareTo(higherStill));
    assertEquals(0, highest.compareTo(equalToHighest));
  }
  public void testToString() 
  {
    String expectedResult = "(100.0, 200.0, 50.0)";
    String actualResult = new Coordinate(100.0, 200.0, 50.0).toString();
    assertEquals(expectedResult, actualResult);
  }

  public void testClone() {
    Coordinate c = new Coordinate(100.0, 200.0, 50.0);
    Coordinate clone = (Coordinate) c.clone();
    assertTrue(c.equals3D(clone));
  }

  public void testDistance() {
    Coordinate coord1 = new Coordinate(0.0, 0.0, 0.0);
    Coordinate coord2 = new Coordinate(100.0, 200.0, 50.0);
    double distance = coord1.distance(coord2);
    assertEquals(distance, 223.60679774997897, 0.00001);
  }
  public void testDistance3D() {
    Coordinate coord1 = new Coordinate(0.0, 0.0, 0.0);
    Coordinate coord2 = new Coordinate(100.0, 200.0, 50.0);
    double distance = coord1.distance3D(coord2);
    assertEquals(distance, 229.128784747792, 0.000001);
  }


}
