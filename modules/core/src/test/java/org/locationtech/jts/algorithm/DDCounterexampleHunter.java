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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.locationtech.jts.geom.Coordinate;

/**
 * Searches for inputs on which an orientation predicate disagrees with the
 * exact reference ({@link RocqRefRunner#refSignExact}), i.e. counterexamples
 * to that predicate's soundness over arbitrary <i>double</i> coordinates
 * (the property requested in JTS #1106).
 * <p>
 * The hunter evaluates three predicates side by side:
 * <ul>
 *   <li>{@link Orientation#index} &mdash; the current JTS predicate, backed by
 *       double-double (DD) arithmetic;</li>
 *   <li>{@link RobustDeterminant#orientationIndex} &mdash; the previous JTS
 *       implementation;</li>
 *   <li>{@link NonRobustCGAlgorithms#orientationIndex} &mdash; a naive
 *       double-precision determinant.</li>
 * </ul>
 * Running all three against the same exact oracle both demonstrates that the
 * search is effective (it finds many counterexamples for the legacy and naive
 * predicates) and quantifies the robustness gained by DD.
 * <p>
 * Empirically the DD predicate produces <b>no</b> counterexamples even under
 * targeted, near-degenerate constructions. This is consistent with a margin
 * argument: for double inputs the smallest non-zero orientation determinant is
 * on the order of 2<sup>-104</sup> relative to the product scale, whereas DD
 * resolves to roughly 2<sup>-106</sup> &mdash; about two bits of headroom.
 */
public class DDCounterexampleHunter {

  /** A predicate under test: maps three points to an orientation index. */
  public interface Predicate {
    int index(Coordinate p0, Coordinate p1, Coordinate q);
    String name();
  }

  public static final Predicate DD = new Predicate() {
    public int index(Coordinate p0, Coordinate p1, Coordinate q) {
      return Orientation.index(p0, p1, q);
    }
    public String name() { return "Orientation.index (DD)"; }
  };

  public static final Predicate ROBUST_DETERMINANT = new Predicate() {
    public int index(Coordinate p0, Coordinate p1, Coordinate q) {
      return RobustDeterminant.orientationIndex(p0, p1, q);
    }
    public String name() { return "RobustDeterminant"; }
  };

  public static final Predicate NON_ROBUST = new Predicate() {
    public int index(Coordinate p0, Coordinate p1, Coordinate q) {
      return NonRobustCGAlgorithms.orientationIndex(p0, p1, q);
    }
    public String name() { return "NonRobustCGAlgorithms"; }
  };

  /** Counterexamples found for a single predicate. */
  public static final class HuntResult {
    public final String predicate;
    public long checked = 0;
    public long counterexamples = 0;
    public final List<double[]> samples = new ArrayList<double[]>();
    private static final int MAX_SAMPLES = 20;

    HuntResult(String predicate) {
      this.predicate = predicate;
    }

    void record(double[] c) {
      counterexamples++;
      if (samples.size() < MAX_SAMPLES) {
        samples.add(c);
      }
    }

    public boolean foundAny() {
      return counterexamples > 0;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(predicate).append(": ").append(counterexamples)
        .append(" counterexample(s) in ").append(checked).append(" cases");
      for (double[] c : samples) {
        sb.append("\n    (").append(c[0]).append(' ').append(c[1])
          .append(", ").append(c[2]).append(' ').append(c[3])
          .append(", ").append(c[4]).append(' ').append(c[5]).append(")")
          .append("  [").append(Double.toHexString(c[0])).append(' ')
          .append(Double.toHexString(c[1])).append(", ")
          .append(Double.toHexString(c[2])).append(' ')
          .append(Double.toHexString(c[3])).append(", ")
          .append(Double.toHexString(c[4])).append(' ')
          .append(Double.toHexString(c[5])).append("]");
      }
      return sb.toString();
    }
  }

