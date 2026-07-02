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
 * Red tests for WKT support of {@code CurvePolygon} geometry
 * (OGC SFA / ISO 19125-2). A CurvePolygon is a polygon whose rings may
 * be CircularStrings, CompoundCurves, or LineStrings.
 */
public class WKTCurvePolygonTest extends GeometryTestCase {

  private static final String TYPENAME_CURVEPOLYGON = "CurvePolygon";

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() { return new TestSuite(WKTCurvePolygonTest.class); }

  public WKTCurvePolygonTest(String name) { super(name); }

  public void testReadXY() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "CURVEPOLYGON(CIRCULARSTRING(0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 3 3, 3 1, 1 1))");
    assertEquals(TYPENAME_CURVEPOLYGON, g.getGeometryType());
    assertFalse(g.isEmpty());
    assertEquals(2, g.getDimension());
    assertEquals(1, g.getBoundaryDimension());
  }

  public void testReadXYZ() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "CURVEPOLYGON Z(CIRCULARSTRING(0 0 0, 4 0 0, 4 4 0, 0 4 0, 0 0 0), (1 1 0, 3 3 0, 3 1 0, 1 1 0))");
    assertEquals(TYPENAME_CURVEPOLYGON, g.getGeometryType());
    assertEquals(0.0, g.getCoordinates()[0].getZ(), 0.0);
  }

  public void testReadCompoundCurveRing() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "CURVEPOLYGON(COMPOUNDCURVE(CIRCULARSTRING(0 0, 1 1, 2 0), (2 0, 0 0)))");
    assertEquals(TYPENAME_CURVEPOLYGON, g.getGeometryType());
    assertFalse(g.isEmpty());
  }

  public void testReadEmpty() throws Exception {
    Geometry g = new CurvedWKTReader().read("CURVEPOLYGON EMPTY");
    assertEquals(TYPENAME_CURVEPOLYGON, g.getGeometryType());
    assertTrue(g.isEmpty());
  }

  public void testReadEmptyZM() throws Exception {
    Geometry g = new CurvedWKTReader().read("CURVEPOLYGON ZM EMPTY");
    assertEquals(TYPENAME_CURVEPOLYGON, g.getGeometryType());
    assertTrue(g.isEmpty());
  }

  public void testWKTRoundTripXY() throws Exception {
    String wkt = "CURVEPOLYGON (CIRCULARSTRING (0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 3 3, 3 1, 1 1))";
    Geometry g = new CurvedWKTReader().read(wkt);
    String emitted = new CurvedWKTWriter().write(g);
    assertTrue("Expected emitted WKT to mention CURVEPOLYGON but was: " + emitted,
        emitted.toUpperCase().contains("CURVEPOLYGON"));
    Geometry g2 = new CurvedWKTReader().read(emitted);
    checkEqual(g, g2);
  }

  /**
   * The CurvePolygon outer ring must be closed (first point == last point).
   * This rule is enforced today by the LinearRing factory inside
   * {@code readCurvePolygonText}, which throws {@link IllegalArgumentException}
   * for non-closed coordinate sequences.
   */
  public void testRejectsUnclosedRing() throws Exception {
    assertNotNull(new CurvedWKTReader().read("CURVEPOLYGON((0 0, 1 0, 1 1, 0 1, 0 0))"));
    try {
      new CurvedWKTReader().read("CURVEPOLYGON((0 0, 1 0, 1 1, 0 1))");
      fail("Expected parse failure for unclosed CURVEPOLYGON ring");
    } catch (IllegalArgumentException e) {
      // expected: LinearRing factory rejects non-closed coordinate sequences
    }
  }
}
