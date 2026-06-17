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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * A polygon whose rings may be straight, circular, or compound curves.
 *
 * <p>Option A (F-CP / FCP-DOVE per SPEC_F_CP.md): legacy {@code getExteriorRing()}
 * and {@code getInteriorRingN(i)} return {@link LinearRing} views obtained from
 * the control-point polyline of the structural ring (phase-1 linear view).
 * {@code toLinear(0.0)} (and thus the legacy ring views) currently return the
 * raw control points with <b>no arc tessellation</b>; the {@code tolerance}
 * parameter is accepted for {@link Linearizable} compatibility but is a no-op
 * in phase 1. Curve-aware code uses {@link #getExteriorCurve()} /
 * {@link #getInteriorCurveN(int)} to obtain the structural {@link LineString}
 * (which may be a {@link CircularString} or {@link CompoundCurve}).
 *
 * <p><b>Equality / identity semantics (EPIC §7 risk, R-EQ TAG):</b>
 * {@code equalsExact} (and by extension structural equality for use in
 * collections via {@link #equals(Object)} / {@link #hashCode()}) is
 * inherited from {@link Polygon} without override. It compares only the
 * control-point {@link LinearRing} views (via {@code toLinear(0.0)}). The
 * structural curves (shell/holes) are <i>not</i> consulted. Consequently:
 * <ul>
 *   <li>Two {@code CurvePolygon}s whose control polylines are identical
 *       compare equal via {@code equalsExact}, even if the curves themselves
 *       differ in type or parameters.</li>
 *   <li>A {@code CurvePolygon} never {@code equalsExact}s a plain
 *       {@code Polygon} (even with identical control points), because
 *       {@link Polygon#isEquivalentClass(Geometry)} (inherited) requires
 *       exact class name match. (Contrast with {@code LineString} subclasses,
 *       where {@code isEquivalentClass} is lenient via {@code instanceof}.)
 * </ul>
 * This is the current (phase-1) behaviour. Arc-aware / structural equality
 * is explicitly deferred to the R-EQ TAG; see the EPIC and
 * {@code SPEC_F_CP.md}. Tests should not assume structural curves affect
 * equality.
 */
public class CurvePolygon extends Polygon implements Linearizable {
  private static final long serialVersionUID = 1L;

  private final LineString structuralShell;
  private final LineString[] structuralHoles;

  public CurvePolygon(LinearRing shell, LinearRing[] holes, GeometryFactory factory) {
    // Note: ternary in super() arg to keep 'super' as the first *statement* for Java 8 source compat.
    // (Flexible constructor bodies / statements-before-super are a newer language feature.)
    super(shell, (holes == null ? new LinearRing[0] : holes), factory);
    this.structuralShell = shell;
    this.structuralHoles = copyAsLineStrings(holes);
  }

  public CurvePolygon(GeometryFactory factory) {
    super(null, null, factory);
    this.structuralShell = null;
    this.structuralHoles = new LineString[0];
  }

  /**
   * Structural constructor (Option A): accepts shell and holes as any LineString
   * (CircularString / CompoundCurve / LineString / LinearRing). Derives
   * LinearRing views at default tolerance for the Polygon supertype so that
   * legacy callers continue to work.
   */
  public CurvePolygon(LineString structuralShell, LineString[] holes, GeometryFactory factory) {
    super(
        deriveLinearRing(structuralShell, factory),
        deriveLinearRings(holes, factory),
        factory);
    this.structuralShell = structuralShell;
    this.structuralHoles = (holes == null) ? new LineString[0] : holes.clone();
  }

  private static LineString[] copyAsLineStrings(LinearRing[] in) {
    if (in == null || in.length == 0) return new LineString[0];
    LineString[] out = new LineString[in.length];
    System.arraycopy(in, 0, out, 0, in.length);
    return out;
  }

  private static LinearRing deriveLinearRing(LineString s, GeometryFactory f) {
    if (s == null) return null;
    if (s instanceof LinearRing) return (LinearRing) s;
    LineString flat = (s instanceof Linearizable)
        ? (LineString) ((Linearizable) s).toLinear(0.0)
        : s;
    Coordinate[] pts = flat.getCoordinates();
    // A curved ring's control polyline must have enough points to form a
    // non-degenerate ring. A full-circle CIRCULARSTRING has only 3 control
    // points (start, antipode, start), whose linear view collapses to a
    // degenerate "there-and-back" ring; producing it silently would yield an
    // invalid CurvePolygon. Reject it with a clear message rather than building
    // an invalid geometry. (Arc tessellation, which would produce a valid ring,
    // is deferred to a later phase.)
    if (pts.length > 0 && pts.length < 4) {
      throw new IllegalArgumentException(
          "Cannot derive a linear ring view from a curved shell/hole with only " + pts.length
          + " control points (e.g. a full-circle CIRCULARSTRING): the control polyline collapses to a "
          + "degenerate ring. Arc tessellation that would produce a valid ring is not available in this "
          + "phase; supply a curve with at least 3 distinct control points (4 coordinates with closure).");
    }
    return f.createLinearRing(pts);
  }

  private static LinearRing[] deriveLinearRings(LineString[] hs, GeometryFactory f) {
    if (hs == null || hs.length == 0) return new LinearRing[0];
    LinearRing[] out = new LinearRing[hs.length];
    for (int i = 0; i < hs.length; i++) {
      out[i] = deriveLinearRing(hs[i], f);
    }
    return out;
  }

  /**
   * Returns the structural shell (may be CircularString, CompoundCurve,
   * LineString or LinearRing). Null for empty.
   */
  public LineString getExteriorCurve() {
    return structuralShell;
  }

  /**
   * Returns the structural interior ring (may be curved). Index 0..getNumInteriorRing()-1.
   */
  public LineString getInteriorCurveN(int n) {
    return structuralHoles[n];
  }

  @Override
  public String getGeometryType() {
    return "CurvePolygon";
  }

  @Override
  public CurvePolygon reverse() {
    return (CurvePolygon) super.reverse();
  }

  @Override
  protected CurvePolygon reverseInternal() {
    GeometryFactory f = getFactory();
    if (isEmpty() || structuralShell == null) {
      return new CurvePolygon(f);
    }
    LineString revShell = (LineString) structuralShell.reverse();
    LineString[] revHoles = new LineString[structuralHoles.length];
    for (int i = 0; i < structuralHoles.length; i++) {
      revHoles[i] = (LineString) structuralHoles[i].reverse();
    }
    return new CurvePolygon(revShell, revHoles, f);
  }

  @Override
  public void normalize() {
    // Normalize the legacy LinearRing views (shell/holes) per Polygon contract (Option A).
    // toLinear() reads these same inherited rings, so getExteriorRing()/getInteriorRingN()
    // and toLinear(0) stay consistent across normalize().
    //
    // The structural curves (getExteriorCurve()/getInteriorCurveN(), the source of truth for
    // arcs) are intentionally left unchanged: super.normalize() may scroll a ring to its lowest
    // vertex, which is not well defined for arc control points (it would break arc-triple
    // grouping). So after normalize() the structural curve may start at a different vertex than
    // the linear view. Full arc-aware structural normalization is deferred; see SPEC_F_CP.md.
    super.normalize();
  }

  @Override
  protected CurvePolygon copyInternal() {
    GeometryFactory f = getFactory();
    if (isEmpty() || structuralShell == null) return new CurvePolygon(f);
    LineString shellCopy = (LineString) structuralShell.copy();
    LineString[] holeCopies = new LineString[structuralHoles.length];
    for (int i = 0; i < structuralHoles.length; i++) {
      holeCopies[i] = (LineString) structuralHoles[i].copy();
    }
    return new CurvePolygon(shellCopy, holeCopies, f);
  }

  @Override
  public Geometry toLinear(double tolerance) {
    GeometryFactory f = getFactory();
    if (isEmpty() || structuralShell == null) return f.createPolygon();
    // Phase 1: no arc tessellation. The control-point linear view is exactly the
    // inherited Polygon's rings (derived from the structural curves at
    // construction). Returning those -- rather than re-deriving from the
    // structural curves -- keeps toLinear() consistent with getExteriorRing() /
    // getInteriorRingN() through normalize(), which canonicalizes the inherited
    // rings in place but cannot scroll/reorient the structural arcs (deferred).
    // The {@code tolerance} parameter is accepted for Linearizable compatibility
    // but is a no-op in phase 1.
    LinearRing shell = (LinearRing) getExteriorRing().copy();
    LinearRing[] holeRings = new LinearRing[getNumInteriorRing()];
    for (int i = 0; i < holeRings.length; i++) {
      holeRings[i] = (LinearRing) getInteriorRingN(i).copy();
    }
    return f.createPolygon(shell, holeRings);
  }
}
