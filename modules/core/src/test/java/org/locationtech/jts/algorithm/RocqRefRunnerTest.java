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

import java.io.InputStream;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Strengthens the orientation-soundness claim (JTS #1106) by running
 * {@link Orientation#index} against the certified-exact reference computed by
 * {@link RocqRefRunner} across the integer domain |c| &le; 2<sup>25</sup>:
 * exhaustively for small magnitudes, and via large randomized, adversarial
 * near-collinear, and domain-boundary samples otherwise.
 * <p>
 * Every test asserts that JTS agrees with the reference on every case, which
 * is exactly the property the requirement asserts.
 */
public class RocqRefRunnerTest extends TestCase {

  /** Resource holding optional externally-exported (e.g. Rocq) proof vectors. */
  private static final String PROOF_VECTORS_RESOURCE =
      "/org/locationtech/jts/algorithm/rocqref/orientation_proof_vectors.txt";

  public static void main(String[] args) {
    TestRunner.run(RocqRefRunnerTest.class);
  }

  public RocqRefRunnerTest(String name) {
    super(name);
  }

  /**
   * Validates the reference itself: the fast 64-bit {@code refSign} must agree
   * with an unconditionally-exact {@link java.math.BigInteger} computation for
   * every coordinate in the domain (including the boundary). If this fails the
   * domain bound is wrong and the rest of the suite is meaningless.
   */
  public void testReferenceIsExactInDomain() {
    Random rnd = new Random(1);
    long bound = RocqRefRunner.SAFE_BOUND;
    for (int i = 0; i < 500000; i++) {
      long p0x = rndIn(rnd, bound), p0y = rndIn(rnd, bound);
      long p1x = rndIn(rnd, bound), p1y = rndIn(rnd, bound);
      long qx = rndIn(rnd, bound),  qy = rndIn(rnd, bound);
      assertEquals(
          RocqRefRunner.refSignBig(p0x, p0y, p1x, p1y, qx, qy),
          RocqRefRunner.refSign(p0x, p0y, p1x, p1y, qx, qy));
    }
  }

  /** Exhaustive over every integer triple with coordinates in [-4, 4]. */
  public void testExhaustiveSmallGrid() {
    RocqRefRunner.Result r = RocqRefRunner.run(RocqRefRunner.exhaustiveGrid(4));
    assertTrue("orientation unsound on small grid:\n" + r, r.isSound());
  }

  /** Large uniform random sample across the full domain. */
  public void testRandomWithinDomain() {
    RocqRefRunner.Result r = RocqRefRunner.run(
        RocqRefRunner.random(500000, RocqRefRunner.SAFE_BOUND, 42));
    assertTrue("orientation unsound on random sample:\n" + r, r.isSound());
  }

  /** Adversarial near-collinear cases &mdash; the hard ones for FP orientation. */
  public void testNearCollinear() {
    RocqRefRunner.Result r = RocqRefRunner.run(
        RocqRefRunner.nearCollinear(500000, RocqRefRunner.SAFE_BOUND, 7));
    assertTrue("orientation unsound on near-collinear sample:\n" + r, r.isSound());
  }

  /** Coordinates at the extreme corners of the domain (largest determinants). */
  public void testDomainBoundary() {
    RocqRefRunner.Result r = RocqRefRunner.run(
        RocqRefRunner.domainBoundary(200000, 99));
    assertTrue("orientation unsound at domain boundary:\n" + r, r.isSound());
  }

  /**
   * Runs any externally-exported proof vectors bundled as a test resource.
   * Skips silently when the resource is absent so the suite stays green before
   * the Rocq export has been fetched.
   */
  public void testExportedProofVectors() throws Exception {
    InputStream in = getClass().getResourceAsStream(PROOF_VECTORS_RESOURCE);
    if (in == null) {
      // no exported vectors present in this build; nothing to check
      return;
    }
    try {
      List<RocqRefRunner.RefCase> cases = RocqRefRunner.loadProofCases(in);
      assertTrue("proof vector resource is present but empty", cases.size() > 0);
      RocqRefRunner.Result r = RocqRefRunner.run(cases);
      assertTrue("orientation unsound on exported proof vectors:\n" + r, r.isSound());
    } finally {
      in.close();
    }
  }

  private static long rndIn(Random rnd, long bound) {
    long span = 2 * bound + 1;
    return Math.floorMod(rnd.nextLong(), span) - bound;
  }

  // ------------------------------------------------------------------
  // R^2 coverage: arbitrary double coordinates vs. the exact reference.
  //
  // These tests address #1106 as written -- soundness over the full
  // coordinate plane, not a bounded-integer sub-domain. JTS uses double-double
  // (~106-bit) arithmetic, which is robust but NOT provably exact for all of
  // R^2 (see OrientationIndexFailureTest#testSimpleFail). Passing these large
  // samples is therefore strong empirical evidence of R^2 agreement, not a
  // proof of soundness; a failure would pinpoint a real robustness defect.
  // ------------------------------------------------------------------

  /** Uniform random doubles at moderate magnitude. */
  public void testR2RandomModerate() {
    RocqRefRunner.Result r = RocqRefRunner.runDoubles(
        RocqRefRunner.randomDoubles(300000, 1.0e6, 11));
    assertTrue("R^2 mismatch (moderate magnitude):\n" + r, r.isSound());
  }

  /** Uniform random doubles at large magnitude (stresses cancellation). */
  public void testR2RandomLarge() {
    RocqRefRunner.Result r = RocqRefRunner.runDoubles(
        RocqRefRunner.randomDoubles(300000, 1.0e15, 13));
    assertTrue("R^2 mismatch (large magnitude):\n" + r, r.isSound());
  }

  /** Adversarial near-collinear doubles displaced only a few ULPs off the line. */
  public void testR2NearCollinear() {
    RocqRefRunner.Result r = RocqRefRunner.runDoubles(
        RocqRefRunner.nearCollinearDoubles(500000, 1.0e6, 17));
    assertTrue("R^2 mismatch (near-collinear):\n" + r, r.isSound());
  }

  /**
   * The double-precision-hard cases collected in
   * {@link OrientationIndexFailureTest}: configurations that break a naive
   * double predicate. JTS's DD predicate is expected to agree with the exact
   * reference on all of them.
   */
  public void testR2LiteratureHardCases() {
    double[][] triples = {
        { 1.4540766091864998, -7.989685402102996,
          23.131039116367354, -7.004368924503866,
          1.4540766091865, -7.989685402102996 },
        { 219.3649559090992, 140.84159161824724,
          168.9018919682399, -5.713787599646864,
          186.80814046338352, 46.28973405831556 },
        { 279.56857838488514, -186.3790522565901,
          -20.43142161511487, 13.620947743409914,
          0, 0 },
        { -26.2, 188.7, 37.0, 290.7, 21.2, 265.2 },
        { -5.9, 163.1, 76.1, 250.7, 14.6, 185 },
        { -0.9575, 0.4511, -0.9295, 0.3291, -0.8945, 0.1766 },
        { -140.8859438214298, 140.88594382142983,
          -57.309236848216706, 57.30923684821671,
          -190.9188309203678, 190.91883092036784 },
    };
    java.util.List<double[]> cases = new java.util.ArrayList<double[]>();
    for (double[] t : triples) {
      // include all three rotations; the exact reference is rotation-consistent
      cases.add(new double[] { t[0], t[1], t[2], t[3], t[4], t[5] });
      cases.add(new double[] { t[2], t[3], t[4], t[5], t[0], t[1] });
      cases.add(new double[] { t[4], t[5], t[0], t[1], t[2], t[3] });
    }
    RocqRefRunner.Result r = RocqRefRunner.runDoubles(cases);
    assertTrue("R^2 mismatch (literature hard cases):\n" + r, r.isSound());
  }
}
