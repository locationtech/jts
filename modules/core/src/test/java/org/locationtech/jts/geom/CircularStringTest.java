/*
 * Copyright (c) 2026 Jeroen Bloemscheer.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
/*
 * AI Disclosure: This file was largely AI-generated.
 * The AI-generated portions are made available under CC0-1.0 and not subject to the project's licence.
 * The human contributor has reviewed and verified that the code is correct.
 * SPDX-License-Identifier: EPL-2.0 OR EDL-1.0 and CC0-1.0
 * Assisted-by: Cursor Agent
 * Assisted-by: xAI Grok
 */
package org.locationtech.jts.geom;

import org.locationtech.jts.algorithm.exactcurve.ExactCircularArc;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests for {@link CircularString}: WKT parse/write, closed rings,
 * collinear degrade, linearize tolerance, and getCoordinates safety.
 *
 * @author Jeroen Bloemscheer
 */
public class CircularStringTest extends GeometryTestCase {

  private final GeometryFactory factory = new GeometryFactory();
  private final WKTReader reader = new WKTReader(factory);
  private final WKTWriter writer = new WKTWriter();

  public static void main(String[] args) {
    TestRunner.run(suite());
  }

  public CircularStringTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(CircularStringTest.class);
  }

  public void testParseAndWrite() throws Exception {
    String wkt = "CIRCULARSTRING (0 0, 1 1, 1 0)";
    Geometry g = reader.read(wkt);
    assertTrue(g instanceof CircularString);
    CircularString cs = (CircularString) g;
    assertEquals(Geometry.TYPENAME_CIRCULARSTRING, cs.getGeometryType());
    assertEquals(3, cs.getNumControlPoints());
    assertEquals(1, cs.getNumArcs());
    assertEquals(wkt, writer.write(cs));
  }

  public void testParseWriteEmpty() throws Exception {
    String wkt = "CIRCULARSTRING EMPTY";
    CircularString cs = (CircularString) reader.read(wkt);
    assertTrue(cs.isEmpty());
    assertEquals(0, cs.getNumControlPoints());
    assertEquals(0, cs.getNumArcs());
    assertEquals(0.0, cs.getLength(), 1e-15);
    assertEquals(wkt, writer.write(cs));
  }

  public void testParseWriteClosedRing() throws Exception {
    String wkt = "CIRCULARSTRING (0 0, 1 0, 1 1, 0 1, 0 0)";
    CircularString cs = (CircularString) reader.read(wkt);
    assertTrue(cs.isClosed());
    assertEquals(5, cs.getNumControlPoints());
    assertEquals(2, cs.getNumArcs());
    assertEquals(wkt, writer.write(cs));
    Coordinate[] controls = cs.getControlPoints();
    assertTrue(controls[0].equals2D(controls[controls.length - 1]));
  }

  public void testClosedSingleArcCircleDiameterConvention() throws Exception {
    // CIRCULARSTRING(s, m, s) is a full circle with diameter s–m.
    CircularString cs = (CircularString) reader.read("CIRCULARSTRING (0 0, 1 0, 0 0)");
    assertTrue(cs.isClosed());
    assertEquals(3, cs.getNumPoints());
    ExactCircularArc arc = cs.getArcN(0);
    assertTrue(arc.isFullCircle());
    assertTrue(arc.isExact());
    assertFalse(arc.isCollinear());
    assertEquals(0.5, arc.getRadius(), 1e-12);
    assertTrue(arc.getCenter().equals2D(new Coordinate(0.5, 0)));
    assertEquals(Math.PI, cs.getLength(), 1e-10);
    assertSame("windows are composed, not rebuilt", arc, cs.getArcN(0));
  }

  public void testCollinearDegradesToChord() throws Exception {
    CircularString cs = (CircularString) reader.read("CIRCULARSTRING (0 0, 1 0, 2 0)");
    ExactCircularArc arc = cs.getArcN(0);
    assertTrue(arc.isCollinear());
    assertEquals(2.0, cs.getLength(), 1e-12);

    LineString linear = cs.toLinear();
    assertEquals(3, linear.getNumPoints());
    assertTrue(linear.getCoordinateN(0).equals2D(new Coordinate(0, 0)));
    assertTrue(linear.getCoordinateN(1).equals2D(new Coordinate(1, 0)));
    assertTrue(linear.getCoordinateN(2).equals2D(new Coordinate(2, 0)));
    assertEquals(2.0, linear.getLength(), 1e-12);

    Envelope env = cs.getEnvelopeInternal();
    assertEquals(0.0, env.getMinX(), 1e-12);
    assertEquals(2.0, env.getMaxX(), 1e-12);
    assertEquals(0.0, env.getMinY(), 1e-12);
    assertEquals(0.0, env.getMaxY(), 1e-12);
  }

  public void testLinearizeTolerance() throws Exception {
    // quarter-ish arc: (1,0) through (0,1) to (-1,0) on the unit circle
    CircularString cs = (CircularString) reader.read("CIRCULARSTRING (1 0, 0 1, -1 0)");
    ExactCircularArc arc = cs.getArcN(0);
    assertFalse(arc.isCollinear());
    assertEquals(1.0, arc.getRadius(), 1e-8);
    assertEquals(Math.PI, cs.getLength(), 1e-8);

    LineString coarse = cs.toLinear(0.5);
    LineString fine = cs.toLinear(0.01);
    assertTrue(fine.getNumPoints() > coarse.getNumPoints());

    Coordinate center = arc.getCenter();
    Coordinate[] finePts = fine.getCoordinates();
    for (int i = 0; i < finePts.length; i++) {
      assertEquals(1.0, finePts[i].distance(center), 0.01 + 1e-6);
    }
    assertTrue(fine.getCoordinateN(0).equals2D(new Coordinate(1, 0)));
    assertTrue(fine.getCoordinateN(fine.getNumPoints() - 1).equals2D(new Coordinate(-1, 0)));
  }

  public void testGetCoordinatesSafety() throws Exception {
    Coordinate[] input = new Coordinate[] {
        new Coordinate(0, 0), new Coordinate(1, 1), new Coordinate(1, 0)
    };
    CircularString cs = factory.createCircularString(input);

    input[0].x = 999;
    input[1].x = 999;
    assertEquals(0.0, cs.getControlPoints()[0].x, 0.0);
    assertEquals("CIRCULARSTRING (0 0, 1 1, 1 0)", writer.write(cs));

    Coordinate[] coords = cs.getCoordinates();
    assertEquals("getCoordinates speaks controls, not linearized vertices", 3, coords.length);
    assertEquals(3, cs.getNumPoints());
    assertTrue(cs.getCoordinateN(1).equals2D(new Coordinate(1, 1)));
    assertEquals(3, cs.getCoordinateSequence().size());
    assertTrue(cs.toLinear().getNumPoints() >= 3);
    double origX = coords[0].x;
    coords[0].x = 12345;
    coords[1] = new Coordinate(-99, -99);

    Coordinate[] again = cs.getCoordinates();
    assertEquals(origX, again[0].x, 0.0);
    assertFalse(again[1].equals2D(new Coordinate(-99, -99)));
    assertEquals(0.0, cs.getControlPoints()[0].x, 0.0);
  }

  public void testControlPointSequenceIsCopy() throws Exception {
    CircularString cs = (CircularString) reader.read("CIRCULARSTRING (0 0, 1 1, 1 0)");
    CoordinateSequence seq = cs.getControlPointSequence();
    seq.setOrdinate(0, 0, 42);
    assertEquals(0.0, cs.getControlPoints()[0].x, 0.0);
  }

  public void testInvalidControlCount() {
    try {
      factory.createCircularString(new Coordinate[] {
          new Coordinate(0, 0), new Coordinate(1, 0)
      });
      fail("expected IllegalArgumentException for 2 control points");
    }
    catch (IllegalArgumentException ex) {
      // expected
    }
    try {
      factory.createCircularString(new Coordinate[] {
          new Coordinate(0, 0), new Coordinate(1, 0),
          new Coordinate(1, 1), new Coordinate(0, 1)
      });
      fail("expected IllegalArgumentException for even control count");
    }
    catch (IllegalArgumentException ex) {
      // expected
    }
  }

  public void testCopyAndReversePreserveType() throws Exception {
    CircularString cs = (CircularString) reader.read("CIRCULARSTRING (0 0, 1 1, 1 0)");
    CircularString copy = (CircularString) cs.copy();
    assertTrue(cs.equalsExact(copy));
    assertEquals(Geometry.TYPENAME_CIRCULARSTRING, copy.getGeometryType());

    CircularString rev = cs.reverse();
    assertEquals(Geometry.TYPENAME_CIRCULARSTRING, rev.getGeometryType());
    assertEquals("CIRCULARSTRING (1 0, 1 1, 0 0)", writer.write(rev));
  }

  public void testChainedWindows() throws Exception {
    CircularString cs = (CircularString) reader.read(
        "CIRCULARSTRING (0 0, 1 1, 1 0, 2 1, 2 0)");
    assertEquals(5, cs.getNumControlPoints());
    assertEquals(2, cs.getNumArcs());
    assertFalse(cs.getArcN(0).isCollinear());
    assertFalse(cs.getArcN(1).isCollinear());
    LineString linear = cs.toLinear();
    assertTrue(linear.getCoordinateN(0).equals2D(new Coordinate(0, 0)));
    assertTrue(linear.getCoordinateN(linear.getNumPoints() - 1).equals2D(new Coordinate(2, 0)));
  }

  public void testEqualsExactRequiresCircularString() throws Exception {
    CircularString cs = (CircularString) reader.read("CIRCULARSTRING (0 0, 1 0, 2 0)");
    LineString line = factory.createLineString(cs.getControlPoints());
    assertFalse(cs.equalsExact(line));
  }
}
