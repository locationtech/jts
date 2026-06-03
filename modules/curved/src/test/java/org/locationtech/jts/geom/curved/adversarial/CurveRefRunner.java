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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.curved.CircularString;
import org.locationtech.jts.geom.curved.CurvedGeometryFactory;

/**
 * A reference runner for curve-awareness properties, inspired by the
 * RocqRefRunner / loadProofCases pattern from locationtech/jts#1197 (orientation
 * soundness over arbitrary doubles).
 * <p>
 * Provides self-contained exact (or high-precision) oracles for circular arc
 * properties (length, etc.) and a loader for "proof vector" / reference case
 * artifacts (text files with certified expected values). Any claimed sign/value
 * in the artifact is validated against the in-Java oracle on load.
 * <p>
 * This enables adversarial/regression tests for the curve module (see
 * CurveCounterexampleHunter and the red TAGs in CurveAwarenessSpecTest) that
 * consume certified exports from the NetTopologySuite.Proofs Rocq
 * development (arc/curve theories, see Proofs#64 for the native length/sweep/
 * in-arc primitives) the same way the orientation work does.
 */
public final class CurveRefRunner {

  private CurveRefRunner() {}

  /**
   * Reference case for a 3-point circular arc (start, mid, end) and its
   * exact arc length.
   */
  public static final class ArcLengthCase {
    public final double sx, sy, mx, my, ex, ey;
    public final double expectedLength;

    public ArcLengthCase(double sx, double sy, double mx, double my,
                         double ex, double ey, double expectedLength) {
      this.sx = sx; this.sy = sy;
      this.mx = mx; this.my = my;
      this.ex = ex; this.ey = ey;
      this.expectedLength = expectedLength;
    }

    @Override
    public String toString() {
      return String.format("Arc((%.6g,%.6g)-(%.6g,%.6g)-(%.6g,%.6g)) len=%.12g",
          sx, sy, mx, my, ex, ey, expectedLength);
    }
  }

  /** The outcome of running JTS CircularString.getLength() against reference cases (modeled on RocqRefRunner.Result). */
  public static final class Result {
    public long checked = 0;
    public long mismatches = 0;
    /** A capped list of human-readable mismatch descriptions. */
    public final List<String> failures = new ArrayList<String>();
    private static final int MAX_FAILURES_RECORDED = 20;

    void record(ArcLengthCase c, double actual) {
      mismatches++;
      if (failures.size() < MAX_FAILURES_RECORDED) {
        failures.add(c + " but CircularString.getLength() returned " + actual);
      }
    }

    public boolean isSound() {
      return mismatches == 0;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(checked).append(" cases checked, ").append(mismatches).append(" mismatch(es)");
      for (String f : failures) {
        sb.append("\n  ").append(f);
      }
      if (mismatches > failures.size()) {
        sb.append("\n  ... (").append(mismatches - failures.size()).append(" more)");
      }
      return sb.toString();
    }
  }

  /**
   * Exact arc length for the circular arc defined by three control points.
   * Uses the standard r*theta formula after computing the circumcenter and
   * radius. For the generated cases in the vectors this is accurate; for
   * extreme magnitudes one would promote to BigDecimal (as in RocqRefRunner).
   */
  public static double exactCircularArcLength(double sx, double sy,
                                              double mx, double my,
                                              double ex, double ey) {
    // Compute circumcenter (cx,cy) and r using the determinant formula
    double d = 2 * (sx * (my - ey) + mx * (ey - sy) + ex * (sy - my));
    if (Math.abs(d) < 1e-12) {
      // degenerate / collinear -> chord length
      return Math.hypot(ex - sx, ey - sy);
    }
    double cx = ((sx*sx + sy*sy) * (my - ey)
               + (mx*mx + my*my) * (ey - sy)
               + (ex*ex + ey*ey) * (sy - my)) / d;
    double cy = ((sx*sx + sy*sy) * (ex - mx)
               + (mx*mx + my*my) * (sx - ex)
               + (ex*ex + ey*ey) * (mx - sx)) / d;
    double r = Math.hypot(sx - cx, sy - cy);
    if (r < 1e-12) {
      return Math.hypot(ex - sx, ey - sy);
    }
    // Central angle using atan2 for robustness (sweep through the mid point)
    double a0 = Math.atan2(sy - cy, sx - cx);
    double a1 = Math.atan2(my - cy, mx - cx);
    double a2 = Math.atan2(ey - cy, ex - cx);
    // Compute the signed sweep a0 -> a2 that passes near a1
    double sweep = a2 - a0;
    // normalize to [-pi, pi] then adjust direction if mid indicates the long way
    sweep = ((sweep + Math.PI) % (2 * Math.PI)) - Math.PI;
    // If the mid point suggests we should go the other way, flip
    double aMidRel = a1 - a0;
    aMidRel = ((aMidRel + Math.PI) % (2 * Math.PI)) - Math.PI;
    if (Math.signum(sweep) * Math.signum(aMidRel) < 0 && Math.abs(sweep) < Math.PI) {
      sweep = (sweep > 0 ? sweep - 2*Math.PI : sweep + 2*Math.PI);
    }
    double theta = Math.abs(sweep);
    return r * theta;
  }

