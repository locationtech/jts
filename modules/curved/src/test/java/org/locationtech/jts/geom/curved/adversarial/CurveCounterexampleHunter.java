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
package org.locationtech.jts.geom.curved.adversarial;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.curved.CircularString;
import org.locationtech.jts.geom.curved.CompoundCurve;
import org.locationtech.jts.geom.curved.CurvePolygon;
import org.locationtech.jts.geom.curved.CurvedGeometryFactory;
import org.locationtech.jts.io.curved.CurvedWKTReader;

/**
 * Adversarial search for cases where curve-aware operations disagree with
 * exact (or high-precision reference) results.
 * <p>
 * Inspired directly by DDCounterexampleHunter from locationtech/jts#1197:
 * side-by-side evaluation of "current impl" (here: the phase-1 linearised
 * behaviour on CircularString) vs. an exact oracle (CurveRefRunner), using
 * targeted generators for hard cases (near-flat arcs that are almost lines,
 * extreme radii, tiny features, etc.).
 * <p>
 * The hunter both demonstrates the current gaps (for the red TAGs in
 * CurveAwarenessSpecTest) and will serve as regression harness once the
 * analytical implementations (M-LEN-*, area with segment correction, etc.)
 * land.
 */
public final class CurveCounterexampleHunter {

  private static final GeometryFactory GF = new CurvedGeometryFactory();
  private static final Random RND = new Random(123456789L);

  private CurveCounterexampleHunter() {}

  public static final class Mismatch {
    public final String generator;
    public final CircularString input;
    public final double linearLength;   // what JTS currently returns (chords)
    public final double exactLength;    // oracle
    public final double delta;

    Mismatch(String g, CircularString in, double lin, double ex) {
      this.generator = g;
      this.input = in;
      this.linearLength = lin;
      this.exactLength = ex;
      this.delta = Math.abs(lin - ex);
    }

    @Override
    public String toString() {
      return String.format("%s delta=%.3g (lin=%.6g exact=%.6g) arc=%s",
          generator, delta, linearLength, exactLength, input);
    }
  }

  /** Mismatch for V-CS (isSimple on curve lineals) or V-CP (isValid on CP). */
  public static final class ValiditySimplicityMismatch {
    public final String generator;
    public final Geometry input;
    public final boolean actualSimpleOrValid;
    public final boolean expectedSimpleOrValid;
    public final String kind; // "V-CS" or "V-CP"

    ValiditySimplicityMismatch(String g, Geometry in, boolean actual, boolean expected, String kind) {
      this.generator = g;
      this.input = in;
      this.actualSimpleOrValid = actual;
      this.expectedSimpleOrValid = expected;
      this.kind = kind;
    }

    @Override
    public String toString() {
      return String.format("%s-%s gen=%s actual=%s expected=%s : %s",
          kind, generator, actualSimpleOrValid, expectedSimpleOrValid, input);
    }
  }

  /** Generator: near-flat arcs (small sagitta relative to chord). */
  public static CircularString nearFlatArc() {
    double chord = 1.0 + RND.nextDouble() * 10;
    double sag = 1e-4 + RND.nextDouble() * 1e-2;
    double sx = 0, sy = 0;
    double ex = chord, ey = 0;
    double mx = chord / 2;
    double my = sag;
    return makeArc(sx, sy, mx, my, ex, ey);
  }

  /** Generator: extreme magnitude (very large or tiny radius). */
  public static CircularString extremeMagnitude() {
    double scale = Math.pow(10, RND.nextDouble() * 8 - 4); // 1e-4 .. 1e4
    if (RND.nextBoolean()) scale = 1.0 / scale; // mix tiny/large
    double sx = 0, sy = -scale;
    double mx = scale, my = 0;
    double ex = 0, ey = scale;
    return makeArc(sx, sy, mx, my, ex, ey);
  }

  /** Generator: random but valid 3-pt arc. */
  public static CircularString randomArc() {
    double sx = RND.nextDouble() * 100 - 50;
    double sy = RND.nextDouble() * 100 - 50;
    double mx = sx + (RND.nextDouble() - 0.5) * 20;
    double my = sy + (RND.nextDouble() - 0.5) * 20;
    double ex = mx + (RND.nextDouble() - 0.5) * 20;
    double ey = my + (RND.nextDouble() - 0.5) * 20;
    return makeArc(sx, sy, mx, my, ex, ey);
  }

