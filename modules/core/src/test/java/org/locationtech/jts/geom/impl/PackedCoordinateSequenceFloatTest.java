package org.locationtech.jts.geom.impl;

import junit.textui.TestRunner;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;

/**
 * Test {@link PackedCoordinateSequence}
 * @version 1.7
 */
public class PackedCoordinateSequenceFloatTest
        extends PackedCoordinateSequenceTest
{
  public static void main(String args[]) {
    TestRunner.run(PackedCoordinateSequenceFloatTest.class);
  }

  public PackedCoordinateSequenceFloatTest(String name)
  {
    super(name);
  }

  @Override
  int getType() { return PackedCoordinateSequenceFactory.FLOAT; }

  @Override
  boolean isSame(CoordinateSequence seq1, CoordinateSequence seq2) {

    if (seq1 == seq2)
      return true;


    if (((PackedCoordinateSequence.Float)seq1).getRawCoordinates() ==
        ((PackedCoordinateSequence.Float)seq2).getRawCoordinates())
      return true;

    return false;
  }

}
