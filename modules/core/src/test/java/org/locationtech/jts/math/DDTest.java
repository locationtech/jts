/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jts.math;

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