  /**
   * Load arc length reference cases from a stream (the "artifact").
   * Format per line (whitespace sep, # comments ignored):
   *   sx sy mx my ex ey expectedLength
   * The expected is cross-checked against exactCircularArcLength; mismatch
   * throws (validates the artifact, like loadProofCases in #1197).
   */
  public static List<ArcLengthCase> loadArcLengthCases(InputStream in) throws IOException {
    List<ArcLengthCase> cases = new ArrayList<>();
    BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    String line;
    int lineNo = 0;
    while ((line = r.readLine()) != null) {
      lineNo++;
      String s = line.trim();
      if (s.isEmpty() || s.startsWith("#")) continue;
      String[] tok = s.split("\\s+");
      if (tok.length < 7) {
        throw new IOException("line " + lineNo + ": expected >=7 tokens, got " + tok.length);
      }
      double sx = Double.parseDouble(tok[0]);
      double sy = Double.parseDouble(tok[1]);
      double mx = Double.parseDouble(tok[2]);
      double my = Double.parseDouble(tok[3]);
      double ex = Double.parseDouble(tok[4]);
      double ey = Double.parseDouble(tok[5]);
      double claimed = Double.parseDouble(tok[6]);
      double derived = exactCircularArcLength(sx, sy, mx, my, ex, ey);
      // Tolerate small fp noise for the generated demo vectors (real Rocq exports
      // will be validated more strictly, as in #1197's loadProofCases).
      if (Math.abs(claimed - derived) > 1e-6 * Math.max(1.0, Math.abs(derived))) {
        throw new IllegalStateException("line " + lineNo
            + ": claimed " + claimed + " disagrees with exact " + derived);
      }
      cases.add(new ArcLengthCase(sx, sy, mx, my, ex, ey, derived));
    }
    return cases;
  }

  /** Convenience: load from classpath resource. */
  public static List<ArcLengthCase> loadArcLengthCases(String resourcePath) throws IOException {
    try (InputStream is = CurveRefRunner.class.getResourceAsStream(resourcePath)) {
      if (is == null) throw new IOException("resource not found: " + resourcePath);
      return loadArcLengthCases(is);
    }
  }

  private static CircularString makeCS(ArcLengthCase c) {
    CoordinateSequence cs = new CurvedGeometryFactory().getCoordinateSequenceFactory().create(3, 2);
    cs.setOrdinate(0, 0, c.sx); cs.setOrdinate(0, 1, c.sy);
    cs.setOrdinate(1, 0, c.mx); cs.setOrdinate(1, 1, c.my);
    cs.setOrdinate(2, 0, c.ex); cs.setOrdinate(2, 1, c.ey);
    return new CircularString(cs, new CurvedGeometryFactory());
  }

  /**
   * Runs CircularString.getLength() against the reference for every case (modeled on RocqRefRunner.run).
   * Returns a Result that can be asserted with isSound() for hardening tests.
   */
  public static Result run(Iterable<ArcLengthCase> cases) {
    Result r = new Result();
    for (ArcLengthCase c : cases) {
      r.checked++;
      CircularString cs = makeCS(c);
      double actual = cs.getLength();
      if (Math.abs(actual - c.expectedLength) > 1e-9 * Math.max(1.0, Math.abs(c.expectedLength))) {
        r.record(c, actual);
      }
    }
    return r;
  }
}
