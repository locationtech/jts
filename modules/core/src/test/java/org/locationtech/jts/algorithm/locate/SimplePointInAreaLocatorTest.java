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
package org.locationtech.jts.algorithm.locate;

import org.locationtech.jts.algorithm.AbstractPointInRingTest;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;

/**
 * Tests IndexedPointInAreaLocator algorithms
 *
 * @version 1.7
 */
public class SimplePointInAreaLocatorTest extends AbstractPointInRingTest {

  private WKTReader reader = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(SimplePointInAreaLocatorTest.class);
  }

  public SimplePointInAreaLocatorTest(String name) { super(name); }


   protected void runPtInRing(int expectedLoc, Coordinate pt, String wkt)
      throws Exception
  {
    Geometry geom = reader.read(wkt);
    SimplePointInAreaLocator loc = new SimplePointInAreaLocator(geom);
    int result = loc.locate(pt);
    assertEquals(expectedLoc, result);
  }

}
