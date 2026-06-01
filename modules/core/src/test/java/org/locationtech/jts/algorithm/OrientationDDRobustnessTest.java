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
 * The suite asserts two complementary things:
 * <ul>
 *   <li>the search is effective &mdash; it readily finds counterexamples for
 *       the naive and legacy predicates that DD replaced; and</li>
 *   <li>the current DD predicate ({@link Orientation#index}) produces no
 *       counterexamples under the same adversarial inputs.</li>
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

  /** DD must have no counterexamples on adversarial near-collinear inputs. */
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
}
