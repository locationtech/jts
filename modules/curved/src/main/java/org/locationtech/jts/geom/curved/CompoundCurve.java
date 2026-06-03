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

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * A connected sequence of {@link LineString} and {@link CircularString}
 * segments — the OGC SFA / ISO 19125-2 {@code COMPOUNDCURVE} type.
 *
 * <p>The members are stored as a {@link LineString} array (with
 * {@code CircularString} instances appearing as members where the
 * corresponding source segment was an arc). The parent
 * {@link LineString}'s coordinate sequence is the concatenation of all
 * member control points (with shared endpoints deduplicated), so callers
 * who use only the {@code LineString} API see a sensible polyline view;
 * callers who want to inspect or render the compound structure use
 * {@link #getCurves()}, {@link #getNumCurves()}, {@link #getCurveN(int)}.
 *
 * <p>The legacy single-arg constructor that takes a
 * {@code CoordinateSequence} (with no member structure) is preserved
 * for two cases: (1) the lenient flat-form fallback used by
 * {@code CurvedWKTReader} when round-tripping output from this writer
 * pre-Phase-3, and (2) third-party callers that built up CompoundCurves
 * before member preservation existed. In both cases the resulting
 * {@code CompoundCurve} reports {@link #getNumCurves()} as 1, with the
 * single member being a plain {@code LineString} carrying all the
 * coordinates.
 */
public class CompoundCurve extends LineString implements Linearizable {
  private static final long serialVersionUID = 2L;

  private final LineString[] members;

  /**
   * Member-aware constructor. Each entry is either a {@code LineString}
   * (straight segment) or a {@code CircularString} (arc segment).
   * Adjacent members must share an endpoint per OGC SFA, but the
   * constructor does not enforce this — validation is deferred to a
   * later phase.
   */
  public CompoundCurve(LineString[] members, GeometryFactory factory) {
    super(concatenateMembers(members, factory), factory);
    this.members = members.clone();
  }

  /**
   * Legacy flat-form constructor. Wraps {@code points} as the single
   * member of this CompoundCurve. Use the array constructor when member
   * structure (lines vs arcs) needs to be preserved.
   */
  public CompoundCurve(CoordinateSequence points, GeometryFactory factory) {
    super(points, factory);
    this.members = new LineString[] { factory.createLineString(points.copy()) };
  }

  @Override
  public String getGeometryType() {
    return "CompoundCurve";
  }

  /**
   * B-CC guard: explicit override asserting the standard lineal boundary contract
   * for structural CompoundCurve.
   *
   * <p>CompoundCurve is a 1D lineal (per SFA/SQL-MM). Therefore it inherits
   * LineString's boundary semantics via BoundaryOp:
   * <ul>
   *   <li>Open (first coord != last coord): MultiPoint( startPoint, endPoint )</li>
   *   <li>Closed (or empty): empty MultiPoint (or startPoint for certain
   *       BoundaryNodeRule configurations on valence-2 closed endpoints).</li>
   * </ul>
   *
   * <p>RED-FIRST SEAM IDENTIFICATION (RGR for B-CC):
   * <ul>
   *   <li>Seam: because CompoundCurve extends LineString, getBoundary() was
   *       inherited and "just worked" on the concatenated control seq (whose
   *       endpoints are the true overall start/end thanks to member concat
   *       dropping only internal junctions).</li>
   *   <li>Why explicit guard now: with Phase-1 structural members (getNumCurves,
   *       getCurveN, copyInternal etc.), we want the boundary contract to be
   *       first-class and asserted in the subclass. This prevents accidental
   *       regression if the parent seq view or LinearString boundary logic
   *       ever changes, and documents the intent (curved lineals deliberately
   *       use point boundaries, not "arc boundaries").</li>
   *   <li>Endpoints: the overall curve start is first vertex of first member;
   *       end is last vertex of last member. The parent seq view matches this,
   *       so super.getBoundary() is semantically and numerically identical.</li>
   *   <li>No core change (BoundaryOp stays in core; we just opt into its
   *       logic explicitly). Pure jts-curved.</li>
   *   <li>CS gets the same for free (also extends LineString); an explicit
   *       guard there is symmetric but out of scope for this TAG.</li>
   * </ul>
   * Green: the one-line delegation below (with structural comment). Verification
   * lives in CompoundCurveMembersTest (open/closed cases, coord checks).
   * Meter red marker left with fail("TAG: B-CC...") per §5 (delete on ship).
   */
  @Override
  public Geometry getBoundary() {
    // B-CC guard: deliberately use (and assert) the inherited LineString /
    // BoundaryOp lineal boundary rules for this structural CompoundCurve.
    // The coord seq passed to super already encodes the correct overall
    // endpoints (member concatenation preserves first-of-first and
    // last-of-last). This override makes the contract visible and robust
    // against future internal representation changes.
    return super.getBoundary();
  }

  /**
   * M-LEN-CC: CompoundCurve.getLength sums the lengths of its members.
   *
   * <p>For {@link CircularString} members this is the analytical arc length
   * (r*theta, from M-LEN-CS). For plain {@link LineString} members it is the
   * standard chord length. This gives the correct total length for a mixed
   * COMPOUNDCURVE without densifying arcs to chords first.
   *
   * <p>RED-FIRST SEAM (RGR for M-LEN-CC; very low risk after F-MC structural
   * members + M-LEN-CS):
   * <ul>
   *   <li>Seam: LineString.getLength() (inherited) always sums the *flat*
   *       control-point chords of the concatenated seq. For a CC with arc
   *       members the control seq has the arc controls, so chord sum undercounts.</li>
   *   <li>With member structure (this class post compoundcurve-members): we have
   *       getNumCurves() / getCurveN(int) returning the original typed segments
   *       (some CircularString, some LineString).</li>
   *   <li>Delegate: each member's getLength() is already correct (CircularString
   *       overrides with exact; LineString is chord-correct for its segment).</li>
   *   <li>Legacy ctor path: falls back to 1-member LineString, length == flat
   *       (correct for pure-linear CCs, and for any pre-structural callers).</li>
   *   <li>No core change; no new math (reuses the exact fn inside CircularString
   *       and CurveRefRunner).</li>
   * </ul>
   * Green: the one-line loop below. Verification can be added to adversarial
   * (reuse arc-length vectors) or CompoundCurveMembersTest. Meter red marker
   * left with fail("TAG: M-LEN-CC...") per §5 convention (delete on ship).
   */
  @Override
  public double getLength() {
    double len = 0.0;
    for (int i = 0; i < members.length; i++) {
      len += members[i].getLength();
    }
    return len;
  }

  /** Number of segment members in this CompoundCurve. Always &ge; 1
   *  for non-empty instances. */
  public int getNumCurves() {
    return members.length;
  }

  /** The {@code n}-th member ({@link LineString} or
   *  {@link CircularString}). */
  public LineString getCurveN(int n) {
    return members[n];
  }

  /** A defensive copy of the member array. */
  public LineString[] getCurves() {
    return members.clone();
  }

  @Override
  protected CompoundCurve copyInternal() {
    LineString[] copy = new LineString[members.length];
    for (int i = 0; i < members.length; i++) {
      copy[i] = (LineString) members[i].copy();
    }
    return new CompoundCurve(copy, getFactory());
  }

  /**
   * Linearises every member and concatenates the result into a single
   * {@link LineString}. Members that implement {@link Linearizable}
   * (e.g. {@link CircularString}) are densified per their own contract;
   * plain {@code LineString} members are copied as-is.
   */
  @Override
  public Geometry toLinear(double tolerance) {
    if (members.length == 0) {
      return getFactory().createLineString();
    }
    List<Coordinate> all = new ArrayList<Coordinate>();
    for (int i = 0; i < members.length; i++) {
      LineString member = members[i];
      LineString linear;
      if (member instanceof Linearizable) {
        Geometry g = ((Linearizable) member).toLinear(tolerance);
        linear = (g instanceof LineString) ? (LineString) g
                                            : getFactory().createLineString(g.getCoordinates());
      } else {
        linear = member;
      }
      Coordinate[] coords = linear.getCoordinates();
      // Drop the leading vertex of every member after the first to
      // avoid duplicating the shared endpoint between adjacent
      // segments.
      int from = (i == 0) ? 0 : 1;
      for (int k = from; k < coords.length; k++) {
        all.add(coords[k]);
      }
    }
    return getFactory().createLineString(all.toArray(new Coordinate[0]));
  }

  /**
   * Concatenate every member's control points into one sequence,
   * deduplicating the shared endpoint between adjacent members. Used
   * to feed the parent {@link LineString} so its
   * {@code getCoordinateSequence} / {@code getCoordinates} continues to
   * return a sensible polyline view.
   */
  private static CoordinateSequence concatenateMembers(LineString[] members, GeometryFactory factory) {
    if (members == null || members.length == 0) {
      return factory.getCoordinateSequenceFactory().create(0, 2);
    }
    List<Coordinate> all = new ArrayList<Coordinate>();
    for (int i = 0; i < members.length; i++) {
      Coordinate[] coords = members[i].getCoordinates();
      int from = (i == 0) ? 0 : 1;
      for (int k = from; k < coords.length; k++) {
        all.add(coords[k]);
      }
    }
    return factory.getCoordinateSequenceFactory().create(all.toArray(new Coordinate[0]));
  }
}
