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
package org.locationtech.jts.spec.curveawareness;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.curved.CircularString;
import org.locationtech.jts.geom.curved.CompoundCurve;
import org.locationtech.jts.geom.curved.CurvePolygon;
import org.locationtech.jts.geom.curved.Linearizable;
import org.locationtech.jts.io.curved.CurvedWKTReader;
import org.locationtech.jts.io.curved.CurvedWKTWriter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Focused spec suite (post-ship) for sub-issue <strong>F-CP</strong> of the SFA Curve
 * Awareness epic (locationtech/jts#1195): <em>structural CurvePolygon</em>.
 *
 * <p>The implementation landed under Option A. The original red-test methods
 * were deleted per epic convention (see the feat commit and SPEC_F_CP.md).
 * What remains are two executable documentation tests:
 * <ul>
 *   <li>{@link #test_FCP_DOVE_legacyPolygonApiContractDecision()} — records the
 *       chosen Option A dovetail contract (legacy Polygon API returns densified
 *       LinearRing views; new curve accessors expose structure).</li>
 *   <li>{@link #test_FCP_EQ_equalityIsViewBasedNotStructural()} — records that
 *       {@code equalsExact} remains view-based (phase-1; structural curves do
 *       not participate; R-EQ is deferred).</li>
 * </ul>
 *
 * <p>Main epic progress is tracked in {@link CurveAwarenessSpecTest}.
 *
 * <p>Oracle note: F-CP is a <em>structural</em> foundation TAG (curved rings are
 * preserved through the {@code CurvePolygon} type, with the Option A dovetail for
 * the legacy Polygon API). Its contract is about representation and API shape, not
 * a geometric quantity the NetTopologySuite.Proofs oracle computes, so there is no
 * oracle vector pin here. The arc geometry these structures carry is oracle-pinned
 * on the downstream numeric TAGs (M-AREA-CP area, C-AREA centroid, V-CP validity,
 * R-PR relate, etc.).
 */
public class CurvePolygonStructuralSpec extends GeometryTestCase {

  public static void main(String[] args) { TestRunner.run(suite()); }
  public static Test suite() { return new TestSuite(CurvePolygonStructuralSpec.class); }
  public CurvePolygonStructuralSpec(String name) { super(name); }

  // A compound shell formed of two semi-circles, plus one linear hole.
  private static final String WKT_CP_COMPOUND_SHELL_WITH_HOLE =
      "CURVEPOLYGON ("
      + "COMPOUNDCURVE ("
      +   "CIRCULARSTRING (0 0, 5 5, 10 0), "
      +   "CIRCULARSTRING (10 0, 5 -5, 0 0)"
      + "), "
      + "(2 -1, 8 -1, 8 1, 2 1, 2 -1)"
      + ")";

  // A circular-string shell (single-arc closed ring) with no holes.
  private static final String WKT_CP_ARC_SHELL =
      "CURVEPOLYGON (CIRCULARSTRING (0 0, 10 0, 5 5, 0 5, 0 0))";

  // A CurvePolygon with a curved hole as well as a curved shell.
  private static final String WKT_CP_CURVED_HOLE =
      "CURVEPOLYGON ("
      + "(0 0, 100 0, 100 100, 0 100, 0 0), "
      + "CIRCULARSTRING (40 50, 50 60, 60 50, 50 40, 40 50)"
      + ")";

  private CurvePolygon readCurvePolygon(String wkt) throws Exception {
    Geometry g = new CurvedWKTReader().read(wkt);
    assertTrue("Reader must produce a CurvePolygon, got " + g.getClass().getSimpleName(),
        g instanceof CurvePolygon);
    return (CurvePolygon) g;
  }

  // FCP-* tests removed (implementation of structural CurvePolygon + reader/writer/copy/toLinear
  // landed using Option A; see SPEC_F_CP.md). DOVE test below kept as executable documentation
  // of the chosen contract. Main epic progress tracked in CurveAwarenessSpecTest.

  // ============================================================
  // FCP-DOVE — legacy Polygon API contract (epic §7 risk #1)
  // ============================================================

  /**
   * Executable documentation of the landed Option A contract (see SPEC_F_CP.md
   * and the F-CP implementation PR).
   *
   * <p>CurvePolygon extends {@link Polygon}, so {@code getExteriorRing()} must
   * return a {@code LinearRing} for legacy callers. Option A returns a
   * {@code LinearRing} built from the control-point polyline of the structural
   * ring (phase-1 "linear view"; {@code toLinear(0.0)} currently returns the
   * raw control points with no arc tessellation — tolerance is a no-op in
   * phase 1). Curve-aware code uses {@code getExteriorCurve()}.
   *
   * <p>The table below is retained for historical context (the three options
   * considered in the spike). The implementation chose A.
   */
  public void test_FCP_DOVE_legacyPolygonApiContractDecision() throws Exception {
    CurvePolygon cp = readCurvePolygon(WKT_CP_COMPOUND_SHELL_WITH_HOLE);
    // Option-A assertion: getExteriorCurve() is the structural accessor,
    // getExteriorRing() keeps returning a LinearRing for legacy callers.
    LineString structural = cp.getExteriorCurve();
    LinearRing legacy = cp.getExteriorRing();
    assertNotNull("FCP-DOVE Option-A: getExteriorCurve() returns the structural shell",
        structural);
    assertNotNull("FCP-DOVE Option-A: getExteriorRing() remains usable by legacy callers",
        legacy);
    assertTrue("FCP-DOVE Option-A: structural shell is a Curve (Compound or Circular), got "
        + structural.getClass().getSimpleName(),
        structural instanceof CompoundCurve || structural instanceof CircularString);
  }

  // ============================================================
  // FCP-EQ — equality/identity is based only on densified views
  // (documents EPIC §7 "equalsExact" open question / R-EQ).
  // Structural curves are *not* part of equalsExact or hashCode.
  // ============================================================

  /**
   * Documents that {@code equalsExact} (and thus structural equality) on
   * {@code CurvePolygon} is inherited from {@code Polygon} and only looks at
   * the densified {@code LinearRing} views. Different structural curves that
   * densify to the same rings compare equal. This matches the current
   * "silent inheritance" after adding structural state; arc-aware equality
   * is deferred to the R-EQ TAG.
   */
  public void test_FCP_EQ_equalityIsViewBasedNotStructural() throws Exception {
    // Arc-shelled CP (structural shell is a CircularString)
    CurvePolygon cpCurved = readCurvePolygon(WKT_CP_ARC_SHELL);

    // Equivalent CP constructed using the control-point view as its "structural"
    // (i.e. a plain LinearRing; the legacy ctor path)
    LinearRing viewShell = cpCurved.getExteriorRing();
    CurvePolygon cpViewBased = new CurvePolygon(viewShell, new LinearRing[0], cpCurved.getFactory());

    // They compare equalExact because their views match (current behaviour)
    assertTrue("CurvePoly with curved structural equalsExact one whose structural is the equivalent linear view (views only)",
        cpCurved.equalsExact(cpViewBased));

    // But curve-aware code can still distinguish via the accessors
    assertTrue("curved one exposes CircularString structural",
        cpCurved.getExteriorCurve() instanceof CircularString);
    assertTrue("view-based one exposes LinearRing as structural",
        cpViewBased.getExteriorCurve() instanceof LinearRing);

    // A plain Polygon from the same view does NOT equalExact the CurvePoly
    // (isEquivalentClass requires exact class match for Polygons)
    Polygon plain = (Polygon) cpCurved.toLinear(0.0);
    assertFalse("CurvePolygon must not equalExact a plain Polygon even with identical densified rings",
        cpCurved.equalsExact(plain));

    // Direct view comparison would succeed, of course
    assertTrue("the views themselves are equalExact",
        cpCurved.getExteriorRing().equalsExact(plain.getExteriorRing()));
  }

}
