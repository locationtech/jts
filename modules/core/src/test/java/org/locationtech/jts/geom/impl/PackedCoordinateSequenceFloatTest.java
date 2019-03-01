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

import junit.textui.TestRunner;
import org.locationtech.jts.io.Ordinate;

/**
 * Test {@link PackedCoordinateSequence.Float}
 * using the {@link CoordinateSequenceTestBase}
 * 
 * @version 1.7
 */
public class PackedCoordinateSequenceFloatTest
    extends CoordinateSequenceTestBase
{
  public static void main(String args[]) {
    TestRunner.run(PackedCoordinateSequenceFloatTest.class);
  }

  public PackedCoordinateSequenceFloatTest(String name)
  {
    super(name);
  }

  @Override
  CoordinateSequenceFactory getCSFactory() {
    return PackedCoordinateSequenceFactory.FLOAT_FACTORY;
  }

  @Override
  int getDefaultDimension() { return PackedCoordinateSequence.DEFAULT_DIMENSION; }

  @Override
  int getFactoryMaxDimension() { return Integer.MAX_VALUE; }

  @Override
  boolean isSame(CoordinateSequence seq1, CoordinateSequence seq2) {

    if (seq1 == seq2)
      return true;


    if (((PackedCoordinateSequence.Float)seq1).getRawCoordinates() ==
            ((PackedCoordinateSequence.Float)seq2).getRawCoordinates())
      return true;

    return false;
  }

  @Override
  public void testZOrdinateIsNaN() {
    double initalZ = PackedCoordinateSequence.getInitialZValue();
    PackedCoordinateSequence.setInitialZValue(Coordinate.NULL_ORDINATE);
    super.testZOrdinateIsNaN();
    PackedCoordinateSequence.setInitialZValue(initalZ);
  }

  @Override
  public void testFactoryCreateWithSizeAndDimension() {
    double initialZ = PackedCoordinateSequence.getInitialZValue();
    PackedCoordinateSequence.setInitialZValue(Coordinate.NULL_ORDINATE);
    super.testFactoryCreateWithSizeAndDimension();
    PackedCoordinateSequence.setInitialZValue(initialZ);
  }

  @Override
  public void testConstructorDirect() {


    // Add additional dimensions
    Coordinate[] coords = createArray(5, Ordinate.createXY());
    CoordinateSequence seq = new PackedCoordinateSequence.Float(coords, 5, 2);

    assertNotNull(seq);
    assertTrue(seq.size() == 5);
    assertTrue(seq.getDimension() == 5);
    assertTrue(seq.getMeasures() == 2);

    if (Double.isNaN(PackedCoordinateSequence.getInitialZValue()))
      assertTrue(Double.isNaN(seq.getZ(2)));
    else
      assertEquals(0d, seq.getZ(2));

    // remove m-dimension
    coords = createArray(5, Ordinate.createXYZM());
    seq = new PackedCoordinateSequence.Float(coords, 3, 0);

    assertNotNull(seq);
    assertTrue(seq.size() == 5);
    assertTrue(seq.getDimension() == 3);
    assertTrue(seq.getMeasures() == 0);

    assertEquals(coords[2].getZ(), seq.getZ(2));

    // remove z-dimension
    coords = createArray(5, Ordinate.createXYZM());
    seq = new PackedCoordinateSequence.Float(coords, 3, 1);

    assertNotNull(seq);
    assertTrue(seq.size() == 5);
    assertTrue(seq.getDimension() == 3);
    assertTrue(seq.getMeasures() == 1);

    assertEquals(coords[2].getM(), seq.getM(2));
  }

}