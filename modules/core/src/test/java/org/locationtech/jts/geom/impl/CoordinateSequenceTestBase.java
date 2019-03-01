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
import java.util.EnumSet;

import org.locationtech.jts.geom.*;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.io.Ordinate;
import org.locationtech.jts.util.Assert;


/**
 * General test cases for CoordinateSequences.
 * Subclasses can set the factory to test different kinds of CoordinateSequences.
 *
 * @version 1.7
 */
public abstract class CoordinateSequenceTestBase
     extends TestCase
{
  public static final int SIZE = 100;

  public static void main(String args[]) {
    TestRunner.run(CoordinateSequenceTestBase.class);
  }

  public CoordinateSequenceTestBase(String name) { super(name); }

  /**
   * Gets the {@link CoordinateSequenceFactory} to use for the tests
   *
   * @return A factory to create {@link CoordinateSequence}s.
   */
  abstract CoordinateSequenceFactory getCSFactory();

  /**
   * Gets the default dimension for {@link CoordinateSequence}s created with {@link #getCSFactory()}.
   *
   * @return A number of dimensions
   */
  abstract int getDefaultDimension();

  /**
   * Gets the maximum possible dimension for {@link CoordinateSequence}s created with {@link #getCSFactory()}.
   *
   * @return A number of dimensions
   */
  abstract int getFactoryMaxDimension();



  public void testConstructorDirect() {
    assertTrue(true);
  }

  public void testFactoryCreateWithNullCoordinateArray() {

    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq = factory.create((Coordinate[]) null);
    assertNotNull(seq);
    assertEquals(0, seq.size());
    assertEquals(getDefaultDimension(), seq.getDimension());
    assertEquals("()", seq.toString());
  }

  public void testFactoryCreateWithNullCoordinateSequence() {

    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq = factory.create((CoordinateSequence) null);
    assertNotNull(seq);
    assertEquals(0, seq.size());
    assertEquals(getDefaultDimension(), seq.getDimension());
    assertEquals("()", seq.toString());
  }

  public void testFactoryCreateWithSize0AndMaxDimension() {

    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq = factory.create(0, getFactoryMaxDimension());
    assertNotNull(seq);
    assertEquals(0, seq.size());
    assertEquals(getFactoryMaxDimension(), seq.getDimension());
    assertEquals("()", seq.toString());
  }

  public void testFactoryCreateWithSizeAndDimension() {

    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq = factory.create(5, 2);
    assertNotNull(seq);
    assertTrue(seq.getCoordinate(0) instanceof CoordinateXY);
    assertEquals(5, seq.size());
    assertEquals(2, seq.getDimension());
    assertEquals(0, seq.getMeasures());
    assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.X));
    assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.Y));
    if (seq instanceof CoordinateArraySequence) {
      assertTrue(Double.isNaN(seq.getZ(0)));
      assertTrue(Double.isNaN(seq.getM(0)));
    }

    seq = factory.create(5, 3);
    assertNotNull(seq);
    //assertTrue(seq.getCoordinate(0) instanceof Coordinate);
    assertNotNull(seq.getCoordinate(0));
    assertTrue(!(seq.getCoordinate(0) instanceof CoordinateXY));
    assertTrue(!(seq.getCoordinate(0) instanceof CoordinateXYM));
    assertTrue(!(seq.getCoordinate(0) instanceof CoordinateXYZM));
    assertEquals(5, seq.size());
    assertEquals(3, seq.getDimension());
    assertEquals(0, seq.getMeasures());
    assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.X));
    assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.Y));
    assertTrue(Double.isNaN(seq.getOrdinate(0, CoordinateSequence.Z)));
    if (seq instanceof CoordinateArraySequence) {
      assertTrue(Double.isNaN(seq.getM(0)));
    }

    seq = factory.create(5, 4);
    if (seq.getDimension() == 4) {
      assertNotNull(seq);
      assertEquals(5, seq.size());
      assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.X));
      assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.Y));
      assertTrue(Double.isNaN(seq.getZ(0)));
      assertTrue(Double.isNaN(seq.getOrdinate(0, CoordinateSequence.Z)));
      if (seq.hasM())
        assertEquals(0d, seq.getM(0));
      assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.M));
    }

    seq = factory.create(5, 4, 1);
    if (seq.getDimension() == 4) {
      assertNotNull(seq);
      assertEquals(5, seq.size());
      assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.X));
      assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.Y));
      assertTrue(Double.isNaN(seq.getZ(0)));
      assertTrue(Double.isNaN(seq.getOrdinate(0, CoordinateSequence.Z)));
      if (seq.hasM())
        assertEquals(0d, seq.getM(0));
      assertEquals(0d, seq.getOrdinate(0, CoordinateSequence.M));
    }
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
    Coordinate[] coords;
    CoordinateSequence seq;

    coords = createArray(SIZE, Ordinate.createXY());
    seq = getCSFactory().create(coords);
    assertTrue(isEqual(seq, coords));

    coords = createArray(SIZE);
    seq = getCSFactory().create(coords);
    assertTrue(isEqual(seq, coords));

    coords = createArray(SIZE, Ordinate.createXYM());
    seq = getCSFactory().create(coords);
    assertTrue(isEqual(seq, coords));

    coords = createArray(SIZE, Ordinate.createXYZM());
    seq = getCSFactory().create(coords);
    assertTrue(isEqual(seq, coords));

  }

  public void testFactoryCreateWithCoordinateArrayAddingDimension()
  {
    Assert.isTrue(true);
  }

  public void testFactoryCreateWithCoordinateSequence()
  {
    doTestCreateByInitSequence(createSequence(getCSFactory(), 100, Ordinate.createXY()));
    doTestCreateByInitSequence(createSequence(getCSFactory(), 100));
    doTestCreateByInitSequence(createSequence(getCSFactory(), 100, Ordinate.createXYM()));
    doTestCreateByInitSequence(createSequence(getCSFactory(), 100, Ordinate.createXYZM()));
  }

  public void testFactoryCreateExtensions() {
    assertTrue(true);
  }

  public void testZeroLength()
  {
    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq;

    seq = factory.create(0, 3);
    assertNotNull(seq);
    assertEquals(0, seq.size());
    assertEquals(3, seq.getDimension());
    assertEquals(0, seq.getMeasures());

    seq = factory.create(0, 3, 1);
    assertNotNull(seq);
    assertEquals(0, seq.size());
    assertEquals(3, seq.getDimension());
    assertEquals(1, seq.getMeasures());

    seq = factory.create((Coordinate[]) null);
    assertNotNull(seq);
    assertEquals(0, seq.size());
    assertEquals(3, seq.getDimension());
    assertEquals(0, seq.getMeasures());

    seq = factory.create((CoordinateSequence) null);
    assertNotNull(seq);
    assertEquals(0, seq.size());
    assertEquals(3, seq.getDimension());
    assertEquals(0, seq.getMeasures());
  }

  public void testIllegalArguments()
  {
    try {
      getCSFactory().create(-3, 3);
      fail("negative sequence size does not throw IllegalArgumentException");
    }
    catch (IllegalArgumentException ignored) { }
    catch (NegativeArraySizeException e) {
      // sub-optimal?
      System.out.println("NegativeArraySizeException thrown (instead of IllegalArgumentException)");
    }

    // continue with specific tests
    doTestIllegalArgumentsOnSpecific();
  }

  protected void doTestIllegalArgumentsOnSpecific() {
    assertTrue(true);
  }

  public void testFactoryLimits()
  {
    assertTrue(true);
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

  public void testCreateByInit()
  {
    Coordinate[] coords = createArray(SIZE);
    CoordinateSequence seq = getCSFactory().create(coords);
    assertTrue(isEqual(seq, coords));
  }

  private void doTestCreateByInitSequence(CoordinateSequence sequence)
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
    doTestCreateByInitAndCopySequence(createSequence(getCSFactory(), 100, Ordinate.createXY()));
    doTestCreateByInitAndCopySequence(createSequence(getCSFactory(), 100));
    doTestCreateByInitAndCopySequence(createSequence(getCSFactory(), 100, Ordinate.createXYM()));
    doTestCreateByInitAndCopySequence(createSequence(getCSFactory(), 100, Ordinate.createXYZM()));
  }

  private void doTestCreateByInitAndCopySequence(CoordinateSequence sequence) {
    if (sequence == null)
      return;

    CoordinateSequenceFactory factory = getCSFactory();
    CoordinateSequence seq1 = factory.create(sequence);
    CoordinateSequence seq2 = factory.create(seq1);
    assertTrue(isNotSameButEqual(seq2, sequence));
  }

  public void testToCoordinateArray() {
    CoordinateSequence seq = createSequence(getCSFactory(), 5, Ordinate.createXY());
    Coordinate[] coords = seq.toCoordinateArray();
    assertTrue(isEqual(seq, coords));
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

  public void testExpandEnvelope() {
    CoordinateSequence cs = createSequence(getCSFactory(), 0);
    Envelope e = new Envelope();
    cs.expandEnvelope(e);
    assertTrue(e.isNull());

    cs = createSequence(getCSFactory(), 5);
    cs.expandEnvelope(e = new Envelope());
    assertEquals(new Envelope(11, 51, 12, 52), e);
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
    return createArray(size, Ordinate.createXYZ());
  }
  static Coordinate[] createArray(int size, EnumSet<Ordinate> ordinates)
  {
    Coordinate[] coords = new Coordinate[size];
    // Note: EnumSet sucks!
    for (int i = 0; i < size; i++) {
      double base = i * 10;
      if (ordinates.contains(Ordinate.Z) && ordinates.contains(Ordinate.M))
        coords[i] = new CoordinateXYZM(base, base + 1, base + 2, base + 3);
      else if (ordinates.contains(Ordinate.Z))
        coords[i] = new Coordinate(base, base + 1, base + 2);
      else if (ordinates.contains(Ordinate.M))
        coords[i] = new CoordinateXYM(base, base + 1, base + 3);
      else
        coords[i] = new CoordinateXY(base, base + 1);
    }
    return coords;
  }

  // not used
  /*
  boolean isAllCoordsEqual(CoordinateSequence seq, Coordinate coord)
  {
    for (int i = 0; i < seq.size(); i++) {
      if (!coord.equals(seq.getCoordinate(i))) return false;

      if (coord.x != seq.getOrdinate(i, CoordinateSequence.X)) return false;
      if (coord.y != seq.getOrdinate(i, CoordinateSequence.Y)) return false;
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
      if (!coords[i].equals(seq.getCoordinate(i))) return false;

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
  protected boolean isEqual(CoordinateSequence seq1, CoordinateSequence seq2) {
    return isEqual(seq1, seq2, 0d);
  }

  protected boolean isEqual(CoordinateSequence seq1, CoordinateSequence seq2, double tolerance)
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
      if (areCoordinatesDifferent(cc1, cc2, tolerance)) return false;

      // Get Coordinate as out argument
      cc1 = seq1.createCoordinate(); seq1.getCoordinate(i, cc1);
      cc2 = seq1.createCoordinate(); seq2.getCoordinate(i, cc2);
      if (areCoordinatesDifferent(cc1, cc2, tolerance)) return false;
    }

    return true;
  }

  private static boolean areCoordinatesDifferent(Coordinate cc1, Coordinate cc2, double tolerance) {

    if (cc1 == null && cc2 == null) return false;
    if (cc1 == null) return true;
    if (cc2 == null) return true;
    if (cc1.getClass() != cc2.getClass()) return true;

    if (cc1 instanceof CoordinateXYM && cc2 instanceof  CoordinateXYM) {
      if (areOrdinateValuesDifferent(cc1.getM(), cc2.getM(), tolerance))
        return true;
    }

    if (cc1 instanceof CoordinateXYZM && !(cc2 instanceof  CoordinateXY)) {
      if (areOrdinateValuesDifferent(cc1.getZ(), cc2.getZ(), tolerance))
        return true;
    }

    if (areOrdinateValuesDifferent(cc1.x, cc2.x, tolerance))
      return true;

    return areOrdinateValuesDifferent(cc1.y, cc2.y, tolerance);

  }

  private static boolean areOrdinateValuesDifferent(double o1, double o2, double tolerance) {
    if (java.lang.Double.isNaN(o1) ^ java.lang.Double.isNaN(o2))
      return true;

    if (java.lang.Double.isNaN(o1))
      return false;

    return !(Math.abs(o1 - o2) <= tolerance);
  }

  boolean isNotSameButEqual(CoordinateSequence seq1, CoordinateSequence seq2) {
    if (isSame(seq1, seq2))
      return false;
    return isEqual(seq1,seq2);
  }

  abstract boolean isSame(CoordinateSequence seq1, CoordinateSequence seq2);

  static boolean isEqual(double expected, double actual) {
    return expected == actual || (Double.isNaN(expected) && Double.isNaN(actual));
  }

  private static CoordinateSequence createSequence(CoordinateSequenceFactory factory, int size) {
    return createSequence(factory,size, EnumSet.of(Ordinate.X, Ordinate.Y, Ordinate.Z));
  }

  static CoordinateSequence createSequence(CoordinateSequenceFactory factory, int size,
                                           EnumSet<Ordinate> ordinates)
  {
    if (size > 100)
      throw new IllegalArgumentException("size *must not* be greater than 100");

    // We always have x- and y-ordinates
    int dimension = 2;
    int measures = 0;
    if (ordinates.contains(Ordinate.Z)) dimension++;
    if (ordinates.contains(Ordinate.M)) { dimension++; measures++; }

    CoordinateSequence seq = null;
    try {
      seq = factory.create(size, dimension, measures);
      for (int i = 0; i < size; i++)
      {
        for (int j = 0; j < dimension; j++) {
          seq.setOrdinate(i, j, (i + 1) * 10 + j + 1);
        }
      }
    }
    catch (Throwable ex)
    {
      System.out.println("Failed to create sequence " +
              "of size " + size + " and ordinate flag " + ordinateFlagString(ordinates) + " " +
              "using '" + factory.getClass().getSimpleName() + "'!");
    }
    return seq;
  }

  private static String ordinateFlagString(EnumSet<Ordinate> ordinateFlag) {

    StringBuilder sb = new StringBuilder("XY");
    if (ordinateFlag.contains(Ordinate.Z)) sb.append('Z');
    if (ordinateFlag.contains(Ordinate.M)) sb.append('M');

    return sb.toString();
  }

}

