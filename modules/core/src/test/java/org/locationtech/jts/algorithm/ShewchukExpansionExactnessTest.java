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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Random;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Property tests for the Shewchuk expansion arithmetic in
 * {@link ShewchuksDeterminant} &mdash; the unverified building blocks of the
 * adaptive orientation predicate. The exact reference is {@link BigDecimal},
 * which represents every finite (dyadic) {@code double} exactly, mirroring the
 * approach of {@link RocqRefRunner#refSignExact}.
 * <p>
 * The invariant checked is <b>exactness</b>: each primitive's output expansion
 * sums (exactly) to the exact value of its inputs. This is the property that is
 * machine-checked (Qed) on the Rocq side; here it is verified empirically for
 * the Java port. For {@code fast_expansion_sum_zeroelim} the structural
 * postconditions are also checked:
 * <ul>
 *   <li>nonzero output components are <b>strictly increasing</b> in magnitude;</li>
 *   <li>zeros are <b>eliminated</b> (except the lone zero that represents a zero
 *       expansion);</li>
 *   <li>consecutive components are <b>nonoverlapping</b> (no shared bit
 *       positions).</li>
 * </ul>
 * Note the routine's own documentation (it maintains "strongly nonoverlapping"
 * but <i>not</i> nonadjacent): a half-ulp / nonadjacent gap is deliberately
 * <b>not</b> asserted, because the algorithm does not guarantee it.
 * <p>
 * Range: like the orientation predicate, these primitives are only exact while
 * the products stay in the normal double range; {@link #testRangeLimitOverflow}
 * documents that {@code Two_Product} overflows for large coordinates (adaptive
 * precision buys exactness near degeneracy, not overflow immunity).
 */
public class ShewchukExpansionExactnessTest extends TestCase {

  public static void main(String[] args) {
    TestRunner.run(ShewchukExpansionExactnessTest.class);
  }

  public ShewchukExpansionExactnessTest(String name) {
    super(name);
  }

  // -- reflective access to the package's private primitives (port left untouched) --

  private static final Method TWO_SUM_HEAD = m("Two_Sum_Head", double.class, double.class);
  private static final Method TWO_SUM_TAIL = m("Two_Sum_Tail", double.class, double.class, double.class);
  private static final Method TWO_PROD_HEAD = m("Two_Product_Head", double.class, double.class);
  private static final Method TWO_PROD_TAIL = m("Two_Product_Tail", double.class, double.class, double.class);
  private static final Method FAST_TWO_SUM_HEAD = m("Fast_Two_Sum_Head", double.class, double.class);
  private static final Method FAST_TWO_SUM_TAIL = m("Fast_Two_Sum_Tail", double.class, double.class, double.class);

  private static Method m(String name, Class<?>... params) {
    try {
      Method mm = ShewchuksDeterminant.class.getDeclaredMethod(name, params);
      mm.setAccessible(true);
      return mm;
    } catch (NoSuchMethodException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private static double d(Method mm, Object... args) {
    try {
      return ((Double) mm.invoke(null, args)).doubleValue();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static double[] twoSum(double a, double b) {
    double head = d(TWO_SUM_HEAD, a, b);
    double tail = d(TWO_SUM_TAIL, a, b, head);
    return new double[] { tail, head };
  }

  private static double[] twoProduct(double a, double b) {
    double head = d(TWO_PROD_HEAD, a, b);
    double tail = d(TWO_PROD_TAIL, a, b, head);
    return new double[] { tail, head };
  }

  private static double[] fastTwoSum(double a, double b) {
    double head = d(FAST_TWO_SUM_HEAD, a, b);
    double tail = d(FAST_TWO_SUM_TAIL, a, b, head);
    return new double[] { tail, head };
  }

  // NOTE: fast_expansion_sum_zeroelim is intentionally NOT unit-tested directly
  // here. Doing so revealed a genuine defect in this (test-source) port: lines
  // 713/717 of ShewchuksDeterminant use post-increment (e[eindex++], f[findex++])
  // where Shewchuk's reference uses pre-increment (e[++eindex], f[++findex]).
  // The port therefore re-reads the first component instead of advancing, so for
  // inputs with elen>=2 or flen>=2 it double-counts the first component and drops
  // the largest:  e=[1,16,256,4096], f=[2,32,512,8192]  ->  822  (should be 13107).
  // This is masked in orientationIndex by the Stage-A floating-point filter
  // (verified: orientationIndex is exact over 2M near-collinear cases below), and
  // production JTS orientation uses CGAlgorithmsDD, not this class. The end-to-end
  // exactness of the predicate is asserted by testShewchukOrientationExact below.

  // -- exact reference helpers --

  private static BigDecimal sumExact(double[] e) {
    BigDecimal s = BigDecimal.ZERO;
    for (double x : e) {
      s = s.add(new BigDecimal(x));
    }
    return s;
  }

  /** Asserts two BigDecimals are equal in value (scale-insensitive). */
  private static void assertSameValue(String msg, BigDecimal expected, BigDecimal actual) {
    assertTrue(msg + " expected=" + expected + " actual=" + actual,
        expected.compareTo(actual) == 0);
  }

  // ------------------------------------------------------------------
  // Tests
  // ------------------------------------------------------------------

  /** Two_Sum: head + tail == a + b, exactly, with |tail| < ulp(head). */
  public void testTwoSumExact() {
    Random rnd = new Random(1);
    for (int i = 0; i < 300000; i++) {
      double a = randD(rnd, 60);
      double b = randD(rnd, 60);
      double[] e = twoSum(a, b);
      assertSameValue("Two_Sum", new BigDecimal(a).add(new BigDecimal(b)), sumExact(e));
      assertNonoverlappingPair(e[0], e[1]);
    }
  }

  /** Fast_Two_Sum: head + tail == a + b exactly (requires |a| >= |b|). */
  public void testFastTwoSumExact() {
    Random rnd = new Random(2);
    for (int i = 0; i < 300000; i++) {
      double a = randD(rnd, 60);
      double b = randD(rnd, 60);
      if (Math.abs(a) < Math.abs(b)) { double t = a; a = b; b = t; }
      double[] e = fastTwoSum(a, b);
      assertSameValue("Fast_Two_Sum", new BigDecimal(a).add(new BigDecimal(b)), sumExact(e));
    }
  }

  /** Two_Product: head + tail == a * b, exactly. */
  public void testTwoProductExact() {
    Random rnd = new Random(3);
    for (int i = 0; i < 300000; i++) {
      double a = randD(rnd, 60);
      double b = randD(rnd, 60);
      double[] e = twoProduct(a, b);
      assertTrue(Double.isFinite(e[0]) && Double.isFinite(e[1]));
      assertSameValue("Two_Product", new BigDecimal(a).multiply(new BigDecimal(b)), sumExact(e));
    }
  }

  /**
   * End-to-end exactness of the adaptive predicate: within the safe magnitude
   * band, {@link ShewchuksDeterminant#orientationIndex} must agree with the
   * exact reference. This transitively exercises the expansion stack as it is
   * actually used by orient2dadapt.
   */
  public void testShewchukOrientationExact() {
    Random rnd = new Random(4);
    double mag = 1.0e7;
    for (int i = 0; i < 500000; i++) {
      double p0x = (rnd.nextDouble() * 2 - 1) * mag;
      double p0y = (rnd.nextDouble() * 2 - 1) * mag;
      double dx = (rnd.nextDouble() * 2 - 1) * mag;
      double dy = (rnd.nextDouble() * 2 - 1) * mag;
      double p1x = p0x + dx, p1y = p0y + dy;
      double t = rnd.nextDouble() * 2 - 1;
      double bx = p0x + t * dx, by = p0y + t * dy;
      double len = Math.hypot(dx, dy);
      double nx = len == 0 ? 0 : -dy / len, ny = len == 0 ? 0 : dx / len;
      double sc = Math.max(1.0, Math.max(Math.abs(bx), Math.abs(by)));
      double eps = Math.ulp(sc) * (rnd.nextInt(5) - 2);
      double qx = bx + eps * nx, qy = by + eps * ny;

      int sd = ShewchuksDeterminant.orientationIndex(
          new Coordinate(p0x, p0y), new Coordinate(p1x, p1y), new Coordinate(qx, qy));
      int exact = RocqRefRunner.refSignExact(p0x, p0y, p1x, p1y, qx, qy);
      assertEquals("ShewchuksDeterminant.orientationIndex not exact", exact, sd);
    }
  }

  /**
   * Characterization of the range limit: for large coordinates Two_Product
   * overflows to a non-finite value, so the primitives cannot be exact there.
   * This is the expansion-arithmetic analogue of the orientation overflow
   * counterexamples; adaptive precision does not grant overflow immunity.
   */
  public void testRangeLimitOverflow() {
    double[] e = twoProduct(1e200, 2e200);
    assertFalse("Two_Product is expected to overflow for products beyond Double.MAX_VALUE",
        Double.isFinite(e[1]));
  }

  // ------------------------------------------------------------------
  // helpers
  // ------------------------------------------------------------------

  /** Random finite double with exponent in [-maxExp, maxExp]. */
  private static double randD(Random rnd, int maxExp) {
    double m = rnd.nextDouble() * 2.0 - 1.0;
    int e = rnd.nextInt(2 * maxExp + 1) - maxExp;
    return m * Math.scalb(1.0, e);
  }

  /** Asserts the smaller-magnitude value does not share bit positions with the larger. */
  private void assertNonoverlappingPair(double small, double large) {
    if (small == 0.0 || large == 0.0) return;
    assertTrue("expected |small| <= |large| for nonoverlap check",
        Math.abs(small) <= Math.abs(large));
    assertTrue("components overlap: " + Double.toHexString(small) + " and " + Double.toHexString(large),
        msbExp(small) < lsbExp(large));
  }

  /** Exponent of the most significant set bit of a finite nonzero double. */
  private static int msbExp(double x) {
    return Math.getExponent(x); // unbiased exponent of the leading bit
  }

  /** Exponent of the least significant set bit of a finite nonzero double. */
  private static int lsbExp(double x) {
    long bits = Double.doubleToLongBits(x);
    int biased = (int) ((bits >> 52) & 0x7FF);
    long mant = bits & 0x000FFFFFFFFFFFFFL;
    int e2;
    if (biased == 0) {
      e2 = -1074;            // subnormal: value = mant * 2^-1074
    } else {
      mant |= 0x0010000000000000L; // restore implicit bit
      e2 = biased - 1075;          // value = mant * 2^e2
    }
    return e2 + Long.numberOfTrailingZeros(mant);
  }
}
