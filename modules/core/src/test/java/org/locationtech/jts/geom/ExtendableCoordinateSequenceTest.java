/*
 * Copyright (c) 2018 Felix Obermaier.
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
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.geom.impl.CoordinateSequenceTestBase;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

import java.io.IOException;


/**
 * Unit test class for {@link ExtendableCoordinateSequence}
 */
public class ExtendableCoordinateSequenceTest extends TestCase {

  public static void main(String[] args){
    TestRunner.run(ExtendableCoordinateSequenceTest.class);
  }

  public ExtendableCoordinateSequenceTest(String name) {
    super(name);
  }

  public void testConstructor() {

    CoordinateSequence seq =
      new ExtendableCoordinateSequence(2);
    assertNotNull(seq);
    assertEquals(2, seq.getDimension());
    assertEquals(0, seq.size());
    assertEquals(ExtendableCoordinateSequence.INITIAL_CAPACITY,
            ((ExtendableCoordinateSequence)seq).getCapacity());

    doTestConstructor(null, 2);
    doTestConstructor(null, 2, 7);
    doTestConstructor(CoordinateArraySequenceFactory.instance(), 2);
    doTestConstructor(CoordinateArraySequenceFactory.instance(), 3);
    doTestConstructor(CoordinateArraySequenceFactory.instance(), 3, 0);
    doTestConstructor(CoordinateArraySequenceFactory.instance(), 3, 7);
    doTestConstructor(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 2);
    doTestConstructor(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 3);
    doTestConstructor(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 4, 7);
  }

  public void testExtend() {

    doTestExtend(CoordinateArraySequenceFactory.instance(), 2);
    doTestExtend(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 4);

  }

  public void testTruncated() {

    doTestTruncated(CoordinateArraySequenceFactory.instance(), 2);
    doTestTruncated(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 4);

  }

  public void testCopy() {

    doTestCopy(CoordinateArraySequenceFactory.instance(), 2);
    doTestCopy(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 4);
    doTestCopy(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 4);

  }

  public void testAdd() {

    doTestAdd(CoordinateArraySequenceFactory.instance(), 2);
    doTestAdd(CoordinateArraySequenceFactory.instance(), 3);
    doTestAdd(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 2);
    doTestAdd(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 3);
    doTestAdd(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 4);

  }

  public void testInsert() {

    doTestInsert(CoordinateArraySequenceFactory.instance(), 2);
    doTestInsert(CoordinateArraySequenceFactory.instance(), 3);
    doTestInsert(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 2);
    doTestInsert(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 3);
    doTestInsert(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 4);

  }

  public void testToCoordinateSequence() {

    doTestCoordinateSequence(CoordinateArraySequenceFactory.instance(), 2);
    doTestCoordinateSequence(CoordinateArraySequenceFactory.instance(), 3);
    doTestCoordinateSequence(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 2);
    doTestCoordinateSequence(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 3);
    doTestCoordinateSequence(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 4);
    doTestCoordinateSequence(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 4);
  }

  public void testSerialization() throws IOException, ClassNotFoundException {
    doTestSerialization(CoordinateArraySequenceFactory.instance(), 2);
    doTestSerialization(CoordinateArraySequenceFactory.instance(), 3);
    doTestSerialization(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 2);
    doTestSerialization(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 3);
    doTestSerialization(PackedCoordinateSequenceFactory.DOUBLE_FACTORY, 4);
    doTestSerialization(PackedCoordinateSequenceFactory.FLOAT_FACTORY, 4);
  }

  private static void doTestConstructor(CoordinateSequenceFactory csf, int dimension) {

    // arrange
    ExtendableCoordinateSequence eseq = null;

    // act
    try {
      eseq = new ExtendableCoordinateSequence(csf, dimension);
      if (csf == null) fail();
    } catch (IllegalArgumentException e) {
      // nothing to do here, this was expected!
    }

    if (csf == null) return;

    // assert
    assertNotNull(eseq);
    assertEquals(0, eseq.size());
    assertEquals(dimension, eseq.getDimension());
    assertEquals(ExtendableCoordinateSequence.INITIAL_CAPACITY, eseq.getCapacity());

  }
  private static void doTestConstructor(CoordinateSequenceFactory csf, int dimension, int capacity) {

    // arrange
    ExtendableCoordinateSequence eseq = null;
    // act
    try {
      eseq = new ExtendableCoordinateSequence(csf, capacity, dimension);
      if (csf == null || capacity < 1) fail();
    } catch (IllegalArgumentException e) {

    }

    // assert
    if (csf == null || capacity < 1) {
      assertNull(eseq);
      return;
    }

    assertNotNull(eseq);
    assertEquals(0, eseq.size());
    assertEquals(dimension, eseq.getDimension());
    assertEquals(capacity, eseq.getCapacity());
  }

