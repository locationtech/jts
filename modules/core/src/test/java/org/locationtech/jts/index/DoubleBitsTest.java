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
package org.locationtech.jts.index;

import org.locationtech.jts.index.quadtree.DoubleBits;

import junit.framework.TestCase;
import junit.textui.TestRunner;

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