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
package org.locationtech.jts.geom.curved;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.curved.CurvedWKTReader;
import org.locationtech.jts.io.curved.CurvedWKTWriter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests for the member-aware {@link CompoundCurve} representation
 * introduced in the compoundcurve-members branch — the read path keeps
 * members instead of flattening, the write path emits a SFA-tagged
 * member list, and the round trip preserves member subtypes.
 */
public class CompoundCurveMembersTest extends GeometryTestCase {

  public static void main(String[] args) { TestRunner.run(suite()); }
  public static Test suite() { return new TestSuite(CompoundCurveMembersTest.class); }
  public CompoundCurveMembersTest(String name) { super(name); }

  /** Reading the SFA-structured form keeps each member at its original
   *  subtype: CircularString stays a CircularString, plain LineString
   *  stays a plain LineString. */
  public void testReaderPreservesMemberSubtypes() throws Exception {
    String wkt = "COMPOUNDCURVE ("
        + "CIRCULARSTRING (0 0, 5 10, 10 0), "
        + "(10 0, 15 5, 20 0), "
        + "CIRCULARSTRING (20 0, 25 -5, 30 0))";
    Geometry g = new CurvedWKTReader().read(wkt);
    assertTrue(g instanceof CompoundCurve);
    CompoundCurve cc = (CompoundCurve) g;
    assertEquals(3, cc.getNumCurves());
    assertEquals("CircularString", cc.getCurveN(0).getGeometryType());
    assertEquals("LineString",     cc.getCurveN(1).getGeometryType());
    assertEquals("CircularString", cc.getCurveN(2).getGeometryType());
  }

  /** The flat round-trip form (single coord list) still parses, as a
   *  single LineString member — backward compatibility for output from
   *  the pre-Phase-3 writer or third-party tools. */
  public void testReaderToleratesFlatFallback() throws Exception {
    String wkt = "COMPOUNDCURVE (0 0, 1 1, 2 2, 3 3)";
    Geometry g = new CurvedWKTReader().read(wkt);
    CompoundCurve cc = (CompoundCurve) g;
    assertEquals(1, cc.getNumCurves());
    assertEquals("LineString", cc.getCurveN(0).getGeometryType());
    assertEquals(4, cc.getCurveN(0).getNumPoints());
  }

  /** Writer emits the SFA-tagged member form when the CompoundCurve
   *  has structured members. */
  public void testWriterEmitsTaggedMemberForm() throws Exception {
    String wkt = "COMPOUNDCURVE ("
        + "CIRCULARSTRING (0 0, 5 10, 10 0), "
        + "(10 0, 15 5, 20 0))";
    Geometry g = new CurvedWKTReader().read(wkt);
    String emitted = new CurvedWKTWriter().write(g);
    String upper = emitted.toUpperCase();
    assertTrue("expected COMPOUNDCURVE prefix in: " + emitted,
        upper.contains("COMPOUNDCURVE"));
    assertTrue("expected nested CIRCULARSTRING tag in: " + emitted,
        upper.contains("CIRCULARSTRING"));
    // Member separation: a comma between the inner CIRCULARSTRING(...)
    // and the bare LineString body.
    int csIdx = upper.indexOf("CIRCULARSTRING");
    int firstClose = emitted.indexOf(")", csIdx);
    assertTrue("expected ',' between members after CIRCULARSTRING(...)",
        firstClose > 0 && emitted.indexOf(",", firstClose) > firstClose);
  }

  /** Full round-trip preserves the member structure exactly. */
  public void testFullRoundTripPreservesMembers() throws Exception {
    String wkt = "COMPOUNDCURVE ("
        + "CIRCULARSTRING (0 0, 5 10, 10 0), "
        + "(10 0, 15 5, 20 0), "
        + "CIRCULARSTRING (20 0, 25 -5, 30 0))";
    Geometry first = new CurvedWKTReader().read(wkt);
    String emitted = new CurvedWKTWriter().write(first);
    Geometry second = new CurvedWKTReader().read(emitted);
    CompoundCurve cc1 = (CompoundCurve) first;
    CompoundCurve cc2 = (CompoundCurve) second;
    assertEquals(cc1.getNumCurves(), cc2.getNumCurves());
    for (int i = 0; i < cc1.getNumCurves(); i++) {
      assertEquals("member subtype mismatch at " + i,
          cc1.getCurveN(i).getGeometryType(),
          cc2.getCurveN(i).getGeometryType());
    }
  }

  /** Empty CompoundCurve round-trips and reports zero members. */
  public void testEmptyHasZeroMembers() throws Exception {
    Geometry g = new CurvedWKTReader().read("COMPOUNDCURVE EMPTY");
    CompoundCurve cc = (CompoundCurve) g;
    assertTrue(cc.isEmpty());
    assertEquals(0, cc.getNumCurves());
    String emitted = new CurvedWKTWriter().write(cc);
    assertTrue(emitted.toUpperCase().contains("EMPTY"));
  }

  /** copy() preserves both the CompoundCurve subclass and its member
   *  array (each member is itself deep-copied). */
  public void testCopyPreservesMembers() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "COMPOUNDCURVE (CIRCULARSTRING (0 0, 5 10, 10 0), (10 0, 20 0))");
    CompoundCurve original = (CompoundCurve) g;
    Geometry copy = original.copy();
    assertTrue("copy should be a CompoundCurve", copy instanceof CompoundCurve);
    CompoundCurve cc = (CompoundCurve) copy;
    assertEquals(original.getNumCurves(), cc.getNumCurves());
    for (int i = 0; i < original.getNumCurves(); i++) {
      // Same subtype, but different instance.
      assertEquals(original.getCurveN(i).getGeometryType(),
          cc.getCurveN(i).getGeometryType());
      assertNotSame("members should be deep-copied",
          original.getCurveN(i), cc.getCurveN(i));
    }
  }

  /** Parent LineString view (getCoordinates / getNumPoints) reflects
   *  the concatenation of members with shared endpoints deduplicated. */
  public void testParentLineStringViewIsConcatenated() throws Exception {
    Geometry g = new CurvedWKTReader().read(
        "COMPOUNDCURVE (CIRCULARSTRING (0 0, 5 10, 10 0), (10 0, 20 0))");
    CompoundCurve cc = (CompoundCurve) g;
    // Member 0: 3 points, member 1: 2 points; shared endpoint dedupe -> 4.
    assertEquals(4, cc.getNumPoints());
    LineString member0 = cc.getCurveN(0);
    LineString member1 = cc.getCurveN(1);
    assertEquals(3, member0.getNumPoints());
    assertEquals(2, member1.getNumPoints());
  }
}
