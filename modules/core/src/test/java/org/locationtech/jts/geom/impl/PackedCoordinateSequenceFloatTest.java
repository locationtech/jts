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

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;

import junit.textui.TestRunner;

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

  public void test4dCoordinateSequence() {
    CoordinateSequence cs = new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.FLOAT)
            .create(new float[]{0.0f,1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f}, 4);
    assertEquals(2.0, cs.getCoordinate(0).getZ());
    assertEquals(3.0, cs.getCoordinate(0).getM());
  }
}
