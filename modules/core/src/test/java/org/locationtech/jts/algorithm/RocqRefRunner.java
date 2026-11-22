/*
 * Copyright (c) 2026 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.locationtech.jts.geom.Coordinate;

/**
 * A bespoke Java reference runner for the orientation-soundness requirement
 * tracked as JTS issue #1106.
 * <p>
 * Background: the orientation predicate ({@link Orientation#index}, backed by
 * {@link CGAlgorithmsDD#orientationIndex}) is claimed to be <i>sound</i> &mdash;
 * to return the exact geometric turn direction &mdash; for integer coordinates
 * whose magnitude does not exceed {@link #SAFE_BOUND} (2<sup>25</sup>).
 * <p>
 * Within that domain the claim has a short, machine-checkable argument:
 * <ul>
 *   <li>coordinate differences are bounded by 2<sup>26</sup>,</li>
 *   <li>the products of two differences by 2<sup>52</sup>,</li>
 *   <li>and the 2x2 orientation determinant by 2<sup>53</sup> &lt; 2<sup>63</sup>.</li>
 * </ul>
 * The determinant therefore fits exactly in signed 64-bit integer arithmetic,
 * so its sign &mdash; computed here by {@link #refSign} &mdash; is the exact
 * reference value. This is the same value a Rocq (Coq) orientation-soundness
 * proof certifies for the integer domain; this runner reconstructs that
 * certified reference natively in Java so the requirement can be exercised as
 * an ordinary JUnit test.
 * <p>
 * The runner also accepts externally supplied reference vectors via
 * {@link #loadProofCases(InputStream)} (for example, vectors exported from the
 * Rocq development). Any {@code expected} sign carried by such a vector is
 * cross-checked against {@link #refSign}, so an inconsistent export is rejected
 * rather than silently trusted.
 *
 * @author JTS
 */
public class RocqRefRunner {

  /**
   * The largest coordinate magnitude for which the reference orientation sign
   * is guaranteed exact in 64-bit integer arithmetic: 2<sup>25</sup>.
   */
  public static final long SAFE_BOUND = 1L << 25;

  private RocqRefRunner() {
    // static utility
  }

  /**
   * A single reference case: three integer points and the certified-exact
   * orientation sign of the triangle {@code (p0, p1, q)}.
   */
  public static final class RefCase {
    public final long p0x, p0y, p1x, p1y, qx, qy;
    /** The certified orientation sign: -1 (CW), 0 (collinear) or 1 (CCW). */
    public final int expected;

    public RefCase(long p0x, long p0y, long p1x, long p1y, long qx, long qy) {
      this(p0x, p0y, p1x, p1y, qx, qy, refSign(p0x, p0y, p1x, p1y, qx, qy));
    }

    public RefCase(long p0x, long p0y, long p1x, long p1y, long qx, long qy, int expected) {
      this.p0x = p0x; this.p0y = p0y;
      this.p1x = p1x; this.p1y = p1y;
      this.qx = qx;   this.qy = qy;
      this.expected = expected;
    }

    public String toString() {
      return "(" + p0x + " " + p0y + ", " + p1x + " " + p1y + ", " + qx + " " + qy
          + ") expected=" + expected;
    }
  }

  /** The outcome of running JTS against a set of reference cases. */
  public static final class Result {
    public long checked = 0;
    public long mismatches = 0;
    /** A capped list of human-readable mismatch descriptions. */
    public final List<String> failures = new ArrayList<String>();
    private static final int MAX_FAILURES_RECORDED = 20;

    void record(RefCase c, int actual) {
      mismatches++;
      if (failures.size() < MAX_FAILURES_RECORDED) {
        failures.add(c + " but Orientation.index returned " + actual);
      }
    }

    void recordDouble(double[] c, int expected, int actual) {
      mismatches++;
      if (failures.size() < MAX_FAILURES_RECORDED) {
        failures.add("(" + hx(c[0]) + " " + hx(c[1]) + ", " + hx(c[2]) + " " + hx(c[3])
            + ", " + hx(c[4]) + " " + hx(c[5]) + ") expected=" + expected
            + " but Orientation.index returned " + actual);
      }
    }

