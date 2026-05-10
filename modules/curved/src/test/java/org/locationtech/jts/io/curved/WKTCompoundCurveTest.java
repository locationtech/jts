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
 * Red tests for WKT support of {@code CompoundCurve} geometry
 * (OGC SFA / ISO 19125-2). A CompoundCurve is a connected sequence of
 * LineStrings and CircularStrings.
 */
public class WKTCompoundCurveTest extends GeometryTestCase {

  private static final String TYPENAME_COMPOUNDCURVE = "CompoundCurve";

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() { return new TestSuite(WKTCompoundCurveTest.class); }

  public WKTCompoundCurveTest(String name) { super(name); }

  public void testReadXY() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "COMPOUNDCURVE((5 3, 5 13), CIRCULARSTRING(5 13, 7 15, 9 13), (9 13, 9 3))");
    assertEquals(TYPENAME_COMPOUNDCURVE, g.getGeometryType());
    assertFalse(g.isEmpty());
    assertEquals(1, g.getDimension());
    // open curve: boundary is its 2 endpoints (dim 0)
    assertEquals(0, g.getBoundaryDimension());
  }

  public void testReadClosedXY() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "COMPOUNDCURVE((5 3, 5 13), CIRCULARSTRING(5 13, 7 15, 9 13), (9 13, 9 3), CIRCULARSTRING(9 3, 7 1, 5 3))");
    assertEquals(TYPENAME_COMPOUNDCURVE, g.getGeometryType());
    // closed curve: empty boundary (dim FALSE / -1)
    assertEquals(-1, g.getBoundaryDimension());
  }

  public void testReadXYZ() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "COMPOUNDCURVE Z((1 2 3, 4 5 6), CIRCULARSTRING(4 5 6, 7 8 9, 10 11 12))");
    assertEquals(TYPENAME_COMPOUNDCURVE, g.getGeometryType());
    assertEquals(3.0, g.getCoordinates()[0].getZ(), 0.0);
  }

  public void testReadXYZM() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "COMPOUNDCURVE ZM((1 2 3 4, 5 6 7 8), CIRCULARSTRING(5 6 7 8, 9 10 11 12, 13 14 15 16))");
    assertEquals(TYPENAME_COMPOUNDCURVE, g.getGeometryType());
    assertEquals(3.0, g.getCoordinates()[0].getZ(), 0.0);
    assertEquals(4.0, g.getCoordinates()[0].getM(), 0.0);
  }

  public void testReadEmpty() throws Exception {
    Geometry g = new CurvedWKTReader().read("COMPOUNDCURVE EMPTY");
    assertEquals(TYPENAME_COMPOUNDCURVE, g.getGeometryType());
    assertTrue(g.isEmpty());
  }

  public void testWKTRoundTripXY() throws Exception {
    String wkt = "COMPOUNDCURVE ((5 3, 5 13), CIRCULARSTRING (5 13, 7 15, 9 13))";
    Geometry g = new CurvedWKTReader().read(wkt);
    String emitted = new CurvedWKTWriter().write(g);
    assertTrue("Expected emitted WKT to mention COMPOUNDCURVE but was: " + emitted,
        emitted.toUpperCase().contains("COMPOUNDCURVE"));
    Geometry g2 = new CurvedWKTReader().read(emitted);
    checkEqual(g, g2);
  }

  /** Adjacent members of a CompoundCurve must share endpoints. */
  public void testRejectsDisconnectedMembers() throws Exception {
    // positive control
    assertNotNull(new CurvedWKTReader().read("COMPOUNDCURVE((0 0, 1 1), (1 1, 2 2))"));

    try {
      new CurvedWKTReader().read("COMPOUNDCURVE((0 0, 1 1), (2 2, 3 3))");
      fail("Expected parse failure for disconnected COMPOUNDCURVE members");
    } catch (Throwable e) {
      // expected
    }
  }
}
