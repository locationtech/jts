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
import org.locationtech.jts.geom.curved.CurvedGeometryFactory;

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
  }
}
