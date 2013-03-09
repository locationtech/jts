package com.vividsolutions.jts.geom.impl;

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
    csFactory = new PackedCoordinateSequenceFactory();
  }

}