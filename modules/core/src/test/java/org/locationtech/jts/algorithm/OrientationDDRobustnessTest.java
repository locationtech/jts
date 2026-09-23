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

import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Uses {@link DDCounterexampleHunter} to characterize the robustness of the
 * JTS orientation predicate over arbitrary double coordinates (JTS #1106).
 * <p>
 * The suite asserts three things:
 * <ul>
 *   <li>the search is effective &mdash; it readily finds counterexamples for
 *       the naive and legacy predicates that DD replaced;</li>
 *   <li>within a bounded coordinate-magnitude band the current DD predicate
 *       ({@link Orientation#index}) produces no counterexamples under the same
 *       adversarial inputs; and</li>
 *   <li>outside that band DD <i>does</i> fail &mdash; for very large
 *       coordinates the products overflow and for very small ones they
 *       underflow, in both cases yielding a wrong (collinear) result. These
 *       are characterization tests of a real limitation, not endorsements.</li>
 * </ul>
 * Budgets are kept modest so the suite runs quickly; the extended evidence
 * comes from {@link DDCounterexampleHunter#main}, which runs far larger hunts.
 */
public class OrientationDDRobustnessTest extends TestCase {

  public static void main(String[] args) {
    TestRunner.run(OrientationDDRobustnessTest.class);
  }

  public OrientationDDRobustnessTest(String name) {
    super(name);
  }

  /**
   * Sanity / methodology check: the naive and legacy predicates MUST fail on
   * adversarial near-collinear inputs. If they did not, the hunt would be
   * vacuous and the DD result below meaningless.
   */
  public void testHunterFindsLegacyCounterexamples() {
    List<double[]> cases = DDCounterexampleHunter.nearCollinear(200000, 1.0e7, 1);

    DDCounterexampleHunter.HuntResult naive =
        DDCounterexampleHunter.hunt(DDCounterexampleHunter.NON_ROBUST, cases);
    DDCounterexampleHunter.HuntResult legacy =
        DDCounterexampleHunter.hunt(DDCounterexampleHunter.ROBUST_DETERMINANT, cases);

    assertTrue("expected naive double predicate to fail on near-collinear inputs",
        naive.foundAny());
    assertTrue("expected legacy RobustDeterminant to fail on near-collinear inputs",
        legacy.foundAny());
  }

  /**
   * Within the safe magnitude band DD must have no counterexamples on
   * adversarial near-collinear inputs. (Magnitudes here are far below the
   * 2^512 overflow threshold; see {@link #testDDFailsOnOverflow}.)
   */
  public void testDDSoundNearCollinear() {
    double[] mags = { 1.0e7, 1.0e12, 4.5e15 };
    for (double mag : mags) {
      List<double[]> cases = DDCounterexampleHunter.nearCollinear(200000, mag, 1);
      DDCounterexampleHunter.HuntResult dd =
          DDCounterexampleHunter.hunt(DDCounterexampleHunter.DD, cases);
      assertFalse("DD counterexample found at magnitude " + mag + ":\n" + dd, dd.foundAny());
    }
  }

  /** DD must have no counterexamples on minimal-determinant product collisions. */
  public void testDDSoundProductCollision() {
    List<double[]> cases = DDCounterexampleHunter.productCollision(500000, 5);
    DDCounterexampleHunter.HuntResult dd =
        DDCounterexampleHunter.hunt(DDCounterexampleHunter.DD, cases);
    assertFalse("DD counterexample found in product-collision search:\n" + dd, dd.foundAny());
  }

  /** DD must have no counterexamples on uniform random inputs across magnitudes. */
  public void testDDSoundUniform() {
    double[] mags = { 1.0, 1.0e3, 1.0e9, 1.0e15 };
    for (double mag : mags) {
      List<double[]> cases = DDCounterexampleHunter.uniform(150000, mag, 23);
      DDCounterexampleHunter.HuntResult dd =
          DDCounterexampleHunter.hunt(DDCounterexampleHunter.DD, cases);
      assertFalse("DD counterexample found (uniform, magnitude " + mag + "):\n" + dd, dd.foundAny());
    }
  }

  /**
   * Characterization: for coordinate magnitudes at or above 2^512 the DD
   * products overflow to Infinity, the determinant becomes NaN, and
   * Orientation.index returns 0 (collinear) for points that are not collinear.
   * Just below the threshold the predicate is still sound.
   */
  public void testDDFailsOnOverflow() {
    // clearly past the overflow threshold: every near-collinear case fails
    DDCounterexampleHunter.HuntResult over =
        DDCounterexampleHunter.hunt(DDCounterexampleHunter.DD,
            DDCounterexampleHunter.extremeMagnitude(2000, 520, 1));
    assertTrue("expected DD to overflow and fail at magnitude 2^520", over.foundAny());

    // a safe magnitude (2^256) far below the threshold remains sound
    DDCounterexampleHunter.HuntResult safe =
        DDCounterexampleHunter.hunt(DDCounterexampleHunter.DD,
            DDCounterexampleHunter.extremeMagnitude(2000, 256, 1));
    assertFalse("DD should be sound well below the overflow threshold:\n" + safe, safe.foundAny());
  }

  /**
   * Characterization: for very small coordinate magnitudes the DD products
   * underflow to zero and Orientation.index returns 0 (collinear) for points
   * that are not collinear.
   */
  public void testDDFailsOnUnderflow() {
    DDCounterexampleHunter.HuntResult under =
        DDCounterexampleHunter.hunt(DDCounterexampleHunter.DD,
            DDCounterexampleHunter.extremeMagnitude(2000, -540, 1));
    assertTrue("expected DD to underflow and fail at magnitude 2^-540", under.foundAny());

    DDCounterexampleHunter.HuntResult safe =
        DDCounterexampleHunter.hunt(DDCounterexampleHunter.DD,
            DDCounterexampleHunter.extremeMagnitude(2000, -256, 1));
    assertFalse("DD should be sound well above the underflow threshold:\n" + safe, safe.foundAny());
  }

  /** The explicit, hand-checked overflow/underflow counterexamples all fail. */
  public void testKnownCounterexamples() {
    for (double[] c : DDCounterexampleHunter.KNOWN_COUNTEREXAMPLES) {
      int expected = RocqRefRunner.refSignExact(c[0], c[1], c[2], c[3], c[4], c[5]);
      int actual = org.locationtech.jts.algorithm.Orientation.index(
          new org.locationtech.jts.geom.Coordinate(c[0], c[1]),
          new org.locationtech.jts.geom.Coordinate(c[2], c[3]),
          new org.locationtech.jts.geom.Coordinate(c[4], c[5]));
      assertTrue("expected exact orientation to be non-collinear", expected != 0);
      assertEquals("known counterexample should show DD reporting collinear (0)",
          0, actual);
      assertTrue("DD result should differ from exact", actual != expected);
    }
  }
}
