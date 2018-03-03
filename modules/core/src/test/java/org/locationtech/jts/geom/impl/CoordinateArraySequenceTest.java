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

package org.locationtech.jts.geom.impl;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.CoordinateXYZM;

import junit.textui.TestRunner;

/**
 * Test {@link CoordinateArraySequence}
 *
 * @version 1.7
 */
public class CoordinateArraySequenceTest
    extends CoordinateSequenceTestBase
{
  public static void main(String args[]) {
    TestRunner.run(CoordinateArraySequenceTest.class);
  }

  public CoordinateArraySequenceTest(String name)
  {
    super(name);
  }

  @Override
  CoordinateSequenceFactory getCSFactory() {
    return CoordinateArraySequenceFactory.instance();
  }

  @Override
  int getDefaultDimension() {
    CoordinateSequence seq = getCSFactory().create(new Coordinate[0]);
    return seq.getDimension();
  }

  @Override
  public void testConstructorDirect() {

    CoordinateSequence seq = new CoordinateArraySequence(5);
    assertNotNull(seq);
    assertEquals(5, seq.size());
    assertEquals(getDefaultDimension(), seq.getDimension());

  }

  public void testSetMeasureThrows() {
    CoordinateSequence seq = new CoordinateArraySequence(1);
    try {
      seq.setOrdinate(0, CoordinateSequence.M, 0d);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
  }

  @Override
  boolean isSame(CoordinateSequence seq1, CoordinateSequence seq2) {

    if (seq1 == seq2)
      return true;
    if (seq1.toCoordinateArray() == seq2.toCoordinateArray())
      return true;

    if (seq1.size() == seq2.size()) {
      for (int i = 0; i < seq1.size(); i++) {
        if (seq1.getCoordinate(i) != seq2.getCoordinate(i))
          return false;
      }
      return true;
    }
    return false;
  }
  
  public void testDimensionAndMeasure()
  {
    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq = factory.create(5, 2);
    CoordinateSequence copy;
    Coordinate coord;
    Coordinate[] array;
    
    initProgression(seq);
    assertEquals("xz", 2, seq.getDimension());
    assertTrue("Z", !seq.hasZ());
    assertTrue("M", !seq.hasM());
    coord = seq.getCoordinate(4);
    assertTrue( coord instanceof CoordinateXY);
    assertEquals( 4.0, coord.getX());
    assertEquals( 4.0, coord.getY());
    array = seq.toCoordinateArray();
    assertEquals(coord, array[4]);
    assertTrue(isEqual(seq,array));
    copy = factory.create(array);
    assertTrue(isEqual(copy,array));
    copy = factory.create(seq);
    assertTrue(isEqual(copy,array));
    
    seq = factory.create(5, 3);
    initProgression(seq);
    assertEquals("xyz", 3, seq.getDimension());
    assertTrue("Z", seq.hasZ());
    assertTrue("M", !seq.hasM());
    coord = seq.getCoordinate(4);
    assertTrue( coord.getClass() == Coordinate.class);
    assertEquals( 4.0, coord.getX());
    assertEquals( 4.0, coord.getY());
    assertEquals( 4.0, coord.getZ());
    array = seq.toCoordinateArray();
    assertEquals(coord, array[4]);
    assertTrue(isEqual(seq,array));
    copy = factory.create(array);
    assertTrue(isEqual(copy,array));
    copy = factory.create(seq);
    assertTrue(isEqual(copy,array));
    
    seq = factory.create(5, 3, 1);
    initProgression(seq);
    assertEquals("xym", 3, seq.getDimension());
    assertTrue("Z", !seq.hasZ());
    assertTrue("M", seq.hasM());
    coord = seq.getCoordinate(4);
    assertTrue( coord instanceof CoordinateXYM);
    assertEquals( 4.0, coord.getX());
    assertEquals( 4.0, coord.getY());
    assertEquals( 4.0, coord.getM());
    array = seq.toCoordinateArray();
    assertEquals(coord, array[4]);
    assertTrue(isEqual(seq,array));
    copy = factory.create(array);
    assertTrue(isEqual(copy,array));
    copy = factory.create(seq);
    assertTrue(isEqual(copy,array));
    
    seq = factory.create(5, 4, 1);
    initProgression(seq);
    assertEquals("xyzm", 4, seq.getDimension());
    assertTrue("Z", seq.hasZ());
    assertTrue("M", seq.hasM());
    coord = seq.getCoordinate(4);
    assertTrue( coord instanceof CoordinateXYZM);
    assertEquals( 4.0, coord.getX());
    assertEquals( 4.0, coord.getY());
    assertEquals( 4.0, coord.getZ());
    assertEquals( 4.0, coord.getM());
    array = seq.toCoordinateArray();
    assertEquals(coord, array[4]);
    assertTrue(isEqual(seq,array));
    copy = factory.create(array);
    assertTrue(isEqual(copy,array));
    copy = factory.create(seq);
    assertTrue(isEqual(copy,array));
    
    try {
      seq = factory.create(5, 2, 1);
      assertEquals(3,seq.getDimension());
      assertEquals(1,seq.getMeasures());
      fail("xm not supported");
    } catch (IllegalArgumentException expected) {
    }    
  }
 
  public void testMixedCoordinates()
  {
    CoordinateSequenceFactory factory = getCSFactory();
    Coordinate coord1 = new Coordinate(1.0,1.0,1.0);
    CoordinateXY coord2 = new CoordinateXY(2.0,2.0);
    CoordinateXYM coord3 = new CoordinateXYM(3.0,3.0,3.0);
    Coordinate[] array = new Coordinate[] {coord1, coord2, coord3};
    CoordinateSequence seq = factory.create(array);
    assertEquals( 3, seq.getDimension());
    assertEquals( 1, seq.getMeasures());
    assertTrue( coord1.equals( seq.getCoordinate(0)));
    assertTrue( coord2.equals( seq.getCoordinate(1)));
    assertTrue( coord3.equals( seq.getCoordinate(2)));   
  }
  
  private void initProgression(CoordinateSequence seq) {
    for (int index = 0; index < seq.size(); index++) {
       for( int ordinateIndex = 0; ordinateIndex < seq.getDimension(); ordinateIndex++) {
         seq.setOrdinate(index, ordinateIndex, (double) index);
       }
    }
  }
}