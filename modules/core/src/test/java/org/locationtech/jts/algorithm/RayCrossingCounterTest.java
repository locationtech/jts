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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;

/**
 * Tests PointInRing algorithms
 *
 * @version 1.7
 */
public class RayCrossingCounterTest extends AbstractPointInRingTest {

  private WKTReader reader = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(RayCrossingCounterTest.class);
    //new RayCrossingCounterTest("RayCrossingCounterTest").testRunPtInRing4d();
  }

  public RayCrossingCounterTest(String name) { super(name); }

  protected void runPtInRing(int expectedLoc, Coordinate pt, String wkt)
          throws Exception
  {
    Geometry geom = reader.read(wkt);
    assertEquals(expectedLoc, RayCrossingCounter.locatePointInRing(pt, geom.getCoordinates()));
  }

  public void testRunPtInRing4d()
  {
    CoordinateSequence cs = new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE)
            .create(new double[]{
                    0.0, 0.0, 0.0, 0.0,
                    10.0, 0.0, 0.0, 0.0,
                    5.0, 10.0, 0.0, 0.0,
                    0.0, 0.0, 0.0, 0.0
            }, 4, 1);
    assertEquals(Location.INTERIOR, RayCrossingCounter.locatePointInRing(new Coordinate(5.0, 2.0), cs));
  }

}
