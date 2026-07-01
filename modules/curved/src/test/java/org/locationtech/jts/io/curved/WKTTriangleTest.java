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
 * Red tests for WKT support of {@code Triangle} geometry
 * (OGC SFA / ISO 19125-2). A Triangle is a Polygon with exactly one
 * outer ring of exactly 4 points (first == last).
 */
public class WKTTriangleTest extends GeometryTestCase {

  private static final String TYPENAME_TRIANGLE = "Triangle";

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() { return new TestSuite(WKTTriangleTest.class); }

  public WKTTriangleTest(String name) { super(name); }

  public void testReadXY() throws Exception {
    Geometry g = new CurvedWKTReader().read("TRIANGLE((0 0, 1 0, 0 1, 0 0))");
    assertEquals(TYPENAME_TRIANGLE, g.getGeometryType());
    assertFalse(g.isEmpty());
    assertEquals(4, g.getNumPoints());
    assertEquals(2, g.getDimension());
    assertEquals(1, g.getBoundaryDimension());
  }

  public void testReadXYZ() throws Exception {
    Geometry g = new CurvedWKTReader().read("TRIANGLE Z((0 0 0, 1 0 0, 0 1 0, 0 0 0))");
    assertEquals(TYPENAME_TRIANGLE, g.getGeometryType());
    assertEquals(0.0, g.getCoordinates()[0].getZ(), 0.0);
    assertEquals(0.0, g.getCoordinates()[2].getZ(), 0.0);
  }

  public void testReadXYM() throws Exception {
    Geometry g = new CurvedWKTReader().read("TRIANGLE M((0 0 7, 1 0 8, 0 1 9, 0 0 7))");
    assertEquals(TYPENAME_TRIANGLE, g.getGeometryType());
    assertEquals(7.0, g.getCoordinates()[0].getM(), 0.0);
  }

  public void testReadXYZM() throws Exception {
    Geometry g = new CurvedWKTReader().read("TRIANGLE ZM((0 0 0 7, 1 0 0 8, 0 1 0 9, 0 0 0 7))");
    assertEquals(TYPENAME_TRIANGLE, g.getGeometryType());
    assertEquals(0.0, g.getCoordinates()[0].getZ(), 0.0);
    assertEquals(7.0, g.getCoordinates()[0].getM(), 0.0);
  }

  public void testReadEmpty() throws Exception {
    Geometry g = new CurvedWKTReader().read("TRIANGLE EMPTY");
    assertEquals(TYPENAME_TRIANGLE, g.getGeometryType());
    assertTrue(g.isEmpty());
  }

  public void testWKTRoundTripXY() throws Exception {
    String wkt = "TRIANGLE ((0 0, 1 0, 0 1, 0 0))";
    Geometry g = new CurvedWKTReader().read(wkt);
    String emitted = new CurvedWKTWriter().write(g);
    assertTrue("Expected emitted WKT to mention TRIANGLE but was: " + emitted,
        emitted.toUpperCase().contains("TRIANGLE"));
    Geometry g2 = new CurvedWKTReader().read(emitted);
    checkEqual(g, g2);
  }

  /**
   * Documents Phase-1 leniency: the parser does not enforce the OGC SFA rule
   * that a Triangle's ring contains exactly 4 points. A 5-point closed ring
   * parses as a Triangle with the extra vertex retained.
   * <p>
   * Tracked via the curve-awareness spec epic (sub-issue VAL-T point-count).
   */
  public void testAcceptsWrongPointCountForNow() throws Exception {
    Geometry g = new CurvedWKTReader().read("TRIANGLE((0 0, 1 0, 1 1, 0 1, 0 0))");
    assertEquals(TYPENAME_TRIANGLE, g.getGeometryType());
    assertEquals(5, g.getNumPoints());
  }

  /**
   * The Triangle ring must be closed (first point == last point). This rule
   * is actually enforced today — not by jts-curved but by the LinearRing
   * factory used inside {@code readTriangleText}, which throws
   * {@link IllegalArgumentException} for non-closed coordinate sequences.
   */
  public void testRejectsUnclosedRing() throws Exception {
    assertNotNull(new CurvedWKTReader().read("TRIANGLE((0 0, 1 0, 0 1, 0 0))"));
    try {
      new CurvedWKTReader().read("TRIANGLE((0 0, 1 0, 0 1, 0 1))");
      fail("Expected parse failure for unclosed TRIANGLE ring");
    } catch (IllegalArgumentException e) {
      // expected: LinearRing factory rejects non-closed coordinate sequences
    }
  }

  /**
   * Documents Phase-1 leniency: the parser does not enforce the OGC SFA rule
   * that a Triangle has no holes. A polygon body with an inner ring parses
   * as a Triangle whose inner ring is silently dropped (only the exterior
   * ring is forwarded by {@code readTriangleText}).
   * <p>
   * Tracked via the curve-awareness spec epic (sub-issue VAL-T holes).
   */
  public void testAcceptsInnerRingForNow() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "TRIANGLE((0 0, 10 0, 0 10, 0 0), (1 1, 2 1, 1 2, 1 1))");
    assertEquals(TYPENAME_TRIANGLE, g.getGeometryType());
  }
}
