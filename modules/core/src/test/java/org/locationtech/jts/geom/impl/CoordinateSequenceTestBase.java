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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.geom.Envelope;


/**
 * General test cases for CoordinateSequences.
 * Subclasses can set the factory to test different kinds of CoordinateSequences.
 *
 * @version 1.7
 */
public abstract class CoordinateSequenceTestBase
     extends TestCase
{
  private static final int SIZE = 100;
  private static final int MAX_DIMENSION = 32 /*Integer.MAX_VALUE*/;
  private static final int MAX_ORDINATES = MAX_DIMENSION;

  /*public*/ static final int XFlag = 1;
  /*public*/ static final int YFlag = 2;
  /*public*/ static final int ZFlag = 4;
  /*public*/ static final int MFlag = 8;
  /*public*/ static final int XYFlag = XFlag | YFlag;
  /*public*/ static final int XYZFlag = XYFlag | ZFlag;
  /*public*/ static final int XYMFlag = XYFlag | MFlag;
  /*public*/ static final int XYZMFlag = XYZFlag | MFlag;

  public static void main(String args[]) {
    TestRunner.run(CoordinateSequenceTestBase.class);
  }

  CoordinateSequenceTestBase(String name) { super(name); }

  abstract CoordinateSequenceFactory getCSFactory();

  public void testConstructorDirect() {
    assertTrue(true);
  }

  abstract int getDefaultDimension();

  int getFactoryMaxDimension() {
    CoordinateSequence seq = getCSFactory().create(0, MAX_DIMENSION);
    return seq.getDimension();
  }

  public void testFactoryCreateWithNullCoordinateArray() {

    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq = factory.create((Coordinate[]) null);
    assertNotNull(seq);
    assertEquals(0, seq.size());
    assertEquals(getDefaultDimension(), seq.getDimension());
    assertEquals("()", seq.toString());
  }

  public void testFactoryCreateWithNullCoordianteSequence() {

    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq = factory.create((CoordinateSequence) null);
    assertNotNull(seq);
    assertEquals(0, seq.size());
    assertEquals(getDefaultDimension(), seq.getDimension());
    assertEquals("()", seq.toString());
  }

  public void testFactoryCreateWithSize0AndMaxDimension() {

    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq = factory.create(0, MAX_DIMENSION);
    assertNotNull(seq);
    assertEquals(0, seq.size());
    assertEquals(getFactoryMaxDimension(), seq.getDimension());
    assertEquals("()", seq.toString());
  }

  public void testFactoryCreateWithSizeAndDimension() {

    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq = factory.create(5, 2);
    assertNotNull(seq);
    assertEquals(5, seq.size());
    assertEquals(2, seq.getDimension());
    assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.X));
    assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.Y));
    if (seq instanceof CoordinateArraySequence) {
      assertTrue(Double.isNaN(seq.getOrdinate(0, CoordinateSequence.Z)));
      assertTrue(Double.isNaN(seq.getOrdinate(0, CoordinateSequence.M)));
    }

    seq = factory.create(5, 3);
    assertNotNull(seq);
    assertEquals(5, seq.size());
    assertEquals(3, seq.getDimension());
    assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.X));
    assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.Y));
    assertTrue(Double.isNaN(seq.getOrdinate(0, CoordinateSequence.Z)));
    if (seq instanceof CoordinateArraySequence) {
      assertTrue(Double.isNaN(seq.getOrdinate(0, CoordinateSequence.M)));
    }

    seq = factory.create(5, 4);
    assertNotNull(seq);
    assertEquals(5, seq.size());
    assertEquals(Math.min(getFactoryMaxDimension(), 4), seq.getDimension());
    assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.X));
    assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.Y));
    assertTrue(Double.isNaN(seq.getOrdinate(0, CoordinateSequence.Z)));
    assertTrue(Double.isNaN(seq.getOrdinate(0, CoordinateSequence.M)));
  }

  public void testFactoryCreateWithSequence() {

    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seqTest = createSequence(factory, 5);
    CoordinateSequence seq = factory.create(seqTest);

    assertNotNull(seq);
    assertEquals(5, seq.size());
    assertTrue(isEqual(seqTest, seq));
  }

  public void testFactoryCreateWithCoordinateArray()
  {
    Coordinate[] coords = createArray(SIZE);
    CoordinateSequence seq = getCSFactory().create(coords);
    assertTrue(isEqual(seq, coords));
  }

  public void testFactoryCreateWithCoordinateSequence()
  {
    doTestCreateByInitSequence(createSequence(getCSFactory(), 100));
    doTestCreateByInitSequence(createSequence(getCSFactory(), 100, XYZFlag));
    doTestCreateByInitSequence(createSequence(getCSFactory(), 100, XYMFlag, true));
  }

  public void testFactoryCreateExtensions() {
    assertTrue(true);
  }

  public void testZeroLength()
  {
    CoordinateSequence seq = getCSFactory().create(0, 3);
    assertTrue(seq.size() == 0);

    CoordinateSequence seq2 = getCSFactory().create((Coordinate[]) null);
    assertTrue(seq2.size() == 0);
  }

  public void testCreateBySizeAndModify()
  {
    Coordinate[] coords = createArray(SIZE);

    CoordinateSequence seq = getCSFactory().create(SIZE, 3);
    for (int i = 0; i < seq.size(); i++) {
      seq.setOrdinate(i, 0, coords[i].x);
      seq.setOrdinate(i, 1, coords[i].y);
      seq.setOrdinate(i, 2, coords[i].getZ());
    }

    assertTrue(isEqual(seq, coords));
  }

  public void test2DZOrdinate()
  {
    Coordinate[] coords = createArray(SIZE);

    CoordinateSequence seq = getCSFactory().create(SIZE, 2);
    for (int i = 0; i < seq.size(); i++) {
      seq.setOrdinate(i, 0, coords[i].x);
      seq.setOrdinate(i, 1, coords[i].y);
    }

    for (int i = 0; i < seq.size(); i++) {
      Coordinate p = seq.getCoordinate(i);
      assertTrue(Double.isNaN(p.getZ()));
    }
  }

  void doTestCreateByInitSequence(CoordinateSequence sequence)
  {
    if (sequence == null)
      return;

    CoordinateSequence seq = getCSFactory().create(sequence);
    assertTrue(isEqual(sequence, seq));
  }

  public void testCreateByInitAndCopy()
  {
    Coordinate[] coords = createArray(SIZE);
    CoordinateSequence seq = getCSFactory().create(coords);
    CoordinateSequence seq2 = getCSFactory().create(seq);
    assertTrue(isEqual(seq2, coords));
  }

  public void testCreateByInitAndCopySequence()
  {
    doTestCreateByInitAndCopySequence(createSequence(getCSFactory(), 100));
    doTestCreateByInitAndCopySequence(createSequence(getCSFactory(), 100, XYZFlag));
    doTestCreateByInitAndCopySequence(createSequence(getCSFactory(), 100, XYMFlag, true));
  }

  public void testToCoordinateArray() {
    CoordinateSequence seq = createSequence(getCSFactory(), XYFlag);
    Coordinate[] coords = seq.toCoordinateArray();
    assertTrue(isEqual(seq, coords));
  }

  void doTestCreateByInitAndCopySequence(CoordinateSequence sequence) {
    if (sequence == null)
      return;

    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq1 = factory.create(sequence);
    CoordinateSequence seq2 = factory.create(seq1);
    assertTrue(isNotSameButEqual(seq2, sequence));
  }

  public void testSerializable() throws IOException, ClassNotFoundException {
    Coordinate[] coords = createArray(SIZE);
    CoordinateSequence seq = getCSFactory().create(coords);
    // throws exception if not serializable
    byte[] data = serialize(seq);
    // check round-trip gives same data
    CoordinateSequence seq2 = deserialize(data);
    assertTrue(isEqual(seq2, coords));
  }
  
  private static byte[] serialize(CoordinateSequence seq) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(seq);
    oos.close();
    return bos.toByteArray();
  }

  private static CoordinateSequence deserialize(byte[] data) throws IOException, ClassNotFoundException {
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    ObjectInputStream ois = new ObjectInputStream(bais);
    Object o = ois.readObject();
    return (CoordinateSequence) o;
  }

  static Coordinate[] createArray(int size)
  {
    Coordinate[] coords = new Coordinate[size];
    for (int i = 0; i < size; i++) {
      double base = 2 * i;
      coords[i] = new Coordinate(base, base + 1, base + 2);
    }
    return coords;
  }

  /* not used
  boolean isAllCoordsEqual(CoordinateSequence seq, Coordinate coord)
  {
    for (int i = 0; i < seq.size(); i++) {
      if (! coord.equals(seq.getCoordinate(i)))  return false;

      if (coord.x != seq.getOrdinate(i, CoordinateSequence.X))  return false;
      if (coord.y != seq.getOrdinate(i, CoordinateSequence.Y))  return false;
      // this does not work for ordinate values equal Double.NaN
      if (coord.z != seq.getOrdinate(i, CoordinateSequence.Z))  return false;
      if (seq.hasZ()) {
        if (coord.getZ() != seq.getZ(i)) return false;
    }
      if (seq.hasM()) {
        if (coord.getM() != seq.getM(i)) return false;
      }
      if (seq.getDimension() > 2) {
        if (coord.getOrdinate(2) != seq.getOrdinate(i, 2)) return false;
      }
      if (seq.getDimension() > 3) {
        if (coord.getOrdinate(3) != seq.getOrdinate(i, 3)) return false;
      }
    }
    return true;
  }
  */

  /**
   * Tests for equality using all supported accessors,
   * to provides test coverage for them.
   * 
   * @param seq    a sequence
   * @param coords an array of coordinates
   * @return <code>true</code> if the ordinate values in the sequence match those in the
   * coordinate array.
   */
  boolean isEqual(CoordinateSequence seq, Coordinate[] coords)
  {
    if (seq.size() != coords.length) return false;

    // carefully get coordinate of the same type as the sequence
    Coordinate p = seq.createCoordinate();
    for (int i = 0; i < seq.size(); i++) {
      if (! coords[i].equals(seq.getCoordinate(i)))  return false;

      // Ordinate named getters
      if (!isEqual(coords[i].x,seq.getX(i))) return false;
      if (!isEqual(coords[i].y,seq.getY(i))) return false;
      if (seq.hasZ()) {
        if (!isEqual(coords[i].getZ(),seq.getZ(i))) return false;
      }
      if (seq.hasM()) {
        if (!isEqual(coords[i].getM(),seq.getM(i))) return false;
      }

      // Ordinate indexed getters
      if (!isEqual(coords[i].x,seq.getOrdinate(i, CoordinateSequence.X))) return false;
      if (!isEqual(coords[i].y,seq.getOrdinate(i, CoordinateSequence.Y))) return false;
      if (seq.getDimension() > 2) {
        if (!isEqual(coords[i].getOrdinate(2),seq.getOrdinate(i, 2))) return false;
      }
      if (seq.getDimension() > 3) {
        if (!isEqual(coords[i].getOrdinate(3),seq.getOrdinate(i, 3))) return false;
      }
      
      // Coordinate getter
      seq.getCoordinate(i, p);
      if (!isEqual(coords[i].x,p.x)) return false;
      if (!isEqual(coords[i].y,p.y)) return false;
      if (seq.hasZ()) {
        if (!isEqual(coords[i].getZ(),p.getZ())) return false;
    }
      if (seq.hasM()) {
        if (!isEqual(coords[i].getM(),p.getM())) return false;
      }
    }
    return true;
  }
  /**
   * Tests for equality using all supported accessors,
   * to provides test coverage for them. The type of
   * the {@link CoordinateSequence}s may differ.
   *
   * @param seq1  the first sequence
   * @param seq2  the second sequence
   * @return {@code true} if both sequences are equal in size, dimension and contents.
   */
  boolean isEqual(CoordinateSequence seq1, CoordinateSequence seq2) {
    return isEqual(seq1, seq2, 0d);
  }
  boolean isEqual(CoordinateSequence seq1, CoordinateSequence seq2, double tolerance)
  {
    if (seq1.size() != seq2.size()) return false;
    if (seq1.getDimension() != seq2.getDimension()) return false;

    for (int i = 0; i < seq1.size(); i++) {
      // Ordinate indexed getters
      for (int j = 0; j < seq1.getDimension(); j++) {
        double o1 = seq1.getOrdinate(i, j);
        double o2 = seq2.getOrdinate(i, j);
        if (Double.isNaN(o1)) {
          if (!Double.isNaN(o2)) return false;
        } else if (Math.abs(o1 - o2) > tolerance)
          return false;
      }

      if (tolerance > 0d) continue;

      // Ordinate named getters
      if (seq1.getX(i) != seq2.getX(i))  return false;
      if (seq1.getY(i) != seq2.getY(i))  return false;

      // Get Coordinate
      if (!seq1.getCoordinate(i).equals3D(seq2.getCoordinate(i)))
        return false;

      // Get Coordinate copy
      Coordinate cc1 = seq1.getCoordinateCopy(i);
      Coordinate cc2 = seq2.getCoordinateCopy(i);
      if (!cc1.equals3D(cc2)) return false;

      // Get Coordinate as out argument
      cc1 = new Coordinate(); seq1.getCoordinate(i, cc1);
      cc2 = new Coordinate(); seq2.getCoordinate(i, cc2);
      if (!cc1.equals3D(cc2)) return false;
    }

    return true;
  }

  boolean isNotSameButEqual(CoordinateSequence seq1, CoordinateSequence seq2) {
    if (isSame(seq1, seq2))
      return false;
    return isEqual(seq1,seq2);
  }

  abstract boolean isSame(CoordinateSequence seq1, CoordinateSequence seq2);

  public void testZOrdinateIsNaN() {
    CoordinateSequence cs = getCSFactory().create(1, 3);
    assertTrue(Double.isNaN(cs.getOrdinate(0, CoordinateSequence.Z)));
  }

  public void testCopy()
  {
    CoordinateSequence csOrig = getCSFactory().create(9, 3);
    CoordinateSequence csCopy = csOrig.copy();
    assertTrue(isNotSameButEqual(csOrig, csCopy));
  }

  /** @deprecated */
  public void testClone() {
    CoordinateSequence csOrig = getCSFactory().create(9, 3);
    CoordinateSequence csClone = (CoordinateSequence) csOrig.clone();
    assertTrue(isNotSameButEqual(csOrig, csClone));
  }

  boolean isEqual( double expected, double actual) {
    return expected == actual || (Double.isNaN(expected)&&Double.isNaN(actual));
  }

  public void testExpandEnvelope() {
    CoordinateSequence cs = createSequence(getCSFactory(), 0);
    Envelope e = new Envelope();
    cs.expandEnvelope(e);
    assertTrue(e.isNull());
  }

  private static final int MAX_ORDINATES = 30;
  public static final int XFlag = 1;
    cs = createSequence(getCSFactory(), 5);
    cs.expandEnvelope(e = new Envelope());
    assertTrue(new Envelope(11, 51, 12, 52).equals(e));
  }

  /*public*/ static CoordinateSequence createSequence(CoordinateSequenceFactory factory, int size) {
    return createSequence(factory,size, XYFlag, false);
  }

  /*public*/  static CoordinateSequence createSequence(CoordinateSequenceFactory factory, int size, int ordinateFlag) {
    return createSequence(factory,size, ordinateFlag, false);
  }

  /*public*/ static CoordinateSequence createSequence(CoordinateSequenceFactory factory, int size, int ordinateFlag,
                                       boolean measureToZ) {

    if (size > 100)
      throw new IllegalArgumentException("size must not be greater than 100");

    // We always have x- and y-ordinates
    ordinateFlag |= XYFlag;
    int[] ordinateIndex = new int[MAX_ORDINATES];

    int dimension = 0;
    for (int i = 0; i < MAX_ORDINATES; i++) {
      if ((ordinateFlag & (1 << i)) != 0) {
        dimension = i;
      }
      ordinateIndex[i] = i;
    }

    // add one
    dimension = dimension + 1;
    if (dimension > 30)
      throw new IllegalArgumentException("ordinateFlag must evaluate to a dimension <= 30");

    // check if measureToZ can be applied
    if (measureToZ) {
      if ((ordinateFlag & ZFlag) == 0) {
        // ZFlag not set, move ordinate indices
        for (int i = 2; i < dimension; i++)
          ordinateIndex[i] = ordinateIndex[i + 1];
        dimension = dimension - 1;
      }
    }

    CoordinateSequence seq = null;
    try {
      seq = factory.create(size, dimension);
      for (int i = 0; i < size; i++)
      {
        for (int j = 0; j < dimension; j++) {
          if ((ordinateFlag &(1<<ordinateIndex[j])) != 0)
            seq.setOrdinate(i, j, (i+1) * 10 + ordinateIndex[j]+1);
          else
            seq.setOrdinate(i, j, Double.NaN);
        }
      }
    }
    catch (Throwable ex)
    {
      System.out.println("Failed to create sequence " +
              "of size " + size + " and ordinate flag " + ordinateFlagString(ordinateFlag) + " " +
              "using '" + factory.getClass().getSimpleName() + "'!");
    }
    return seq;
  }


  private static String ordinateFlagString(int ordinateFlag) {

    StringBuilder sb = new StringBuilder("XY");
    if ((ordinateFlag & ZFlag) == ZFlag) sb.append('Z');
    if ((ordinateFlag & MFlag) == MFlag) sb.append('M');

    StringBuilder sbOther = new StringBuilder();
    int lastIndex = -1;
    for (int i = 4; i < MAX_ORDINATES; i++) {
      if ((ordinateFlag & (1<<i)) != 0) {
        sbOther.append('1');
        lastIndex = i - 4;
      } else
        sbOther.append('0');
    }
    if (lastIndex > 0) {
      sb.append('|');
      sb.append(sbOther.toString().substring(0, lastIndex));
    }

    return sb.toString();
  }
}

