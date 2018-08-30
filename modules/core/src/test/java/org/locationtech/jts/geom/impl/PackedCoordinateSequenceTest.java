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

  int getType() { return PackedCoordinateSequenceFactory.DOUBLE; }

  @Override
  CoordinateSequenceFactory getCSFactory() {
    return new PackedCoordinateSequenceFactory(getType());
  }

  @Override
  int getDefaultDimension() { return 3; }

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
    assertTrue(coord != array[4]);
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
    assertTrue(coord != array[4]);
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
    assertTrue(coord != array[4]);
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
    assertTrue(coord != array[4]);
    assertTrue(isEqual(seq,array));
    copy = factory.create(array);
    assertTrue(isEqual(copy,array));
    copy = factory.create(seq);
    assertTrue(isEqual(copy,array));
    
    try {
      seq = factory.create(5, 2, 1);
      fail("xm not supported");
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

  @Override
  public void testFactoryCreateWithCoordinateSequence()
  {
    super.testFactoryCreateWithCoordinateSequence();
    doTestCreateByInitSequence(createSequence(getCSFactory(), 100, XYMFlag));
    doTestCreateByInitSequence(createSequence(getCSFactory(), 100, XYZMFlag, true));
  }

  @Override
  public void testCreateByInitAndCopySequence()
  {
    super.testCreateByInitAndCopySequence();
    doTestCreateByInitAndCopySequence(createSequence(getCSFactory(), 100, XYMFlag));
    doTestCreateByInitAndCopySequence(createSequence(getCSFactory(), 100, XYZMFlag, true));
  }

  public void testFactoryConstructor() {
    PackedCoordinateSequenceFactory factory;
    factory = new PackedCoordinateSequenceFactory();
    assertEquals(PackedCoordinateSequenceFactory.DOUBLE, factory.getType());
    factory = new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE);
    assertEquals(PackedCoordinateSequenceFactory.DOUBLE, factory.getType());
    factory = new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.FLOAT);
    assertEquals(PackedCoordinateSequenceFactory.FLOAT, factory.getType());

    try {
      factory = new PackedCoordinateSequenceFactory(2);
      fail();
    } catch (IllegalArgumentException e) {

    }
  }

  public void testMOrdinateIs0() {
    CoordinateSequence cs = getCSFactory().create(1, 3, 1);
    assertFalse(Double.isNaN(cs.getOrdinate(0, 2)));
    assertEquals(0d, cs.getM(0));
    assertEquals(0d, cs.getOrdinate(0, 2));
    cs = getCSFactory().create(1, 4, 1);
    assertFalse(Double.isNaN(cs.getOrdinate(0, CoordinateSequence.M)));
    assertEquals(0d, cs.getM(0));
    assertEquals(0d, cs.getOrdinate(0, 3));
  }

  /** @deprecated */
  public void testConstructorWithJustCoordinateArray() {
    if (getType() == PackedCoordinateSequenceFactory.DOUBLE) {
      Coordinate[] coords = createArray(5);
      CoordinateSequence seq = new PackedCoordinateSequence.Double(coords);
      assertNotNull(seq);
      assertEquals(5, seq.size());
      assertEquals(3, seq.getDimension());
      assertTrue(isEqual(seq, coords));
    }
  }

  public void testFailWithInsufficientDimension() {
    PackedCoordinateSequenceFactory factory = (PackedCoordinateSequenceFactory) getCSFactory();

    // dimension = 0
    try {
      factory.create(0, 0, 0);
      fail();
    } catch (IllegalArgumentException e) {

    }

    // dimension = 1
    try {
      factory.create(0, 1, 0);
      fail();
    } catch (IllegalArgumentException e) {

    }

    // dimension - measure >= 2
    try {
      factory.create(0, 3, 2);
      fail();
    } catch (IllegalArgumentException e) {

    }
  }

  public void testFactoryCreateByCoordinateArrayAndDimension() {
    PackedCoordinateSequenceFactory factory = (PackedCoordinateSequenceFactory)getCSFactory();


    // Create with array of Coordinates
    Coordinate[] coords = createArray(5); // Defaults to XYZFlag
    CoordinateSequence sequence = factory.create(coords);
    assertEquals(5, sequence.size());
    assertEquals(3, sequence.getDimension());
    assertEquals(0, sequence.getMeasures());
    assertTrue(checkOrdinates(coords, sequence));

    // Create with array of CoordinateXYs
    coords = createArray(5, XYFlag);
    sequence = factory.create(coords);
    assertEquals(5, sequence.size());
    assertEquals(2, sequence.getDimension());
    assertEquals(0, sequence.getMeasures());
    assertTrue(checkOrdinates(coords, sequence));

    // Create with array of CoordinateXYMs
    coords = createArray(5, XYMFlag);
    sequence = factory.create(coords);
    assertEquals(5, sequence.size());
    assertEquals(3, sequence.getDimension());
    assertEquals(1, sequence.getMeasures());
    assertTrue(checkOrdinates(coords, sequence));

    // Create with array of CoordinateXYMs
    coords = createArray(5, XYZMFlag);
    sequence = factory.create(coords);
    assertEquals(5, sequence.size());
    assertEquals(4, sequence.getDimension());
    assertEquals(1, sequence.getMeasures());
    assertTrue(checkOrdinates(coords, sequence));
  }

  public void testSetOrdinateInvalidatesCachedCoordinateArray() {

    PackedCoordinateSequence seq = (PackedCoordinateSequence) getCSFactory().create(1, 3);

    Coordinate[] coords, compare = null;
    compare = seq.toCoordinateArray();
    seq.setX(0, 1d);
    coords = seq.toCoordinateArray();
    assertTrue(coords != compare);

    compare = coords;
    seq.setY(0, 2d);
    coords = seq.toCoordinateArray();
    assertTrue(coords != compare);

    compare = coords;
    seq.setOrdinate(0, 2, 3d);
    coords = seq.toCoordinateArray();
    assertTrue(coords != compare);

    compare = coords;
    coords = seq.toCoordinateArray();
    assertTrue(coords == compare);

    assertEquals(new Coordinate(1, 2, 3), coords[0]);

    if (seq instanceof PackedCoordinateSequence.Float) {
      float[] raw = ((PackedCoordinateSequence.Float)seq).getRawCoordinates();
      assertNotNull(raw);
      assertEquals(3, raw.length);
      assertEquals(1f, raw[0]);
      assertEquals(2f, raw[1]);
      assertEquals(3f, raw[2]);
    } else if (seq instanceof PackedCoordinateSequence.Double) {
      double[] raw = ((PackedCoordinateSequence.Double)seq).getRawCoordinates();
      assertNotNull(raw);
      assertEquals(3, raw.length);
      assertEquals(1d, raw[0]);
      assertEquals(2d, raw[1]);
      assertEquals(3d, raw[2]);
    }

    // make sure cache is created
    coords = seq.toCoordinateArray();
    Coordinate c1 = seq.getCoordinate(0);
    Coordinate c2 = seq.getCoordinate(0);
    assertTrue(c1 == c2);

  }

  @Override
  public void testFactoryCreateExtensions() {
    super.testFactoryCreateExtensions();

    PackedCoordinateSequenceFactory factory = (PackedCoordinateSequenceFactory)getCSFactory();
    PackedCoordinateSequence seq;

    try {
      seq = (PackedCoordinateSequence)factory.create(new double[0], 0);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    seq = (PackedCoordinateSequence)factory.create(new double[0], 2);
    assertNotNull(seq);
    assertEquals(0, seq.size());
    assertEquals(2, seq.getDimension());

    seq = (PackedCoordinateSequence)factory.create(new double[] {1d, 2d}, 2);
    assertNotNull(seq);
    assertEquals(1, seq.size());
    assertEquals(2, seq.getDimension());
    assertEquals(1d, seq.getX(0));
    assertEquals(2d, seq.getY(0));

    seq = (PackedCoordinateSequence)factory.create(new float[] {1f, 2f}, 2);
    assertNotNull(seq);
    assertEquals(1, seq.size());
    assertEquals(2, seq.getDimension());
    assertEquals(1d, seq.getX(0));
    assertEquals(2d, seq.getY(0));

    try {
      factory.create(new double[] {1d, 2d, 3d}, 2);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      factory.create(new double[] {1d, 2d, 3d}, 0);
      fail();
    }
    catch (IllegalArgumentException e) {
    }    try {
      factory.create(new float[] {1f, 2f, 3f}, 2);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    try {
      factory.create(new double[] {1d, 2d, 3d}, 0);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    // Setting/getting type
    int type = factory.getType();
    factory.setType(PackedCoordinateSequenceFactory.FLOAT);
    assertEquals(PackedCoordinateSequenceFactory.FLOAT, factory.getType());
    factory.setType(PackedCoordinateSequenceFactory.DOUBLE);
    assertEquals(PackedCoordinateSequenceFactory.DOUBLE, factory.getType());
    try {
      factory.setType(3);
      fail();
    } catch (IllegalArgumentException e) {

    }

    // restore
    factory.setType(type);

    assertEquals(PackedCoordinateSequenceFactory.DOUBLE,
                 PackedCoordinateSequenceFactory.DOUBLE_FACTORY.getType());
    assertEquals(PackedCoordinateSequenceFactory.FLOAT,
                 PackedCoordinateSequenceFactory.FLOAT_FACTORY.getType());

    // Creating Double from Float or vice versa
    PackedCoordinateSequenceFactory factory2 = (PackedCoordinateSequenceFactory) getCSFactory();
    factory2.setType(type == PackedCoordinateSequenceFactory.DOUBLE ?
            PackedCoordinateSequenceFactory.FLOAT : PackedCoordinateSequenceFactory.DOUBLE);

    CoordinateSequence seq1 = createSequence(factory, 7, XYZMFlag);
    CoordinateSequence seq2 = factory2.create(seq1);
    assertTrue(isEqual(seq1, seq2, 1e-5d));
  }



  private static boolean checkOrdinates(Coordinate[] coords, CoordinateSequence sequence) {

    if (coords.length != sequence.size())
      return false;

    int dimension = sequence.getDimension();
    for (int i = 0; i < coords.length; i++) {
      if (coords[i].x != sequence.getX(i)) return false;
      if (dimension == 1) continue;

      if (coords[i].y != sequence.getY(i)) return false;
      if (dimension == 2) continue;

      if (coords[i] instanceof CoordinateXY) {
        if (i == 0) assertFalse(sequence.hasZ());
        assertTrue(java.lang.Double.isNaN(sequence.getZ(i)));
        if (i == 0) assertFalse(sequence.hasM());
        assertTrue(java.lang.Double.isNaN(sequence.getM(i)));
      }
      else if (coords[i] instanceof CoordinateXYM) {
        if (i == 0) assertFalse(sequence.hasZ());
        assertTrue(java.lang.Double.isNaN(sequence.getZ(i)));
        if (i == 0) assertTrue(sequence.hasM());
        assertFalse(java.lang.Double.isNaN(sequence.getM(i)));
      }
      else if (coords[i] instanceof CoordinateXYZM) {
        if (i == 0) assertTrue(sequence.hasZ());
        assertEquals(coords[i].getZ(), sequence.getZ(i));
        if (i == 0) assertTrue(sequence.hasM());
        assertEquals(coords[i].getM(), sequence.getM(i));
      }
      else {
        if (i == 0) assertTrue(sequence.hasZ());
        assertEquals(coords[i].getZ(), sequence.getZ(i));
        if (i == 0) assertFalse(sequence.hasM());
        assertTrue(java.lang.Double.isNaN(sequence.getM(i)));
      }
    }
    return true;
  }

  @Override
  boolean isSame(CoordinateSequence seq1, CoordinateSequence seq2) {

    if (seq1 == seq2)
      return true;


    if (((PackedCoordinateSequence.Double)seq1).getRawCoordinates() ==
        ((PackedCoordinateSequence.Double)seq2).getRawCoordinates())
      return true;

    return false;
  }

}