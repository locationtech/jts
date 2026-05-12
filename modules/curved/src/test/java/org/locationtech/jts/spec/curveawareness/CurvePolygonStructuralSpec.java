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

  // ============================================================
  // FCP-S — shell is a Curve, not a LinearRing
  // ============================================================

  /**
   * The CurvePolygon shell is exposed as a {@code CompoundCurve}, retaining
   * its arc-aware identity. The current Phase-1 reader collapses the
   * compound shell to a flat {@code LinearRing} on read.
   */
  public void test_FCP_S_shellIsExposedAsCompoundCurve() throws Exception {
    CurvePolygon cp = readCurvePolygon(WKT_CP_COMPOUND_SHELL_WITH_HOLE);
    Geometry shell = structuralShellOf(cp);
    assertTrue("FCP-S: shell should be a CompoundCurve, got "
        + shell.getClass().getSimpleName(), shell instanceof CompoundCurve);
  }

  /**
   * A CIRCULARSTRING-shelled CurvePolygon exposes its shell as a
   * {@code CircularString}, not a {@code LinearRing}.
   */
  public void test_FCP_S_singleArcShellIsExposedAsCircularString() throws Exception {
    CurvePolygon cp = readCurvePolygon(WKT_CP_ARC_SHELL);
    Geometry shell = structuralShellOf(cp);
    assertTrue("FCP-S: arc shell should be a CircularString, got "
        + shell.getClass().getSimpleName(), shell instanceof CircularString);
  }

  // ============================================================
  // FCP-MEM — shell preserves member subtypes
  // ============================================================

  /**
   * The CompoundCurve shell's members each retain their subtype. Today the
   * Phase-1 reader collapses everything to flat coordinates.
   */
  public void test_FCP_MEM_shellMembersRetainSubtypes() throws Exception {
    CurvePolygon cp = readCurvePolygon(WKT_CP_COMPOUND_SHELL_WITH_HOLE);
    Geometry shell = structuralShellOf(cp);
    assertTrue(shell instanceof CompoundCurve);
    CompoundCurve cc = (CompoundCurve) shell;
    assertEquals("FCP-MEM: shell should have two members", 2, numMembers(cc));
    assertTrue("FCP-MEM: member 0 should be a CircularString",
        memberOf(cc, 0) instanceof CircularString);
    assertTrue("FCP-MEM: member 1 should be a CircularString",
        memberOf(cc, 1) instanceof CircularString);
  }

  // ============================================================
  // FCP-H — interior rings are also Curves
  // ============================================================

  /**
   * Per OGC SFA / ISO 19125-2 §6.1.10, a CurvePolygon's interior rings can
   * themselves be curves. The structural CurvePolygon must expose each hole
   * as a Curve (LineString or CircularString or CompoundCurve), not as a
   * flat LinearRing.
   */
  public void test_FCP_H_curvedHoleIsExposedAsCircularString() throws Exception {
    CurvePolygon cp = readCurvePolygon(WKT_CP_CURVED_HOLE);
    assertEquals("FCP-H: CurvePolygon has one interior ring", 1, cp.getNumInteriorRing());
    Geometry hole = structuralHoleOf(cp, 0);
    assertTrue("FCP-H: curved hole should be a CircularString, got "
        + hole.getClass().getSimpleName(), hole instanceof CircularString);
  }

  // ============================================================
  // FCP-CP — copy() preserves curve identity of shell + holes
  // ============================================================

  /**
   * {@code copyInternal()} preserves the shell + holes as Curves. The
   * current Phase-1 override deep-copies the linearised LinearRings.
   */
  public void test_FCP_CP_copyPreservesCurveIdentityOfShellAndHoles() throws Exception {
    CurvePolygon cp = readCurvePolygon(WKT_CP_COMPOUND_SHELL_WITH_HOLE);
    CurvePolygon copy = (CurvePolygon) cp.copy();
    Geometry origShell = structuralShellOf(cp);
    Geometry copyShell = structuralShellOf(copy);
    assertTrue("FCP-CP: original shell must be a CompoundCurve (structural form), got "
        + origShell.getClass().getSimpleName(), origShell instanceof CompoundCurve);
    assertTrue("FCP-CP: copied shell must also be a CompoundCurve, got "
        + copyShell.getClass().getSimpleName(), copyShell instanceof CompoundCurve);
    assertNotSame("FCP-CP: copy must be a deep copy of the shell", origShell, copyShell);
  }

  // ============================================================
  // FCP-TL — toLinear walks shell and holes
  // ============================================================

  /**
   * {@code toLinear(tolerance)} on a structural CurvePolygon returns a
   * flat {@code Polygon} whose shell is the shell's own {@code toLinear}
   * (densified at the given tolerance) and whose holes are each hole's
   * {@code toLinear}.
   */
  public void test_FCP_TL_linearisationWalksShellAndHoles() throws Exception {
    CurvePolygon cp = readCurvePolygon(WKT_CP_COMPOUND_SHELL_WITH_HOLE);
    Geometry flat = ((Linearizable) cp).toLinear(0.01);
    assertEquals("FCP-TL: result is a Polygon", "Polygon", flat.getGeometryType());
    Polygon p = (Polygon) flat;
    // The compound shell densifies into >> 5 chord coords at 1% tolerance;
    // the linear hole keeps its 5.
    assertTrue("FCP-TL: shell should be densified beyond the compound's control points "
        + "(got " + p.getExteriorRing().getNumPoints() + " points)",
        p.getExteriorRing().getNumPoints() > 10);
    assertEquals("FCP-TL: linear hole should pass through unchanged",
        5, p.getInteriorRingN(0).getNumPoints());
  }

  // ============================================================
  // FCP-WKT — round-trip preserves the structural tag
  // ============================================================

  /**
   * Writing a structural CurvePolygon to WKT and re-reading it produces a
   * geometry of the same structural form. Today the writer emits flat
   * polygon body, so the reader sees flat rings and the round-trip is
   * lossy.
   */
  public void test_FCP_WKT_roundTripPreservesCompoundShell() throws Exception {
    CurvePolygon cp = readCurvePolygon(WKT_CP_COMPOUND_SHELL_WITH_HOLE);
    String emitted = new CurvedWKTWriter().write(cp);
    assertTrue("FCP-WKT: emitted WKT must contain the COMPOUNDCURVE tag inside the body, "
        + "got: " + emitted, emitted.toUpperCase().contains("COMPOUNDCURVE"));
    CurvePolygon roundTripped = (CurvePolygon) new CurvedWKTReader().read(emitted);
    Geometry shell = structuralShellOf(roundTripped);
    assertTrue("FCP-WKT: round-tripped shell must remain a CompoundCurve, got "
        + shell.getClass().getSimpleName(), shell instanceof CompoundCurve);
  }

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
    fail("FCP-DOVE: a structural CurvePolygon needs a named accessor that returns "
        + "the CompoundCurve shell. Pick option A (new getExteriorCurve()), B (widen "
        + "getExteriorRing() to LineString), or C (fail-fast on getExteriorRing()) "
        + "and record the choice in modules/curved/spec/SPEC_F_CP.md before this "
        + "test is replaced by the implementation. Today getExteriorRing() returns "
        + "a flat LinearRing of densified chord coordinates: "
        + cp.getExteriorRing().getClass().getSimpleName());
  }

  // ============================================================
  // Helpers — abstract over the chosen accessor so the FCP-DOVE
  // decision can flip in one place without rewriting every test.
  // ============================================================

  /**
   * Returns the structural shell of the given CurvePolygon. Once F-CP lands
   * this delegates to whatever named accessor the {@code FCP-DOVE} decision
   * settled on (A: {@code getExteriorCurve()}; B: {@code getExteriorRing()}
   * widened; C: a new method on CurvePolygon itself).
   *
   * <p>Today it returns {@code cp.getExteriorRing()} so the structural-
   * preservation tests fail at the structural-class assertion rather than
   * the accessor-missing one.
   */
  private static Geometry structuralShellOf(CurvePolygon cp) {
    return cp.getExteriorRing();
  }

  private static Geometry structuralHoleOf(CurvePolygon cp, int i) {
    return cp.getInteriorRingN(i);
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
}
