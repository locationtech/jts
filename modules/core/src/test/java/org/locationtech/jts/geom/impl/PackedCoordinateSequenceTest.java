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
 * Test {@link PackedCoordinateSequence}
 * @version 1.7
 */
public class PackedCoordinateSequenceTest
    extends CoordinateSequenceTestBase
{
  public static void main(String args[]) {
    TestRunner.run(PackedCoordinateSequenceTest.class);
  }

  public PackedCoordinateSequenceTest(String name)
  {
    super(name);
  }

  @Override
  CoordinateSequenceFactory getCSFactory() {
    return new PackedCoordinateSequenceFactory();
  }
  
  public void testDouble() {
    checkAll( PackedCoordinateSequenceFactory.DOUBLE_FACTORY );
  }
  
  public void testFloat() {
    checkAll( PackedCoordinateSequenceFactory.FLOAT_FACTORY) ;
  }
  
  public void checkAll(CoordinateSequenceFactory factory)
  {
    checkDim2(1, factory);
    checkDim2(5, factory);
    checkDim3(factory);
    checkDim3_M1(factory);
    checkDim4_M1(factory);
    checkDimInvalid(factory);
  }
  
  public void checkDim2(int size, CoordinateSequenceFactory factory)
  {
    CoordinateSequence seq = factory.create(size, 2);
    initProgression(seq);
    
    assertEquals("Dimension should be 2", 2, seq.getDimension());
    assertTrue("Z should not be present", !seq.hasZ());
    assertTrue("M should not be present", !seq.hasM());
    
    int indexLast = size - 1;
    double valLast = indexLast;
    
    Coordinate coord = seq.getCoordinate(indexLast);
    assertTrue( coord instanceof CoordinateXY);
    assertEquals( valLast, coord.getX());
    assertEquals( valLast, coord.getY());
    
    Coordinate[] array = seq.toCoordinateArray();
    assertEquals(coord, array[indexLast]);
    assertTrue(coord != array[indexLast]);
    assertTrue(isEqual(seq,array));
    
    CoordinateSequence copy = factory.create(array);
    assertTrue(isEqual(copy,array));
    
    CoordinateSequence copy2 = factory.create(seq);
    assertTrue(isEqual(copy2,array));
  }
  
  public void checkDim3(CoordinateSequenceFactory factory)
  {
    CoordinateSequence seq = factory.create(5, 3);
    initProgression(seq);
    
    assertEquals("Dimension should be 3", 3, seq.getDimension());
    assertTrue("Z should be present", seq.hasZ());
    assertTrue("M should not be present", !seq.hasM());
    
    Coordinate coord = seq.getCoordinate(4);
    assertTrue( coord.getClass() == Coordinate.class);
    assertEquals( 4.0, coord.getX());
    assertEquals( 4.0, coord.getY());
    assertEquals( 4.0, coord.getZ());
    
    Coordinate[] array = seq.toCoordinateArray();
    assertEquals(coord, array[4]);
    assertTrue(coord != array[4]);
    assertTrue(isEqual(seq, array));
    
    CoordinateSequence copy = factory.create(array);
    assertTrue(isEqual(copy, array));
    
    CoordinateSequence copy2 = factory.create(seq);
    assertTrue(isEqual(copy2, array));
  }
    
  public void checkDim3_M1(CoordinateSequenceFactory factory)
  { 
    CoordinateSequence seq = factory.create(5, 3, 1);
    initProgression(seq);   
    
    assertEquals("Dimension should be 3", 3, seq.getDimension());
    assertTrue("Z should not be present", !seq.hasZ());
    assertTrue("M should be present", seq.hasM());
    
    Coordinate coord = seq.getCoordinate(4);
    assertTrue( coord instanceof CoordinateXYM);
    assertEquals( 4.0, coord.getX());
    assertEquals( 4.0, coord.getY());
    assertEquals( 4.0, coord.getM());
    
    Coordinate[] array = seq.toCoordinateArray();
    assertEquals(coord, array[4]);
    assertTrue(coord != array[4]);
    assertTrue(isEqual(seq,array));
    
    CoordinateSequence copy = factory.create(array);
    assertTrue(isEqual(copy,array));
    
    CoordinateSequence copy2 = factory.create(seq);
    assertTrue(isEqual(copy2,array));
  }
  
  public void checkDim4_M1(CoordinateSequenceFactory factory)
  {
    CoordinateSequence seq = factory.create(5, 4, 1);
    initProgression(seq);
    
    assertEquals("Dimension should be 4", 4, seq.getDimension());
    assertTrue("Z should be present", seq.hasZ());
    assertTrue("M should be present", seq.hasM());
    
    Coordinate coord = seq.getCoordinate(4);
    assertTrue( coord instanceof CoordinateXYZM);
    assertEquals( 4.0, coord.getX());
    assertEquals( 4.0, coord.getY());
    assertEquals( 4.0, coord.getZ());
    assertEquals( 4.0, coord.getM());
    
    Coordinate[] array = seq.toCoordinateArray();
    assertEquals(coord, array[4]);
    assertTrue(coord != array[4]);
    assertTrue(isEqual(seq,array));
    
    CoordinateSequence copy = factory.create(array);
    assertTrue(isEqual(copy,array));
    
    CoordinateSequence copy2 = factory.create(seq);
    assertTrue(isEqual(copy2, array));    
  }  
  
  public void checkDimInvalid(CoordinateSequenceFactory factory)
  {
    try {
      CoordinateSequence seq = factory.create(5, 2, 1);
      fail("Dimension=2/Measure=1 (XM) not supported");
    } catch (IllegalArgumentException expected) {
    }    
  }
  
  private void initProgression(CoordinateSequence seq) {
    for (int index = 0; index < seq.size(); index++) {
       for( int ordinateIndex = 0; ordinateIndex < seq.getDimension(); ordinateIndex++) {
         seq.setOrdinate(index, ordinateIndex, (double) index);
       }
    }
  }

}