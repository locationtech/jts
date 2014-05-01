package com.vividsolutions.jts.math;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests basic accessor and mutator operations for {@link DD}s.
 * 
 * @author Martin Davis
 *
 */
public class DDTest 
  extends TestCase
{
  private static final double VALUE_DBL = 2.2;
  
  public static void main(String args[]) {
      TestRunner.run(DDTest.class);
    }

  public DDTest(String name) { super(name); }

  public void testSetValueDouble()
  {
    assertTrue(VALUE_DBL == (new DD(1)).setValue(VALUE_DBL).doubleValue());
  }
  public void testSetValueDD()
  {
    assertTrue((new DD(VALUE_DBL)).equals((new DD(1)).setValue(new DD(2.2))));
    assertTrue(DD.PI.equals((new DD(1)).setValue(DD.PI)));
  }
  public void testCopy()
  {
    assertTrue((new DD(VALUE_DBL)).equals(DD.copy(new DD(VALUE_DBL))));
    assertTrue(DD.PI.equals(DD.copy(DD.PI)));
  }
}
