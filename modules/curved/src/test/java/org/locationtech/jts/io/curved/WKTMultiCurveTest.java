/*
 * Copyright (c) 2026 grootstebozewolf
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.io.curved;


import org.locationtech.jts.geom.Geometry;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Red tests for WKT support of {@code MultiCurve} geometry
 * (OGC SFA / ISO 19125-2). A MultiCurve is a collection of LineStrings,
 * CircularStrings, and/or CompoundCurves.
 */
public class WKTMultiCurveTest extends GeometryTestCase {

  private static final String TYPENAME_MULTICURVE = "MultiCurve";

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() { return new TestSuite(WKTMultiCurveTest.class); }

  public WKTMultiCurveTest(String name) { super(name); }

  public void testReadXY() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "MULTICURVE((5 5, 3 5, 3 3, 0 3), CIRCULARSTRING(0 0, 0.2 1, 0.5 1.4), COMPOUNDCURVE(CIRCULARSTRING(0 0, 1 1, 1 0), (1 0, 0 1)))");
    assertEquals(TYPENAME_MULTICURVE, g.getGeometryType());
    assertFalse(g.isEmpty());
    assertEquals(3, g.getNumGeometries());
    assertEquals(1, g.getDimension());
  }

  public void testReadHomogeneousLineStrings() throws Exception {
    Geometry g = new CurvedWKTReader().read("MULTICURVE((0 0, 1 1), (2 2, 3 3))");
    assertEquals(TYPENAME_MULTICURVE, g.getGeometryType());
    assertEquals(2, g.getNumGeometries());
  }

  public void testReadXYZ() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "MULTICURVE Z(CIRCULARSTRING(0 0 0, 1 1 0, 2 0 0), (3 3 0, 4 4 0))");
    assertEquals(TYPENAME_MULTICURVE, g.getGeometryType());
    assertEquals(2, g.getNumGeometries());
    assertEquals(0.0, g.getCoordinates()[0].getZ(), 0.0);
  }

  public void testReadEmpty() throws Exception {
    Geometry g = new CurvedWKTReader().read("MULTICURVE EMPTY");
    assertEquals(TYPENAME_MULTICURVE, g.getGeometryType());
    assertTrue(g.isEmpty());
    assertEquals(0, g.getNumGeometries());
  }

  public void testReadWithEmptyMember() throws Exception {
    Geometry g = new CurvedWKTReader().read("MULTICURVE((0 0, 1 1), EMPTY, CIRCULARSTRING(2 2, 3 3, 4 2))");
    assertEquals(TYPENAME_MULTICURVE, g.getGeometryType());
    assertEquals(3, g.getNumGeometries());
    assertTrue(g.getGeometryN(1).isEmpty());
  }

  public void testWKTRoundTripXY() throws Exception {
    String wkt = "MULTICURVE ((5 5, 3 5, 3 3, 0 3), CIRCULARSTRING (0 0, 1 1, 2 0))";
    Geometry g = new CurvedWKTReader().read(wkt);
    String emitted = new CurvedWKTWriter().write(g);
    assertTrue("Expected emitted WKT to mention MULTICURVE but was: " + emitted,
        emitted.toUpperCase().contains("MULTICURVE"));
    Geometry g2 = new CurvedWKTReader().read(emitted);
    checkEqual(g, g2);
  }
}
