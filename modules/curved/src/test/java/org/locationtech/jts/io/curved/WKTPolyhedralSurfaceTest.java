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
 * Red tests for WKT support of {@code PolyhedralSurface} geometry
 * (OGC SFA / ISO 19125-2). A PolyhedralSurface is a contiguous collection
 * of polygonal patches sharing edges.
 */
public class WKTPolyhedralSurfaceTest extends GeometryTestCase {

  private static final String TYPENAME_POLYHEDRALSURFACE = "PolyhedralSurface";

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() { return new TestSuite(WKTPolyhedralSurfaceTest.class); }

  public WKTPolyhedralSurfaceTest(String name) { super(name); }

  public void testReadXY() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "POLYHEDRALSURFACE(((0 0, 0 1, 1 1, 1 0, 0 0)), ((1 1, 1 2, 2 2, 2 1, 1 1)))");
    assertEquals(TYPENAME_POLYHEDRALSURFACE, g.getGeometryType());
    assertFalse(g.isEmpty());
    assertEquals(2, g.getNumGeometries());
    assertEquals(2, g.getDimension());
    assertEquals(1, g.getBoundaryDimension());
  }

  public void testReadXYZ() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "POLYHEDRALSURFACE Z(((0 0 0, 0 1 0, 0 1 1, 0 0 0)), ((0 0 0, 0 1 0, 1 0 0, 0 0 0)), ((0 0 0, 1 0 0, 0 1 1, 0 0 0)), ((1 0 0, 0 1 0, 0 1 1, 1 0 0)))");
    assertEquals(TYPENAME_POLYHEDRALSURFACE, g.getGeometryType());
    assertEquals(4, g.getNumGeometries());
    assertEquals(0.0, g.getCoordinates()[0].getZ(), 0.0);
  }

  public void testReadEmpty() throws Exception {
    Geometry g = new CurvedWKTReader().read("POLYHEDRALSURFACE EMPTY");
    assertEquals(TYPENAME_POLYHEDRALSURFACE, g.getGeometryType());
    assertTrue(g.isEmpty());
    assertEquals(0, g.getNumGeometries());
  }

  public void testReadEmptyZ() throws Exception {
    Geometry g = new CurvedWKTReader().read("POLYHEDRALSURFACE Z EMPTY");
    assertEquals(TYPENAME_POLYHEDRALSURFACE, g.getGeometryType());
    assertTrue(g.isEmpty());
  }

  public void testWKTRoundTripXY() throws Exception {
    String wkt = "POLYHEDRALSURFACE (((0 0, 0 1, 1 1, 1 0, 0 0)), ((1 1, 1 2, 2 2, 2 1, 1 1)))";
    Geometry g = new CurvedWKTReader().read(wkt);
    String emitted = new CurvedWKTWriter().write(g);
    assertTrue("Expected emitted WKT to mention POLYHEDRALSURFACE but was: " + emitted,
        emitted.toUpperCase().contains("POLYHEDRALSURFACE"));
    Geometry g2 = new CurvedWKTReader().read(emitted);
    checkEqual(g, g2);
  }

  public void testWKTRoundTripXYZ() throws Exception {
    String wkt = "POLYHEDRALSURFACE Z (((0 0 0, 0 1 0, 0 1 1, 0 0 0)), ((0 0 0, 0 1 0, 1 0 0, 0 0 0)))";
    Geometry g = new CurvedWKTReader().read(wkt);
    String emitted = new CurvedWKTWriter(3).write(g);
    Geometry g2 = new CurvedWKTReader().read(emitted);
    checkEqualXYZ(g, g2);
  }
}
