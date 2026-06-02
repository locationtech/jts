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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * A polygon whose rings may be straight, circular, or compound curves.
 *
 * <p>Option A (F-CP / FCP-DOVE per SPEC_F_CP.md): legacy {@code getExteriorRing()}
 * and {@code getInteriorRingN(i)} return {@link LinearRing} views obtained by
 * linearising at default tolerance (0.0); curve-aware code uses
 * {@link #getExteriorCurve()} / {@link #getInteriorCurveN(int)} to obtain the
 * structural {@link LineString} (which may be a {@link CircularString} or
 * {@link CompoundCurve}).
 *
 * <p><b>Equality / identity semantics (EPIC §7 risk, R-EQ TAG):</b>
 * {@code equalsExact} (and by extension structural equality for use in
 * collections via {@link #equals(Object)} / {@link #hashCode()}) is
 * inherited from {@link Polygon} without override. It compares only the
 * densified {@link LinearRing} views (the chord approximations created at
 * construction time via {@code toLinear(0.0)}). The structural curves
 * (shell/holes) are <i>not</i> consulted. Consequently:
 * <ul>
 *   <li>Two {@code CurvePolygon}s whose curves densify to identical rings
 *       (same control points at tol=0) compare equal via {@code equalsExact},
 *       even if the curves themselves differ in type or parameters.</li>
 *   <li>A {@code CurvePolygon} never {@code equalsExact}s a plain
 *       {@code Polygon} (even with identical flattened rings), because
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
    return f.createLinearRing(flat.getCoordinates());
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
    LineString[] rings = structuralRings();
    if (rings.length == 0) {
      return new CurvePolygon(f);
    }
    LineString revShell = (LineString) rings[0].reverse();
    LineString[] revHoles = new LineString[rings.length - 1];
    for (int i = 0; i < revHoles.length; i++) {
      revHoles[i] = (LineString) rings[i + 1].reverse();
    }
    return new CurvePolygon(revShell, revHoles, f);
  }

  @Override
  public void normalize() {
    // Normalize the legacy LinearRing views (shell/holes) per Polygon contract (Option A).
    // The structural curves (source of truth for arcs) are left unchanged; their densified
    // views are normalized. This avoids losing curved identity while keeping legacy API
    // behaviour (e.g. normalized() rings for getExteriorRing etc.).
    // Full arc-aware structural normalization (e.g. consistent orientation of CircularString
    // members) is deferred; see review notes for SPEC_F_CP.md alignment.
    super.normalize();
  }

  /**
   * Computes the boundary of this CurvePolygon using its structural curves
   * (per F-CP Option A), not the densified LinearRing views.
   * <p>
   * For a simple (0-hole) CurvePolygon the result is the structural exterior
   * curve (CircularString, CompoundCurve, or LinearRing for the all-linear case)
   * -- a LineString subtype. For polygons with holes the result is a MultiCurve
   * containing the structural ring curves (in shell + holes order).
   * This implements B-CP while preserving the Polygon boundary contract for
   * legacy linear CurvePolygons (LR / MLS subtypes where no curves present).
   *
   * @return a lineal geometry (LineString or MultiCurve/MultiLineString subtype)
   * @see Geometry#getBoundary
   */
  @Override
  public Geometry getBoundary() {
    if (isEmpty() || structuralShell == null) {
      return getFactory().createMultiLineString();
    }
    LineString[] rings = structuralRings();
    if (rings.length == 0) {
      return getFactory().createMultiLineString();
    }
    if (rings.length == 1) {
      // 0-hole: return copy of the (only) structural ring. This preserves the
      // exact subtype (LinearRing -> LR via its copyInternal; CompoundCurve,
      // CircularString likewise). Matches the spirit of Polygon's 0-hole path
      // (createLinearRing from seq) while keeping curve identity for B-CP etc.
      return (LineString) rings[0].copy();
    }
    // >=1 hole (or more generally >1 ring): decide container for soundness.
    // If *all* structural rings are LinearRings (possible for a CurvePolygon
    // built via legacy ctor or with explicit LR structs), delegate to super so
    // we return a plain MultiLineString (exact match to Polygon contract for
    // the linear-degenerate case). Otherwise return a MultiCurve (the curve-
    // aware container) even if some members happen to be linear.
    boolean allLinearRings = true;
    for (LineString r : rings) {
      if (!(r instanceof LinearRing)) {
        allLinearRings = false;
        break;
      }
    }
    if (allLinearRings) {
      return super.getBoundary();
    }
    GeometryFactory f = getFactory();
    if (f instanceof CurvedGeometryFactory) {
      return ((CurvedGeometryFactory) f).createMultiCurve(rings);
    }
    return new MultiCurve(rings, f);
  }

  /**
   * Returns the structural rings in boundary order: [shell, hole0, hole1, ...].
   * Never null; length == 0 for empty. Used by getBoundary (B-CP etc) and
   * available for copy/reverse/toLinear if they want to DRY up later.
   */
  private LineString[] structuralRings() {
    if (structuralShell == null) {
      return new LineString[0];
    }
    LineString[] rings = new LineString[structuralHoles.length + 1];
    rings[0] = structuralShell;
    System.arraycopy(structuralHoles, 0, rings, 1, structuralHoles.length);
    return rings;
  }

  @Override
  protected CurvePolygon copyInternal() {
    GeometryFactory f = getFactory();
    LineString[] rings = structuralRings();
    if (rings.length == 0) return new CurvePolygon(f);
    LineString shellCopy = (LineString) rings[0].copy();
    LineString[] holeCopies = new LineString[rings.length - 1];
    for (int i = 0; i < holeCopies.length; i++) {
      holeCopies[i] = (LineString) rings[i + 1].copy();
    }
    return new CurvePolygon(shellCopy, holeCopies, f);
  }

  @Override
  public Geometry toLinear(double tolerance) {
    GeometryFactory f = getFactory();
    if (isEmpty() || structuralShell == null) return f.createPolygon();
    LineString shellFlat = (structuralShell instanceof Linearizable)
        ? (LineString) ((Linearizable) structuralShell).toLinear(tolerance)
        : structuralShell;
    LinearRing shell = f.createLinearRing(shellFlat.getCoordinates());
    LinearRing[] holeRings = new LinearRing[structuralHoles.length];
    for (int i = 0; i < structuralHoles.length; i++) {
      LineString h = structuralHoles[i];
      LineString hflat = (h instanceof Linearizable)
          ? (LineString) ((Linearizable) h).toLinear(tolerance)
          : h;
      holeRings[i] = f.createLinearRing(hflat.getCoordinates());
    }
    return f.createPolygon(shell, holeRings);
  }
}