  private static void doTestExtend(CoordinateSequenceFactory csf, int dimension) {

    // arrange
    ExtendableCoordinateSequence eseq = new ExtendableCoordinateSequence(csf, dimension);

    int capacity = eseq.getCapacity();
    int currentCapacity = capacity;
    int count = (int) (2.5 * capacity);
    for (int i = 0; i < count; i++) {
      eseq.setOrdinate(i, CoordinateSequence.X, i * 10 + 1);
      eseq.setOrdinate(i, CoordinateSequence.Y, i * 10 + 2);

      assertEquals(i + 1, eseq.size());
      if (eseq.size() > currentCapacity)
        currentCapacity += (currentCapacity >> 1);
      assertEquals(currentCapacity, eseq.getCapacity());
    }

    assertEquals(count, eseq.size());
    currentCapacity += (currentCapacity >> 1);
    currentCapacity += (currentCapacity >> 1);
    eseq.setOrdinate(currentCapacity + 3, CoordinateSequence.X, (currentCapacity + 3)*10 + 1);
    eseq.setOrdinate(currentCapacity + 3, CoordinateSequence.Y, (currentCapacity + 3)*10 + 2);

    assertEquals(currentCapacity + 4, eseq.getCapacity());
    assertEquals(currentCapacity + 4, eseq.size());
  }