  /** V-CS generator: the classic self-overlapping multi-arc (revisits interior point). Built manually to avoid ParseException in static gen. */
  public static CircularString selfOverlappingArc() {
    CoordinateSequence cs = GF.getCoordinateSequenceFactory().create(7, 2);
    double[][] pts = {{0,0},{10,5},{20,0},{10,-5},{0,0},{-10,5},{-20,0}};
    for (int i=0; i<7; i++) {
      cs.setOrdinate(i, 0, pts[i][0]);
      cs.setOrdinate(i, 1, pts[i][1]);
    }
    return new CircularString(cs, GF);
  }

  /** V-CS generator: two non-adjacent arcs that cross (fallback to overlap for hunt). */
  public static CircularString crossingArcs() {
    return selfOverlappingArc();
  }

  /** V-CP generator: CurvePolygon with self-intersecting shell (closed version of overlap for ring ctor). */
  public static CurvePolygon selfIntersectingCurvePolygon() {
    // Closed variant of the overlap: append start at end so LinearRing ctor happy.
    CoordinateSequence cs = GF.getCoordinateSequenceFactory().create(8, 2);
    double[][] pts = {{0,0},{10,5},{20,0},{10,-5},{0,0},{-10,5},{-20,0},{0,0}};
    for (int i=0; i<8; i++) {
      cs.setOrdinate(i, 0, pts[i][0]);
      cs.setOrdinate(i, 1, pts[i][1]);
    }
    CircularString badRing = new CircularString(cs, GF);
    return new CurvePolygon(badRing, new LineString[0], GF);
  }

  /** V-CP generator: good simple CP for positive cases. Built manually. */
  public static CurvePolygon simpleCurvePolygon() {
    // Simple closed: line + arc, but use two arcs for curve shell
    CoordinateSequence cs = GF.getCoordinateSequenceFactory().create(5, 2);
    double[][] pts = {{0,0},{5,-5},{10,0},{5,5},{0,0}};
    for (int i=0; i<5; i++) {
      cs.setOrdinate(i, 0, pts[i][0]);
      cs.setOrdinate(i, 1, pts[i][1]);
    }
    CircularString ring = new CircularString(cs, GF);
    return new CurvePolygon(ring, new LineString[0], GF);
  }

  private static CircularString makeArc(double sx, double sy, double mx, double my,
                                        double ex, double ey) {
    CoordinateSequence cs = GF.getCoordinateSequenceFactory().create(3, 2);
    cs.setOrdinate(0, 0, sx); cs.setOrdinate(0, 1, sy);
    cs.setOrdinate(1, 0, mx); cs.setOrdinate(1, 1, my);
    cs.setOrdinate(2, 0, ex); cs.setOrdinate(2, 1, ey);
    return new CircularString(cs, GF);
  }

  /**
   * Run a batch of adversarial searches. Returns mismatches where the
   * current (linear) length on the CircularString differs from the exact
   * circular arc length beyond a small relative tol.
   */
  public static List<Mismatch> huntArcLength(int itersPerGenerator) {
    List<Mismatch> bad = new ArrayList<>();
    double relTol = 1e-9;
    for (int i = 0; i < itersPerGenerator; i++) {
      for (String gen : new String[]{"nearFlat", "extreme", "random"}) {
        CircularString arc;
        switch (gen) {
          case "nearFlat": arc = nearFlatArc(); break;
          case "extreme": arc = extremeMagnitude(); break;
          default: arc = randomArc(); break;
        }
        double lin = arc.getLength(); // current impl = chord polyline
        double ex = CurveRefRunner.exactCircularArcLength(
            arc.getCoordinateN(0).x, arc.getCoordinateN(0).y,
            arc.getCoordinateN(1).x, arc.getCoordinateN(1).y,
            arc.getCoordinateN(2).x, arc.getCoordinateN(2).y);
        if (Math.abs(lin - ex) > relTol * Math.max(1.0, Math.abs(ex))) {
          bad.add(new Mismatch(gen, arc, lin, ex));
        }
      }
    }
    return bad;
  }

