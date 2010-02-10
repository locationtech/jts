package test.jts.junit.geom.impl;

import com.vividsolutions.jts.geom.impl.*;

import junit.textui.TestRunner;
import junit.framework.TestCase;

/**
 * Test {@link CoordinateArraySequence}
 *
 * @version 1.7
 */
public class CoordinateArraySequenceTest
    extends CoordinateSequenceTestBase
{
  public static void main(String args[]) {
    TestRunner.run(CoordinateArraySequenceTest.class);
  }

  public CoordinateArraySequenceTest(String name)
  {
    super(name);
    csFactory = CoordinateArraySequenceFactory.instance();
  }
}