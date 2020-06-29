/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;


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

  abstract CoordinateSequenceFactory getCSFactory();
  
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

  public void testCreateByInit()
  {
    Coordinate[] coords = createArray(SIZE);
    CoordinateSequence seq = getCSFactory().create(coords);
    assertTrue(isEqual(seq, coords));
  }

  public void testCreateByInitAndCopy()
  {
    Coordinate[] coords = createArray(SIZE);
    CoordinateSequence seq = getCSFactory().create(coords);
    CoordinateSequence seq2 = getCSFactory().create(seq);
    assertTrue(isEqual(seq2, coords));
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

  Coordinate[] createArray(int size)
  {
    Coordinate[] coords = new Coordinate[size];
    for (int i = 0; i < size; i++) {
      double base = 2 * 1;
      coords[i] = new Coordinate(base, base + 1, base + 2);
    }
    return coords;
  }

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

  /**
   * Tests for equality using all supported accessors,
   * to provides test coverage for them.
   * 
   * @param seq
   * @param coords
   * @return
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
  boolean isEqual( double expected, double actual) {
    return expected == actual || (Double.isNaN(expected)&&Double.isNaN(actual));
  }
}

