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
      seq.setOrdinate(i, 2, coords[i].z);
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
      assertTrue(Double.isNaN(p.z));
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
      if (! coord.equals(seq.getCoordinate(i)))  return false;

      if (coord.x != seq.getOrdinate(i, CoordinateSequence.X))  return false;
      if (coord.y != seq.getOrdinate(i, CoordinateSequence.Y))  return false;
      if (coord.z != seq.getOrdinate(i, CoordinateSequence.Z))  return false;
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

    Coordinate p = new Coordinate();
    
    for (int i = 0; i < seq.size(); i++) {
      if (! coords[i].equals(seq.getCoordinate(i)))  return false;

      // Ordinate named getters
      if (coords[i].x != seq.getX(i))  return false;
      if (coords[i].y != seq.getY(i))  return false;

      // Ordinate indexed getters
      if (coords[i].x != seq.getOrdinate(i, CoordinateSequence.X))  return false;
      if (coords[i].y != seq.getOrdinate(i, CoordinateSequence.Y))  return false;
      if (coords[i].z != seq.getOrdinate(i, CoordinateSequence.Z))  return false;
      
      // Coordinate getter
      seq.getCoordinate(i, p);
      if (coords[i].x != p.x) return false;
      if (coords[i].y != p.y)  return false;
      if (coords[i].z != p.z)  return false;
      
    }
    return true;
  }
}

