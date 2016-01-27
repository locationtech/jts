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
package com.vividsolutions.jts.index;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.index.quadtree.DoubleBits;
/**
 * Tests DoubleBits
 * @version 1.7
 */
public class DoubleBitsTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(DoubleBitsTest.class);
  }

  public DoubleBitsTest(String name) { super(name); }

  public void testExponent() throws Exception
  {
    assertTrue(DoubleBits.exponent(-1) == 0);
    assertTrue(DoubleBits.exponent(8.0) == 3);
    assertTrue(DoubleBits.exponent(128.0) == 7);
  }

}