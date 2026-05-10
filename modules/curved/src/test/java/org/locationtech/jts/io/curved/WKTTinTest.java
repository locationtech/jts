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
 * Red tests for WKT support of {@code Tin} geometry (Triangulated Irregular
 * Network, OGC SFA / ISO 19125-2). A Tin is a PolyhedralSurface whose patches
 * are all triangles.
 */
public class WKTTinTest extends GeometryTestCase {

  private static final String TYPENAME_TIN = "Tin";

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() { return new TestSuite(WKTTinTest.class); }

  public WKTTinTest(String name) { super(name); }

  public void testReadXY() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "TIN(((0 0, 1 0, 0 1, 0 0)), ((1 0, 1 1, 0 1, 1 0)))");
    assertEquals(TYPENAME_TIN, g.getGeometryType());
    assertFalse(g.isEmpty());
    assertEquals(2, g.getNumGeometries());
    assertEquals(2, g.getDimension());
    assertEquals(1, g.getBoundaryDimension());
  }

  public void testReadXYZ() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "TIN Z(((0 0 0, 1 0 0, 0 1 0, 0 0 0)), ((1 0 0, 1 1 0, 0 1 0, 1 0 0)))");
    assertEquals(TYPENAME_TIN, g.getGeometryType());
    assertEquals(2, g.getNumGeometries());
    assertEquals(0.0, g.getCoordinates()[0].getZ(), 0.0);
  }

  public void testReadEmpty() throws Exception {
    Geometry g = new CurvedWKTReader().read("TIN EMPTY");
    assertEquals(TYPENAME_TIN, g.getGeometryType());
    assertTrue(g.isEmpty());
    assertEquals(0, g.getNumGeometries());
  }

  public void testWKTRoundTripXY() throws Exception {
    String wkt = "TIN (((0 0, 1 0, 0 1, 0 0)), ((1 0, 1 1, 0 1, 1 0)))";
    Geometry g = new CurvedWKTReader().read(wkt);
    String emitted = new CurvedWKTWriter().write(g);
    assertTrue("Expected emitted WKT to mention TIN but was: " + emitted,
        emitted.toUpperCase().contains("TIN"));
    Geometry g2 = new CurvedWKTReader().read(emitted);
    checkEqual(g, g2);
  }

  /** Every patch in a Tin must be a triangle (4-point closed ring). */
  public void testRejectsNonTrianglePatch() throws Exception {
    // positive control
    assertNotNull(new CurvedWKTReader().read("TIN(((0 0, 1 0, 0 1, 0 0)))"));

    try {
      new CurvedWKTReader().read("TIN(((0 0, 1 0, 1 1, 0 1, 0 0)))");
      fail("Expected parse failure for TIN with quadrilateral patch");
    } catch (Throwable e) {
      // expected
    }
  }
}
