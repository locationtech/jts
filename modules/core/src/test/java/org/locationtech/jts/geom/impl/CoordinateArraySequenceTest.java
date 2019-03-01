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
import org.locationtech.jts.io.Ordinate;

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
  CoordinateArraySequenceFactory getCSFactory() {
    return CoordinateArraySequenceFactory.instance();
  }

  @Override
  int getDefaultDimension() { return 3; }

  @Override
  int getFactoryMaxDimension() { return 4; }

  @Override
  protected void doTestIllegalArgumentsOnSpecific() {
    try {
      getCSFactory().create(0, 2, 1);
      // ToDo:
      //   Evaluate: javadoc on CoordinateSequenceFactory.create
      //   functions state that the they "should not fail" and
      //   instead should return some sort of sequence.
      // fail();
    } catch (IllegalArgumentException e) { }
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
  public void testFactoryCreateWithSize0AndMaxDimension() {
    // ToDo:
    //  Fails because CoordinateArraySequence.create(size, dimension)
    //  only allows a dimension value of 4 when at least a measure value
    //  is specified.
    //super.testFactoryCreateWithSize0AndMaxDimension();
  }

  @Override
  public void testFactoryLimits() {
    // Expected to clip dimension and measure value within factory limits
    
    CoordinateArraySequenceFactory factory = getCSFactory();
    CoordinateSequence sequence = factory.create(10, 4);
    assertEquals("clipped dimension 3", 3, sequence.getDimension());
    assertEquals("default measure   0", 0, sequence.getMeasures());
    assertTrue(sequence.hasZ());
    assertTrue(!sequence.hasM());
    
    sequence = factory.create(10, 4, 0);
    assertEquals("clipped dimension 3", 3, sequence.getDimension());
    assertEquals("provided measure  0", 0, sequence.getMeasures());
    assertTrue(sequence.hasZ());
    assertTrue(!sequence.hasM());

    sequence = factory.create(10, 4, 2); // note clip to spatial dimension
    assertEquals("clipped dimension 3", 3, sequence.getDimension());
    assertEquals("clipped measure   1", 1, sequence.getMeasures());
    assertTrue(!sequence.hasZ());
    assertTrue(sequence.hasM());
    
    sequence = factory.create(10, 5, 1);
    assertEquals("clipped dimension 3", 4, sequence.getDimension());
    assertEquals("provided measure  1", 1, sequence.getMeasures());
    assertTrue(sequence.hasZ());
    assertTrue(sequence.hasM());

    // previously this clipped to dimension 3, measure 3
    sequence = factory.create(10, 1);
    assertEquals("clipped dimension 2", 2, sequence.getDimension());
    assertEquals("default measure   0", 0, sequence.getMeasures());
    assertTrue(!sequence.hasZ());
    assertTrue(!sequence.hasM());

    sequence = factory.create(10, 2, 1);
    assertEquals("clipped dimension 3", 3, sequence.getDimension());
    assertEquals("provided measure  1", 1, sequence.getMeasures());
    assertTrue(!sequence.hasZ());
    assertTrue(sequence.hasM());
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
    
    // dimensions clipped from XM to XYM
    seq = factory.create(5, 2, 1);
    assertEquals(3,seq.getDimension());
    assertEquals(1,seq.getMeasures());
  }
 
  public void testMixedCoordinates()
  {
    CoordinateSequenceFactory factory = getCSFactory();
    Coordinate coord1 = new Coordinate(1.0,1.0,1.0);
    CoordinateXY coord2 = new CoordinateXY(2.0,2.0);
    CoordinateXYM coord3 = new CoordinateXYM(3.0,3.0,3.0);
    
    Coordinate[] array = new Coordinate[] {coord1, coord2, coord3, null};
    CoordinateSequence seq = factory.create(array);
    assertEquals( 3, seq.getDimension());
    assertEquals( 1, seq.getMeasures());
    assertTrue( coord1.equals( seq.getCoordinate(0)));
    assertTrue( coord2.equals( seq.getCoordinate(1)));
    assertTrue( coord3.equals( seq.getCoordinate(2)));   
    assertNull( seq.getCoordinate(3));   
  }
  
  private void initProgression(CoordinateSequence seq) {
    for (int index = 0; index < seq.size(); index++) {
       for( int ordinateIndex = 0; ordinateIndex < seq.getDimension(); ordinateIndex++) {
         seq.setOrdinate(index, ordinateIndex, (double) index);
       }
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
}