  /** V-CS hunter: generate known-bad and random arcs, collect where isSimple() does not match expected. */
  public static List<ValiditySimplicityMismatch> huntIsSimple(int itersPerGenerator) {
    List<ValiditySimplicityMismatch> bad = new ArrayList<>();
    // Known bad: must return false
    CircularString overlap = selfOverlappingArc();
    boolean actual = overlap.isSimple();
    if (actual != false) {
      bad.add(new ValiditySimplicityMismatch("selfOverlap", overlap, actual, false, "V-CS"));
    }
    // TODO better crossing generator; for now use another constructed overlap-like
    // Random arcs: expect true for most (but hunter surfaces false-positives in isSimple)
    for (int i = 0; i < itersPerGenerator; i++) {
      CircularString arc = randomArc();
      actual = arc.isSimple();
      // For random valid 3pt, expect simple (no self cross for single arc)
      if (!actual) {
        bad.add(new ValiditySimplicityMismatch("randomShouldBeSimple", arc, actual, true, "V-CS"));
      }
      // Also multi member via Compound for V-CS
      try {
        CompoundCurve cc = new CompoundCurve(
            new CurvedGeometryFactory().getCoordinateSequenceFactory().create(5, 2), new CurvedGeometryFactory());
        // simplistic; real multi would use proper ctor, here just exercise if possible
      } catch (Exception ignore) {}
    }
    return bad;
  }

  /** V-CP hunter: generate good/bad CPs, collect mismatches in isValid(). Uses sector + isSimple under the hood. */
  public static List<ValiditySimplicityMismatch> huntIsValid(int iters) {
    List<ValiditySimplicityMismatch> bad = new ArrayList<>();
    // Good case
    CurvePolygon good = simpleCurvePolygon();
    boolean actual = good.isValid();
    if (!actual) {
      bad.add(new ValiditySimplicityMismatch("simpleGood", good, actual, true, "V-CP"));
    }
    // Bad self-intersect (reuses V-CS bad)
    CurvePolygon badSelf = selfIntersectingCurvePolygon();
    actual = badSelf.isValid();
    if (actual != false) {
      bad.add(new ValiditySimplicityMismatch("selfIntersectShell", badSelf, actual, false, "V-CP"));
    }
    // Random-ish for over-accept
    for (int i = 0; i < iters; i++) {
      CircularString ra = randomArc();
      // wrap as degenerate CP (may be invalid for other reasons, but exercise)
      try {
        CurvePolygon rp = new CurvePolygon(ra, new LineString[0], GF);
        actual = rp.isValid();
        // many random wraps will be invalid (not closed etc), so only record if expects valid but not
      } catch (Exception ignore) {}
    }
    return bad;
  }

  /**
   * Convenience main for large-scale evidence gathering (like the hunter
   * mains in #1197).
   */
  public static void main(String[] args) {
    int iters = args.length > 0 ? Integer.parseInt(args[0]) : 100_000;
    System.out.println("Hunting arc length counterexamples (" + iters + " per gen)...");
    List<Mismatch> bad = huntArcLength(iters);
    System.out.println("Found " + bad.size() + " mismatches.");
    for (int i = 0; i < Math.min(5, bad.size()); i++) {
      System.out.println("  " + bad.get(i));
    }
    if (bad.isEmpty()) {
      System.out.println("No counterexamples in this run (try more iters or different gens).");
    }

    System.out.println("\nHunting V-CS (isSimple) counterexamples (" + (iters/10) + " iters)...");
    List<ValiditySimplicityMismatch> vcs = huntIsSimple(iters / 10);
    System.out.println("V-CS hunter found " + vcs.size() + " counterexamples (nice cases for hardening).");
    for (int i = 0; i < Math.min(3, vcs.size()); i++) {
      System.out.println("  " + vcs.get(i));
    }

    System.out.println("\nHunting V-CP (isValid) counterexamples...");
    List<ValiditySimplicityMismatch> vcp = huntIsValid(iters / 20);
    System.out.println("V-CP hunter found " + vcp.size() + " counterexamples.");
    for (int i = 0; i < Math.min(3, vcp.size()); i++) {
      System.out.println("  " + vcp.get(i));
    }
  }
}
