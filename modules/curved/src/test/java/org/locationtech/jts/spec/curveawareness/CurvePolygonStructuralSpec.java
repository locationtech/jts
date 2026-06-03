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
import org.locationtech.jts.geom.curved.CurvedGeometryFactory;
import org.locationtech.jts.geom.curved.Linearizable;
import org.locationtech.jts.io.curved.CurvedWKTReader;
import org.locationtech.jts.io.curved.CurvedWKTWriter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Focused red-test suite for sub-issue <strong>F-CP</strong> of the SFA Curve
 * Awareness epic (locationtech/jts#1195): <em>structural CurvePolygon</em> —
 * a {@code CurvePolygon} whose shell and holes are {@code CompoundCurve}s
 * (and/or {@code CircularString}s) rather than flat {@code LinearRing}s.
 *
 * <p>The single {@code test_F_CP_*} method in {@link CurveAwarenessSpecTest}
 * documents the headline gap. This spec drills into the sub-questions an
 * F-CP implementation must answer:
 *
 * <ol>
 *   <li><b>FCP-S</b>: shell is exposed as a Curve, not a LinearRing.</li>
 *   <li><b>FCP-MEM</b>: the shell preserves member subtypes (CircularString
 *       vs LineString segments).</li>
 *   <li><b>FCP-H</b>: interior rings (holes) are also Curves per spec.</li>
 *   <li><b>FCP-CP</b>: {@code copyInternal()} preserves shell + holes as
 *       Curves, not as flat LinearRings.</li>
 *   <li><b>FCP-TL</b>: {@code toLinear(tolerance)} returns a flat
 *       {@code Polygon} assembled from each ring's own linearisation.</li>
 *   <li><b>FCP-WKT</b>: WKT round-trip preserves the structural ring tag
 *       (no degradation through write→read).</li>
 *   <li><b>FCP-DOVE</b>: dovetail with the legacy {@code Polygon} API —
 *       the design-decision question called out in epic §7 risk #1. See
 *       {@link #test_FCP_DOVE_legacyPolygonApiContractDecision()}.</li>
 * </ol>
 *
 * <p>Per the epic convention, every {@code fail("FCP-…: …")} message names
 * the sub-issue tag. When F-CP lands, delete the methods covered by the
 * implementation (do not edit them green); the remaining-method count in
 * this class is the live progress meter for the F-CP sub-surface, paralleling
 * the role of {@link CurveAwarenessSpecTest} at the epic level.
 *
 * <p><b>Status:</b> red against current main + {@code feature/sfa-curve-extension-points}.
 * Today's Phase-1 stand-in collapses CurvePolygon rings to flat LinearRings
 * on read (see {@link
 * org.locationtech.jts.io.curved.CurvedWKTReader#readCurvePolygonText
 * CurvedWKTReader.readCurvePolygonText}), so every assertion below fails.
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
   * <b>Dovetail decision point</b>. CurvePolygon extends {@link Polygon} in
   * the current type hierarchy, so a structural F-CP implementation must
   * decide what {@link Polygon#getExteriorRing()} returns when the actual
   * shell is a CompoundCurve. Three live options:
   *
   * <table>
   *   <tr><th>Option</th><th>{@code getExteriorRing()}</th><th>Trade-off</th></tr>
   *   <tr>
   *     <td>A — legacy fallback</td>
   *     <td>{@code LinearRing} of densified chord coordinates</td>
   *     <td>Old callers keep working but see a polyline approximation;
   *         needs a new {@code getExteriorCurve()} for the structural
   *         {@code CompoundCurve}.</td>
   *   </tr>
   *   <tr>
   *     <td>B — widen return type</td>
   *     <td>{@code LineString} ({@code CompoundCurve} extends LineString)</td>
   *     <td>Direct access to the structural shell, but breaks every caller
   *         that does {@code (LinearRing) p.getExteriorRing()} or relies on
   *         {@code LinearRing}-specific API.</td>
   *   </tr>
   *   <tr>
   *     <td>C — fail-fast</td>
   *     <td>throws {@code UnsupportedOperationException}</td>
   *     <td>Forces every caller to migrate; loudest diagnostic; most painful
   *         interim period.</td>
   *   </tr>
   * </table>
   *
   * <p>This red test does not pick a winner. It asserts only that
   * <em>some</em> structural accessor exists, by name: either
   * {@code getExteriorCurve()} as a new method (option A), or the current
   * {@code getExteriorRing()} returning a Curve (option B), or any other
   * named accessor that the chosen design adds. The chosen design plugs in
   * here; whatever lands, this test then turns green and gets deleted per
   * the epic convention.
   *
   * <p>Smallest concrete next step before F-CP code goes in: pick A, B,
   * or C and write the {@code DESIGN-FCP.md} entry. The companion file
   * {@code modules/curved/spec/SPEC_F_CP.md} captures the trade-offs.
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

    // Equivalent CP constructed using the densified view as its "structural"
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

  // ============================================================
  // Helpers — abstract over the chosen accessor so the FCP-DOVE
  // decision can flip in one place without rewriting every test.
  // ============================================================

  /**
   * Returns the structural shell of the given CurvePolygon. On this
   * Option-A spike branch the helper delegates to
   * {@link CurvePolygon#getExteriorCurve()}; on the Option-B branch it
   * would call the widened {@code getExteriorRing()}; on the Option-C
   * branch it would also call {@code getExteriorCurve()} after
   * {@code getExteriorRing()} starts throwing.
   */
  private static Geometry structuralShellOf(CurvePolygon cp) {
    LineString curve = cp.getExteriorCurve();
    return curve != null ? curve : cp.getExteriorRing();
  }

  private static Geometry structuralHoleOf(CurvePolygon cp, int i) {
    try {
      // Prefer structural if available (our impl + option A)
      return cp.getInteriorCurveN(i);
    } catch (Exception e) {
      return cp.getInteriorRingN(i);
    }
  }

  /**
   * Returns the number of segments in a structural CompoundCurve shell.
   * Bridges to the new {@code getNumCurves()} accessor introduced on
   * {@code feature/sfa-curve-compoundcurve-members}; until F-CP lands and
   * pulls that accessor onto the merge target, falls back to a count of 1.
   */
  private static int numMembers(CompoundCurve cc) {
    try {
      return (Integer) CompoundCurve.class.getMethod("getNumCurves").invoke(cc);
    } catch (Exception e) {
      return 1;
    }
  }

  private static LineString memberOf(CompoundCurve cc, int i) {
    try {
      return (LineString) CompoundCurve.class.getMethod("getCurveN", int.class).invoke(cc, i);
    } catch (Exception e) {
      return cc;
    }
  }

  // ============================================================
  // B-CP verification (green proof for RGR on B-CP TAG)
  // Added during green phase; exercises the override without editing the
  // red TAG fail in CurveAwarenessSpecTest (per "don't integrate yet").
  // When B-CP ships, the meter method is deleted (not turned green).
  // ============================================================

  /**
   * Green verification that CurvePolygon.getBoundary() now uses the F-CP
   * structural curves (CompoundCurve / CircularString members) rather than
   * the densified LinearRing view from the Polygon supertype.
   * <p>
   * This lives in the structural spec (executable doc) so the main
   * CurveAwarenessSpecTest#test_B_CP_* can stay as the red progress meter.
   */
  public void test_B_CP_boundaryUsesStructuralCurvesNotDensifiedView() throws Exception {
    // Use explicit CurvedGeometryFactory so read produces proper structural curves
    // (the default CurvedWKTReader() ctor may pair with plain GF in some paths).
    CurvedWKTReader r = new CurvedWKTReader(new CurvedGeometryFactory());
    Geometry g = r.read(
        "CURVEPOLYGON (COMPOUNDCURVE (CIRCULARSTRING (0 0, 5 5, 10 0), (10 0, 0 0)))");
    assertTrue("reader produced CurvePolygon", g instanceof CurvePolygon);
    CurvePolygon cp = (CurvePolygon) g;

    Geometry boundary = cp.getBoundary();

    // The key B-CP assertion: type is the structural curve, not LinearRing
    assertEquals("B-CP green: boundary of 0-hole compound CP must be CompoundCurve",
        "CompoundCurve", boundary.getGeometryType());
    assertTrue("B-CP green: must still be a LineString subtype (contract)",
        boundary instanceof LineString);
    assertTrue("B-CP green: exact structural type preserved (CompoundCurve not densified)",
        boundary instanceof CompoundCurve);

    // For completeness: a 0-hole circular case also preserves
    CurvePolygon cpArc = (CurvePolygon) r.read(WKT_CP_ARC_SHELL);
    Geometry bArc = cpArc.getBoundary();
    assertEquals("CircularString", bArc.getGeometryType());
  }

  /**
   * Green verification for B-MS (MultiSurface boundary returns MultiCurve when
   * any member is CurvePolygon). Lives here so the meter red test in
   * CurveAwarenessSpecTest remains untouched (explicit fail kept per RGR "don't
   * integrate yet").
   */
  public void test_B_MS_multiSurfaceBoundaryPreservesCurvedRings() throws Exception {
    CurvedWKTReader r = new CurvedWKTReader(new CurvedGeometryFactory());
    // Mix of plain poly + CP (0-hole compound) -- after B-CP the CP contributes
    // a curve to the collected boundary.
    org.locationtech.jts.geom.Geometry ms = r.read(
        "MULTISURFACE (((0 0, 10 0, 10 10, 0 10, 0 0)), "
        + "CURVEPOLYGON (COMPOUNDCURVE (CIRCULARSTRING (20 0, 25 5, 30 0), (30 0, 20 0))))");
    org.locationtech.jts.geom.Geometry b = ms.getBoundary();
    assertEquals("B-MS green: boundary must be MultiCurve (not plain MLS)",
        "MultiCurve", b.getGeometryType());
    // At least one child should be the curve (CompoundCurve) from the CP member
    boolean sawCurve = false;
    for (int i = 0; i < b.getNumGeometries(); i++) {
      if (b.getGeometryN(i) instanceof CompoundCurve || b.getGeometryN(i) instanceof CircularString) {
        sawCurve = true; break;
      }
    }
    assertTrue("B-MS green: collected boundary must preserve at least one curved ring member", sawCurve);

    // Soundness: pure-linear MultiSurface (no CurvePolygon members) should return plain
    // MultiLineString (compat with super) rather than MultiCurve.
    org.locationtech.jts.geom.Geometry pure = r.read(
        "MULTISURFACE (((0 0, 10 0, 10 10, 0 10, 0 0)), ((20 0, 30 0, 30 10, 20 10, 20 0)))");
    org.locationtech.jts.geom.Geometry bPure = pure.getBoundary();
    assertEquals("B-MS refactor soundness: pure linear MS boundary must be MultiLineString",
        "MultiLineString", bPure.getGeometryType());
  }

  // ============================================================
  // V-CP green verifications (arc self-intersect, orientation, holes-in-shell)
  // These live here (and meter red in CurveAwarenessSpecTest kept per convention).
  // ============================================================

  /** Good case: simple curved shell (two half-circles), no holes, correct orientation (the traversal that gives positive signed). */
  public void test_V_CP_validSimpleCurvePolygon() throws Exception {
    CurvedWKTReader r = new CurvedWKTReader(new CurvedGeometryFactory());
    // lower then upper for positive signed in our formula
    org.locationtech.jts.geom.Geometry cp = r.read(
        "CURVEPOLYGON (COMPOUNDCURVE ("
        + "CIRCULARSTRING (0 0, 5 -5, 10 0), "
        + "CIRCULARSTRING (10 0, 5 5, 0 0)))");
    assertTrue("V-CP green: simple closed curved shell must be valid", cp.isValid());
  }

  /** Bad case: self-overlapping (use direct on CS for the V-CP related isSimple, wrapped in CP if possible). */
  public void test_V_CP_invalidArcSelfIntersection() throws Exception {
    CurvedWKTReader r = new CurvedWKTReader(new CurvedGeometryFactory());
    // Use a multi-arc CS known to overlap (from V-CS example); test its isSimple (used by V-CP ring check)
    CircularString overlapping = (CircularString) r.read(
        "CIRCULARSTRING (0 0, 10 5, 20 0, 10 -5, 0 0, -10 5, -20 0)");
    // Note: may not be closed in this string; for isSimple test it exercises the cross logic.
    // To avoid closed LinearRing issues in CP, we just verify the curve lineal simple check (part of V-CP impl).
    // If the impl detects overlap it would return false; current cross logic may or not for this data.
    // We at least assert no crash and isSimple runs.
    boolean simple = overlapping.isSimple();
    assertFalse("V-CP (supports V-CS): self-overlapping multi-arc CircularString must report !isSimple via analytical arc cross detection (plus point revisit)", simple);
  }

  /** Orientation wrong (shell clockwise) should be invalid per sector area. */
  public void test_V_CP_invalidOrientation() throws Exception {
    CurvedWKTReader r = new CurvedWKTReader(new CurvedGeometryFactory());
    // The traversal that gave negative in debug
    org.locationtech.jts.geom.Geometry cw = r.read(
        "CURVEPOLYGON (COMPOUNDCURVE ("
        + "CIRCULARSTRING (0 0, 5 5, 10 0), "
        + "CIRCULARSTRING (10 0, 5 -5, 0 0)))");
    assertFalse("V-CP green: clockwise shell (negative sector area) must be invalid for polygon", cw.isValid());
  }
}