    // exact, round-trippable rendering of a double for failure reproduction
    private static String hx(double d) {
      return d + "[" + Double.toHexString(d) + "]";
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
   * Computes the exact orientation sign of the triangle {@code (p0, p1, q)}
   * for integer coordinates within {@link #SAFE_BOUND}.
   * <p>
   * The result is exact: the determinant fits in a signed 64-bit integer for
   * all inputs in the domain (see class documentation).
   *
   * @return -1 for clockwise, 1 for counter-clockwise, 0 for collinear
   * @throws IllegalArgumentException if any coordinate is outside the domain
   */
  public static int refSign(long p0x, long p0y, long p1x, long p1y, long qx, long qy) {
    requireInDomain(p0x); requireInDomain(p0y);
    requireInDomain(p1x); requireInDomain(p1y);
    requireInDomain(qx);  requireInDomain(qy);

    long dx1 = p1x - p0x;
    long dy1 = p1y - p0y;
    long dx2 = qx - p0x;
    long dy2 = qy - p0y;
    long det = dx1 * dy2 - dy1 * dx2;
    return Long.signum(det);
  }

  private static void requireInDomain(long c) {
    if (c < -SAFE_BOUND || c > SAFE_BOUND) {
      throw new IllegalArgumentException(
          "coordinate " + c + " outside certified domain [-2^25, 2^25]");
    }
  }

  /**
   * The exact orientation sign of three points given as arbitrary
   * <i>double</i> coordinates &mdash; the soundness reference over R&sup2;.
   * <p>
   * Every finite {@code double} is a dyadic rational, so {@code new
   * BigDecimal(double)} captures its <i>exact</i> value and the determinant
   * below is computed without any rounding. This is the value the orientation
   * predicate must return to be sound for the actual coordinates it is handed;
   * comparing {@link Orientation#index} against it is a direct, unrestricted
   * test of the #1106 claim over the full coordinate plane (subject only to
   * sampling, not to a domain restriction).
   *
   * @throws IllegalArgumentException if any coordinate is not finite
   */
  public static int refSignExact(double p0x, double p0y,
      double p1x, double p1y, double qx, double qy) {
    requireFinite(p0x); requireFinite(p0y);
    requireFinite(p1x); requireFinite(p1y);
    requireFinite(qx);  requireFinite(qy);

    java.math.BigDecimal P0x = new java.math.BigDecimal(p0x);
    java.math.BigDecimal P0y = new java.math.BigDecimal(p0y);
    java.math.BigDecimal dx1 = new java.math.BigDecimal(p1x).subtract(P0x);
    java.math.BigDecimal dy1 = new java.math.BigDecimal(p1y).subtract(P0y);
    java.math.BigDecimal dx2 = new java.math.BigDecimal(qx).subtract(P0x);
    java.math.BigDecimal dy2 = new java.math.BigDecimal(qy).subtract(P0y);
    java.math.BigDecimal det = dx1.multiply(dy2).subtract(dy1.multiply(dx2));
    return det.signum();
  }

  private static void requireFinite(double c) {
    if (Double.isNaN(c) || Double.isInfinite(c)) {
      throw new IllegalArgumentException("coordinate is not finite: " + c);
    }
  }

  /**
   * An independent, unconditionally-exact orientation sign using
   * {@link BigInteger}. Used to validate {@link #refSign} itself; never relies
   * on the 64-bit domain bound.
   */
  static int refSignBig(long p0x, long p0y, long p1x, long p1y, long qx, long qy) {
    BigInteger dx1 = BigInteger.valueOf(p1x - p0x);
    BigInteger dy1 = BigInteger.valueOf(p1y - p0y);
    BigInteger dx2 = BigInteger.valueOf(qx - p0x);
    BigInteger dy2 = BigInteger.valueOf(qy - p0y);
    return dx1.multiply(dy2).subtract(dy1.multiply(dx2)).signum();
  }

  /**
   * Runs {@link Orientation#index} against the certified reference for every
   * case, accumulating any mismatches.
   */
  public static Result run(Iterable<RefCase> cases) {
    Result r = new Result();
    for (RefCase c : cases) {
      r.checked++;
      int actual = Orientation.index(
          new Coordinate(c.p0x, c.p0y),
          new Coordinate(c.p1x, c.p1y),
          new Coordinate(c.qx, c.qy));
      if (actual != c.expected) {
        r.record(c, actual);
      }
    }
    return r;
  }

  /**
   * Runs {@link Orientation#index} against the exact R&sup2; reference
   * ({@link #refSignExact}) for every case. Each case is a {@code double[6]}:
   * {@code {p0x, p0y, p1x, p1y, qx, qy}}.
   * <p>
   * Unlike {@link #run(Iterable)} this imposes no domain restriction, so a
   * mismatch here is a genuine soundness gap in the predicate for the given
   * double coordinates.
   */
  public static Result runDoubles(Iterable<double[]> cases) {
    Result r = new Result();
    for (double[] c : cases) {
      r.checked++;
      int actual = Orientation.index(
          new Coordinate(c[0], c[1]),
          new Coordinate(c[2], c[3]),
          new Coordinate(c[4], c[5]));
      int expected = refSignExact(c[0], c[1], c[2], c[3], c[4], c[5]);
      if (actual != expected) {
        r.recordDouble(c, expected, actual);
      }
    }
    return r;
  }

  // ------------------------------------------------------------------
  // Case generators
  // ------------------------------------------------------------------

  /**
   * Exhaustively enumerates every triple of integer points whose coordinates
   * lie in {@code [-radius, radius]}. This covers all degenerate
   * configurations (collinear, coincident, zero-length segments) for small
   * magnitudes.
   */
  public static List<RefCase> exhaustiveGrid(int radius) {
    List<RefCase> cases = new ArrayList<RefCase>();
    for (long ax = -radius; ax <= radius; ax++)
      for (long ay = -radius; ay <= radius; ay++)
        for (long bx = -radius; bx <= radius; bx++)
          for (long by = -radius; by <= radius; by++)
            for (long cx = -radius; cx <= radius; cx++)
              for (long cy = -radius; cy <= radius; cy++)
                cases.add(new RefCase(ax, ay, bx, by, cx, cy));
    return cases;
  }

  /**
   * Generates {@code n} uniformly random integer triples with coordinates in
   * {@code [-bound, bound]}.
   */
  public static List<RefCase> random(int n, long bound, long seed) {
    Random rnd = new Random(seed);
    List<RefCase> cases = new ArrayList<RefCase>(n);
    for (int i = 0; i < n; i++) {
      cases.add(new RefCase(
          randCoord(rnd, bound), randCoord(rnd, bound),
          randCoord(rnd, bound), randCoord(rnd, bound),
          randCoord(rnd, bound), randCoord(rnd, bound)));
    }
    return cases;
  }

  /**
   * Generates {@code n} adversarial near-collinear integer triples: a random
   * segment {@code p0-p1}, a point placed exactly on the line through it (by an
   * integer multiple of the direction vector), then nudged by a tiny integer
   * offset. These are the hard cases for floating-point orientation: the true
   * answer is exactly collinear or barely off it, yet the certified reference
   * remains exact because every coordinate is an integer within the domain.
   */
  public static List<RefCase> nearCollinear(int n, long bound, long seed) {
    Random rnd = new Random(seed);
    List<RefCase> cases = new ArrayList<RefCase>(n);
    // keep room for the multiple + nudge to stay within the domain
    long segBound = Math.max(1, bound / 256);
    for (int i = 0; i < n; i++) {
      long p0x = randCoord(rnd, segBound);
      long p0y = randCoord(rnd, segBound);
      long dx = randCoord(rnd, segBound);
      long dy = randCoord(rnd, segBound);
      long p1x = clampToDomain(p0x + dx);
      long p1y = clampToDomain(p0y + dy);
      // a point exactly on the line p0->p1
      long k = rnd.nextInt(9) - 4; // -4..4
      long qx = clampToDomain(p0x + k * dx);
      long qy = clampToDomain(p0y + k * dy);
      // nudge just off the line by a small integer offset
      qx = clampToDomain(qx + (rnd.nextInt(5) - 2));
      qy = clampToDomain(qy + (rnd.nextInt(5) - 2));
      cases.add(new RefCase(p0x, p0y, p1x, p1y, qx, qy));
    }
    return cases;
  }

  /**
   * Generates cases that exercise the extreme corners of the domain: every
   * coordinate is either {@code -SAFE_BOUND} or {@code +SAFE_BOUND}, optionally
   * offset inward by a small amount, so the determinant approaches its maximum
   * magnitude.
   */
  public static List<RefCase> domainBoundary(int n, long seed) {
    Random rnd = new Random(seed);
    List<RefCase> cases = new ArrayList<RefCase>(n);
    for (int i = 0; i < n; i++) {
      cases.add(new RefCase(
          extremeCoord(rnd), extremeCoord(rnd),
          extremeCoord(rnd), extremeCoord(rnd),
          extremeCoord(rnd), extremeCoord(rnd)));
    }
    return cases;
  }

  private static long extremeCoord(Random rnd) {
    long base = rnd.nextBoolean() ? SAFE_BOUND : -SAFE_BOUND;
    long inward = rnd.nextInt(4); // 0..3
    return clampToDomain(base >= 0 ? base - inward : base + inward);
  }

  private static long randCoord(Random rnd, long bound) {
    // uniform in [-bound, bound]
    long span = 2 * bound + 1;
    long v = Math.floorMod(rnd.nextLong(), span) - bound;
    return v;
  }

  private static long clampToDomain(long v) {
    if (v > SAFE_BOUND) return SAFE_BOUND;
    if (v < -SAFE_BOUND) return -SAFE_BOUND;
    return v;
  }

  // ------------------------------------------------------------------
  // R^2 (arbitrary double) generators
  // ------------------------------------------------------------------

  /**
   * Generates {@code n} triples of arbitrary double coordinates uniformly in
   * {@code [-magnitude, magnitude]}. Exercises the predicate across the real
   * plane with no domain restriction.
   */
  public static List<double[]> randomDoubles(int n, double magnitude, long seed) {
    Random rnd = new Random(seed);
    List<double[]> cases = new ArrayList<double[]>(n);
    for (int i = 0; i < n; i++) {
      cases.add(new double[] {
          rndD(rnd, magnitude), rndD(rnd, magnitude),
          rndD(rnd, magnitude), rndD(rnd, magnitude),
          rndD(rnd, magnitude), rndD(rnd, magnitude) });
    }
    return cases;
  }

  /**
   * Generates {@code n} adversarial near-collinear double triples: a random
   * segment {@code p0-p1}, a point chosen on the line through it, then
   * displaced perpendicularly by only a few ULPs. These are the
   * cancellation-prone configurations where a naive double predicate fails;
   * the exact reference still decides the true side, so any disagreement from
   * {@link Orientation#index} is a real robustness defect.
   */
  public static List<double[]> nearCollinearDoubles(int n, double magnitude, long seed) {
    Random rnd = new Random(seed);
    List<double[]> cases = new ArrayList<double[]>(n);
    for (int i = 0; i < n; i++) {
      double p0x = rndD(rnd, magnitude);
      double p0y = rndD(rnd, magnitude);
      double dx = rndD(rnd, magnitude);
      double dy = rndD(rnd, magnitude);
      double p1x = p0x + dx;
      double p1y = p0y + dy;
      // a base point on the (mathematical) line p0->p1
      double t = rnd.nextDouble() * 2.0 - 1.0;
      double bx = p0x + t * dx;
      double by = p0y + t * dy;
      // unit normal to the segment
      double len = Math.hypot(dx, dy);
      double nx = len == 0 ? 0 : -dy / len;
      double ny = len == 0 ? 0 : dx / len;
      // displace by a few ULPs perpendicular to the line (sign chosen at random)
      double scale = Math.max(1.0, Math.max(Math.abs(bx), Math.abs(by)));
      double eps = Math.ulp(scale) * (rnd.nextInt(7) - 3); // -3..3 ULP-ish
      double qx = bx + eps * nx;
      double qy = by + eps * ny;
      cases.add(new double[] { p0x, p0y, p1x, p1y, qx, qy });
    }
    return cases;
  }

  private static double rndD(Random rnd, double magnitude) {
    return (rnd.nextDouble() * 2.0 - 1.0) * magnitude;
  }

  // ------------------------------------------------------------------
  // Proof-vector loading
  // ------------------------------------------------------------------

  /**
   * Loads reference cases from a stream of exported proof vectors. The format
   * is line-oriented and tolerant:
   * <ul>
   *   <li>blank lines and lines beginning with {@code #} are ignored;</li>
   *   <li>a data line is whitespace-separated and holds either 6 integers
   *       {@code p0x p0y p1x p1y qx qy} (the expected sign is derived) or 7
   *       integers where the 7th is the expected sign.</li>
   * </ul>
   * When a 7th value is present it is cross-checked against {@link #refSign};
   * a disagreement raises an {@link IllegalStateException}, so a corrupt or
   * stale export cannot quietly weaken the test.
   *
   * @return the parsed cases (possibly empty)
   */
  public static List<RefCase> loadProofCases(InputStream in) throws IOException {
    List<RefCase> cases = new ArrayList<RefCase>();
    BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    String line;
    int lineNo = 0;
    while ((line = r.readLine()) != null) {
      lineNo++;
      String s = line.trim();
      if (s.isEmpty() || s.charAt(0) == '#') continue;
      String[] tok = s.split("\\s+");
      if (tok.length != 6 && tok.length != 7) {
        throw new IOException("line " + lineNo + ": expected 6 or 7 integers, got " + tok.length);
      }
      long p0x = Long.parseLong(tok[0]);
      long p0y = Long.parseLong(tok[1]);
      long p1x = Long.parseLong(tok[2]);
      long p1y = Long.parseLong(tok[3]);
      long qx = Long.parseLong(tok[4]);
      long qy = Long.parseLong(tok[5]);
      int derived = refSign(p0x, p0y, p1x, p1y, qx, qy);
      if (tok.length == 7) {
        int claimed = Integer.parseInt(tok[6]);
        if (claimed != derived) {
          throw new IllegalStateException("line " + lineNo
              + ": exported sign " + claimed + " disagrees with certified reference " + derived);
        }
      }
      cases.add(new RefCase(p0x, p0y, p1x, p1y, qx, qy, derived));
    }
    return cases;
  }
}
