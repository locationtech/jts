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
 * Red tests for WKT support of {@code CircularString} geometry
 * (OGC SFA / ISO 19125-2).
 * <p>
 * These tests document the expected behavior via the {@link WKTReader} /
 * {@link WKTWriter} public API. They fail against current JTS because
 * the WKTReader does not recognize the {@code CIRCULARSTRING} keyword and
 * the geometry implementation does not exist.
 */
public class WKTCircularStringTest extends GeometryTestCase {

  private static final String TYPENAME_CIRCULARSTRING = "CircularString";

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() { return new TestSuite(WKTCircularStringTest.class); }

  public WKTCircularStringTest(String name) { super(name); }

  public void testReadXY() throws Exception {
    Geometry g = new CurvedWKTReader().read("CIRCULARSTRING(1 5, 6 2, 7 3)");
    assertEquals(TYPENAME_CIRCULARSTRING, g.getGeometryType());
    assertFalse(g.isEmpty());
    assertEquals(3, g.getNumPoints());
    assertEquals(1, g.getDimension());
    assertEquals(0, g.getBoundaryDimension());
  }

  public void testReadXYZ() throws Exception {
    Geometry g = new CurvedWKTReader().read("CIRCULARSTRING Z(1 2 3, 4 5 6, 7 8 9)");
    assertEquals(TYPENAME_CIRCULARSTRING, g.getGeometryType());
    assertEquals(3.0, g.getCoordinates()[0].getZ(), 0.0);
    assertEquals(6.0, g.getCoordinates()[1].getZ(), 0.0);
  }

  public void testReadXYM() throws Exception {
    Geometry g = new CurvedWKTReader().read("CIRCULARSTRING M(1 2 7, 4 5 8, 7 8 9)");
    assertEquals(TYPENAME_CIRCULARSTRING, g.getGeometryType());
    assertEquals(7.0, g.getCoordinates()[0].getM(), 0.0);
    assertEquals(8.0, g.getCoordinates()[1].getM(), 0.0);
  }

  public void testReadXYZM() throws Exception {
    Geometry g = new CurvedWKTReader().read("CIRCULARSTRING ZM(1 2 3 4, 5 6 7 8, 9 10 11 12)");
    assertEquals(TYPENAME_CIRCULARSTRING, g.getGeometryType());
    assertEquals(3.0, g.getCoordinates()[0].getZ(), 0.0);
    assertEquals(4.0, g.getCoordinates()[0].getM(), 0.0);
  }

  public void testReadEmpty() throws Exception {
    Geometry g = new CurvedWKTReader().read("CIRCULARSTRING EMPTY");
    assertEquals(TYPENAME_CIRCULARSTRING, g.getGeometryType());
    assertTrue(g.isEmpty());
    assertEquals(0, g.getNumPoints());
  }

  public void testReadEmptyZ() throws Exception {
    Geometry g = new CurvedWKTReader().read("CIRCULARSTRING Z EMPTY");
    assertEquals(TYPENAME_CIRCULARSTRING, g.getGeometryType());
    assertTrue(g.isEmpty());
  }

  public void testWKTRoundTripXY() throws Exception {
    String wkt = "CIRCULARSTRING (1 5, 6 2, 7 3)";
    Geometry g = new CurvedWKTReader().read(wkt);
    String emitted = new CurvedWKTWriter().write(g);
    assertTrue("Expected emitted WKT to start with CIRCULARSTRING but was: " + emitted,
        emitted.toUpperCase().contains("CIRCULARSTRING"));
    Geometry g2 = new CurvedWKTReader().read(emitted);
    checkEqual(g, g2);
  }

  public void testWKTRoundTripXYZM() throws Exception {
    String wkt = "CIRCULARSTRING ZM (1 2 3 4, 5 6 7 8, 9 10 11 12)";
    Geometry g = new CurvedWKTReader().read(wkt);
    String emitted = new CurvedWKTWriter(4).write(g);
    Geometry g2 = new CurvedWKTReader().read(emitted);
    checkEqualXYZM(g, g2);
  }

  /**
   * Documents Phase-1 leniency: the parser does not enforce the OGC SFA rule
   * that a CircularString must contain an odd number of points (each arc
   * defined by a start/mid/end triple). A 4-point input parses without error.
   * <p>
   * Tracked for the validation phase via the curve-awareness spec epic
   * (sub-issue VAL-CS). When that lands this test should flip back to an
   * explicit {@code expectThrows(ParseException)} — the assertion below will
   * fail at that point, signalling the test author to update.
   */
  public void testAcceptsEvenPointCountForNow() throws Exception {
    Geometry g = new CurvedWKTReader().read("CIRCULARSTRING(0 0, 1 1, 2 0, 3 1)");
    assertEquals(TYPENAME_CIRCULARSTRING, g.getGeometryType());
    assertEquals(4, g.getNumPoints());
  }
}
