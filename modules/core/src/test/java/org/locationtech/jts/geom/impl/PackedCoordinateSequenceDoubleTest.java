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
 * Test {@link PackedCoordinateSequence.Double}
 * using the {@link CoordinateSequenceTestBase}
 * @version 1.7
 */
public class PackedCoordinateSequenceDoubleTest
    extends CoordinateSequenceTestBase
{
  public static void main(String args[]) {
    TestRunner.run(PackedCoordinateSequenceDoubleTest.class);
  }

  public PackedCoordinateSequenceDoubleTest(String name)
  {
    super(name);
  }

  @Override
  CoordinateSequenceFactory getCSFactory() {
    return PackedCoordinateSequenceFactory.DOUBLE_FACTORY;
  }

  public void test3dCoordinateSequence() {
    CoordinateSequence cs = new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE)
            .create(new double[]{0.0,1.0,2.0,3.0,4.0,5.0}, 3);
    assertEquals(2.0, cs.getCoordinate(0).getZ());
  }

  public void test4dCoordinateSequence() {
    CoordinateSequence cs = new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE)
            .create(new double[]{0.0,1.0,2.0,3.0,4.0,5.0,6.0,7.0}, 4);
    assertEquals(2.0, cs.getCoordinate(0).getZ());
    assertEquals(3.0, cs.getCoordinate(0).getM());
  }

}