  private static ExtendableCoordinateSequence create(CoordinateSequenceFactory csf, int dimension, int size) {

    ExtendableCoordinateSequence eseq = new ExtendableCoordinateSequence(csf, dimension);
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < dimension; j++)
        eseq.setOrdinate(i, j, i * 10d + j + 1);
    }
    return eseq;

  }

  private static void doTestCopy(CoordinateSequenceFactory csf, int dimension)
  {
    ExtendableCoordinateSequence eseq = create(csf, dimension, 6);

    CoordinateSequence eseqCopy = eseq.copy();
    assertNotNull(eseqCopy);
    assertTrue(eseqCopy instanceof ExtendableCoordinateSequence);
    assertEquals(eseq.size(), eseqCopy.size());
    assertEquals(eseq.getDimension(), eseqCopy.getDimension());
    assertEquals(eseq.getCapacity(), ((ExtendableCoordinateSequence)eseqCopy).getCapacity());

    assertTrue(CoordinateSequences.isEqual(eseq, eseqCopy));

    eseqCopy = (CoordinateSequence) eseq.clone();
    assertNotNull(eseqCopy);
    assertTrue(eseqCopy instanceof ExtendableCoordinateSequence);
    assertEquals(eseq.size(), eseqCopy.size());
    assertEquals(eseq.getDimension(), eseqCopy.getDimension());
    assertEquals(eseq.getCapacity(), ((ExtendableCoordinateSequence)eseqCopy).getCapacity());

    assertTrue(CoordinateSequences.isEqual(eseq, eseqCopy));

  }

  private static void doTestTruncated(CoordinateSequenceFactory csf, int dimension)
  {
    ExtendableCoordinateSequence eseq = create(csf, dimension, 8);

    CoordinateSequence seqTruncated = eseq.truncated();
    assertNotNull(seqTruncated);
    assertEquals(8, seqTruncated.size());
    assertEquals(eseq.getDimension(), seqTruncated.getDimension());

    assertTrue(CoordinateSequences.isEqual(eseq, seqTruncated));
  }

  private static void doTestAdd(CoordinateSequenceFactory csf, int dimension) {

    // arrange
    ExtendableCoordinateSequence eseq = create(csf, dimension, 8);

    // act
    switch (dimension) {
      case 2:
        eseq.add(-1, -2);
        break;
      case 3:
        eseq.add(-1, -2, -3);
        break;
      case 4:
        eseq.add(-1, -2, -3, -4);
        break;
    }
    eseq.add(new Coordinate(-11, -12));

    // assert
    assertEquals(10, eseq.size());
    assertEquals(-1d, eseq.getOrdinate(8, CoordinateSequence.X));
    assertEquals(-2d, eseq.getOrdinate(8, CoordinateSequence.Y));
    if (dimension>2)
      assertEquals(-3d, eseq.getOrdinate(8, CoordinateSequence.Z));
    if (dimension>3)
      assertEquals(-4d, eseq.getOrdinate(8, CoordinateSequence.M));

    assertEquals(-11d, eseq.getOrdinate(9, CoordinateSequence.X));
    assertEquals(-12d, eseq.getOrdinate(9, CoordinateSequence.Y));
    if (dimension>2)
      assertTrue(Double.isNaN(eseq.getOrdinate(9, CoordinateSequence.Z)));
  }

  private static void doTestInsert(CoordinateSequenceFactory csf, int dimension) {

    // arrange
    ExtendableCoordinateSequence eseq = create(csf, dimension, 8);

    // act
    switch (dimension) {
      case 2:
        eseq.insertAt(2, -1, -2);
        break;
      case 3:
        eseq.insertAt(2,-1, -2, -3);
        break;
      case 4:
        eseq.insertAt(2,-1, -2, -3, -4);
        break;
    }
    eseq.insertAt(7, new Coordinate(-11, -12));

    // assert
    assertEquals(10, eseq.size());
    assertEquals(-1d, eseq.getOrdinate(2, CoordinateSequence.X));
    assertEquals(-2d, eseq.getOrdinate(2, CoordinateSequence.Y));
    if (dimension>2)
      assertEquals(-3d, eseq.getOrdinate(2, CoordinateSequence.Z));
    if (dimension>3)
      assertEquals(-4d, eseq.getOrdinate(2, CoordinateSequence.M));

    assertEquals(-11d, eseq.getOrdinate(7, CoordinateSequence.X));
    assertEquals(-12d, eseq.getOrdinate(7, CoordinateSequence.Y));
    if (dimension>2)
      assertTrue(Double.isNaN(eseq.getOrdinate(7, CoordinateSequence.Z)));
  }

  private static void doTestCoordinateSequence(CoordinateSequenceFactory csf, int dimension) {

    final int size = 5;
    ExtendableCoordinateSequence eseq = create(csf, dimension, size);

    Coordinate[] coordinates = eseq.toCoordinateArray();

    assertNotNull(coordinates);
    assertEquals(size, coordinates.length);
    Coordinate tmp = new Coordinate();
    for (int i = 0; i < coordinates.length; i++)
    {
      assertTrue(coordinates[i].equals2D(eseq.getCoordinate(i)));
      eseq.getCoordinate(i, tmp);
      assertTrue(coordinates[i].equals2D(tmp));
      assertTrue(coordinates[i].equals2D(eseq.getCoordinateCopy(i)));

      assertEquals(i * 10d + 1, eseq.getX(i));
      assertEquals(i * 10d + 1, eseq.getOrdinate(i, CoordinateSequence.X));
      assertEquals(i * 10d + 2, eseq.getY(i));
      assertEquals(i * 10d + 2, eseq.getOrdinate(i, CoordinateSequence.Y));

      for (int j = 2; j < eseq.getDimension(); j++)
        assertEquals(i * 10d + j+1, eseq.getOrdinate(i, j));
    }

    Envelope expEnv = new Envelope(1, (size-1)*10+1, 2, (size-1)*10+2);
    Envelope actEnv = new Envelope();
    eseq.expandEnvelope(actEnv);

    assertEquals(expEnv, actEnv);
    String seqText = eseq.toString();
    assertTrue(seqText.length() > 0);

    try {
      tmp = eseq.getCoordinate(eseq.getCapacity());
      fail();
    } catch (IllegalArgumentException e) {
      // This is expected
    }

    try {
      eseq.setOrdinate(-1, CoordinateSequence.X, -1);
      fail();
    } catch (IllegalArgumentException e) {
      // This is expected
    }
  }

  private static void doTestSerialization(CoordinateSequenceFactory csf, int dimension)
          throws IOException, ClassNotFoundException {

    final int size = 5;
    ExtendableCoordinateSequence eseq1 = create(csf, dimension, size);
    byte[] eseqBytes = CoordinateSequenceTestBase.serialize(eseq1);
    ExtendableCoordinateSequence eseq2 =
            (ExtendableCoordinateSequence)CoordinateSequenceTestBase.deserialize(eseqBytes);

    assertNotNull(eseq2);
    assertTrue(CoordinateSequences.isEqual(eseq1, eseq2));
    assertEquals(eseq1.getCapacity(), eseq2.getCapacity());

  }
}