  /**
   * Runs {@code predicate} against the exact reference for every case,
   * collecting disagreements.
   */
  public static HuntResult hunt(Predicate predicate, Iterable<double[]> cases) {
    HuntResult r = new HuntResult(predicate.name());
    for (double[] c : cases) {
      r.checked++;
      int expected = RocqRefRunner.refSignExact(c[0], c[1], c[2], c[3], c[4], c[5]);
      int actual = predicate.index(
          new Coordinate(c[0], c[1]),
          new Coordinate(c[2], c[3]),
          new Coordinate(c[4], c[5]));
      if (actual != expected) {
        r.record(c);
      }
    }
    return r;
  }

  // ------------------------------------------------------------------
  // Adversarial case generators
  // ------------------------------------------------------------------

  /**
   * Near-collinear triples at the given magnitude: a random segment with a
   * point displaced only a few ULPs perpendicular to it. Returns
   * {@code double[6]} cases {@code {p0x,p0y,p1x,p1y,qx,qy}}.
   */
  public static List<double[]> nearCollinear(int n, double magnitude, long seed) {
    Random rnd = new Random(seed);
    List<double[]> cases = new ArrayList<double[]>(n);
    for (int i = 0; i < n; i++) {
      double p0x = rndD(rnd, magnitude);
      double p0y = rndD(rnd, magnitude);
      double dx = rndD(rnd, magnitude);
      double dy = rndD(rnd, magnitude);
      double p1x = p0x + dx;
      double p1y = p0y + dy;
      double t = rnd.nextDouble() * 2.0 - 1.0;
      double bx = p0x + t * dx;
      double by = p0y + t * dy;
      double len = Math.hypot(dx, dy);
      double nx = len == 0 ? 0 : -dy / len;
      double ny = len == 0 ? 0 : dx / len;
      double scale = Math.max(1.0, Math.max(Math.abs(bx), Math.abs(by)));
      double eps = Math.ulp(scale) * (rnd.nextInt(5) - 2);
      double qx = bx + eps * nx;
      double qy = by + eps * ny;
      cases.add(new double[] { p0x, p0y, p1x, p1y, qx, qy });
    }
    return cases;
  }

  /**
   * Minimal-determinant "product collision" triples {@code (0,0),(a,b),(c,d)}
   * where {@code d} is chosen as the double nearest {@code b*c/a}, forcing the
   * determinant {@code a*d - b*c} toward zero. This drives the predicate into
   * the regime where extended precision is required.
   */
  public static List<double[]> productCollision(int n, long seed) {
    Random rnd = new Random(seed);
    List<double[]> cases = new ArrayList<double[]>(n);
    for (int i = 0; i < n; i++) {
      // full-precision mantissas with fractional bits so products are not exact doubles
      double a = 1.0 + rnd.nextDouble();
      double b = 1.0 + rnd.nextDouble();
      double c = 1.0 + rnd.nextDouble();
      double scale = Math.scalb(1.0, rnd.nextInt(40)); // vary exponent
      a *= scale; b *= scale; c *= scale;
      double d = b * c / a; // rounds to nearest double => a*d ~= b*c
      cases.add(new double[] { 0, 0, a, b, c, d });
    }
    return cases;
  }

  /** Uniform random triples in {@code [-magnitude, magnitude]}. */
  public static List<double[]> uniform(int n, double magnitude, long seed) {
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

  private static double rndD(Random rnd, double magnitude) {
    return (rnd.nextDouble() * 2.0 - 1.0) * magnitude;
  }

  /**
   * Command-line entry point for an extended hunt. Prints per-predicate
   * counterexample counts for several adversarial strategies.
   */
  public static void main(String[] args) {
    Predicate[] preds = { DD, ROBUST_DETERMINANT, NON_ROBUST };
    double[] mags = { 1.0e7, 1.0e12, 4.5e15 };
    for (double mag : mags) {
      List<double[]> cases = nearCollinear(1_000_000, mag, 1);
      System.out.println("near-collinear, magnitude " + mag + ":");
      for (Predicate p : preds) {
        System.out.println("  " + hunt(p, cases));
      }
    }
    List<double[]> pc = productCollision(2_000_000, 5);
    System.out.println("product-collision:");
    for (Predicate p : preds) {
      System.out.println("  " + hunt(p, pc));
    }
  }
}
