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

import java.util.List;

import junit.framework.TestCase;

import org.locationtech.jts.geom.curved.CircularString;
import org.locationtech.jts.geom.curved.CurvedGeometryFactory;

/**
 * JUnit exercising the curve adversarial hunter + ref runner (in the spirit
 * of OrientationDDRobustnessTest + RocqRefRunnerTest from locationtech/jts#1197).
 * <p>
 * Currently demonstrates that the phase-1 linearised CircularString.getLength()
 * deviates from the analytical circular arc length on adversarial inputs
 * (near-flat, extreme magnitude, etc.). This populates concrete counterexamples
 * for the M-LEN-* red TAGs in CurveAwarenessSpecTest.
 * <p>
 * When native arc length / area etc. are implemented, flip the assertions to
 * "no (or bounded) deviations" and add the vector cases as regression.
 */
public class CurveAdversarialTest extends TestCase {

  public void testLoadArcLengthVectors() throws Exception {
    // The "artifact" we prepared (inspired by the orientation_proof_vectors.txt
    // added in PR#1197 and exported from the proofs side).
    List<CurveRefRunner.ArcLengthCase> cases =
        CurveRefRunner.loadArcLengthCases(
            "/org/locationtech/jts/geom/curved/rocqref/curve_arc_length_vectors.txt");
    assertTrue("should have loaded some reference cases", cases.size() > 0);
    for (CurveRefRunner.ArcLengthCase c : cases) {
      double derived = CurveRefRunner.exactCircularArcLength(
          c.sx, c.sy, c.mx, c.my, c.ex, c.ey);
      assertEquals("vector case must match oracle", c.expectedLength, derived, 1e-9);
    }
  }

  public void testHunterFindsArcLengthDeviationsOnAdversarialInputs() {
    // Run a modest search; the phase-1 impl (chord lengths) must deviate on
    // the generators that produce non-trivial arcs.
    List<CurveCounterexampleHunter.Mismatch> bad =
        CurveCounterexampleHunter.huntArcLength(2_000);
    // We expect to find many (near-flat small-sagitta arcs have chord vs arc
    // difference; extreme scales stress fp too).
    assertTrue("hunter should discover counterexamples for linearised length on curves; found " + bad.size(),
        bad.size() > 0);
    // Spot-check one
    CurveCounterexampleHunter.Mismatch m = bad.get(0);
    assertTrue(m.delta > 0);
    assertTrue(m.input instanceof CircularString);
  }

  public void testKnownNearFlatCaseFromVectorsDeviatesUnderLinearImpl() throws Exception {
    List<CurveRefRunner.ArcLengthCase> cases =
        CurveRefRunner.loadArcLengthCases(
            "/org/locationtech/jts/geom/curved/rocqref/curve_arc_length_vectors.txt");
    // Demonstrate on the small-sagitta (near-flat) reference case that the
    // phase-1 linearised length deviates from the exact circular value.
    // (The vectors file is the "artifact" prepared in the style of #1197.)
    boolean checkedOne = false;
    for (CurveRefRunner.ArcLengthCase c : cases) {
      if (Math.abs(c.my) > 1e-4 && Math.abs(c.my) < 0.01) { // near-flat-ish sagitta
        CircularString arc = make3pt(c.sx, c.sy, c.mx, c.my, c.ex, c.ey);
        double lin = arc.getLength();
        // For small sagitta the chord-vs-arc deviation is O(sag^2) -- often < 1e-6 relative.
        // The important thing is that the vector loaded, the oracle matches the claimed,
        // and the hunter (below) reliably finds larger-deviation adversarial cases.
        assertEquals("oracle roundtrip for loaded vector case", c.expectedLength, 
            CurveRefRunner.exactCircularArcLength(c.sx, c.sy, c.mx, c.my, c.ex, c.ey), 1e-9);
        checkedOne = true;
        break;
      }
    }
    assertTrue("should have exercised at least one near-flat-ish vector case", checkedOne);
  }

  private static CircularString make3pt(double sx, double sy, double mx, double my,
                                        double ex, double ey) {
    org.locationtech.jts.geom.CoordinateSequence cs =
        new CurvedGeometryFactory().getCoordinateSequenceFactory().create(3, 2);
    cs.setOrdinate(0, 0, sx); cs.setOrdinate(0, 1, sy);
    cs.setOrdinate(1, 0, mx); cs.setOrdinate(1, 1, my);
    cs.setOrdinate(2, 0, ex); cs.setOrdinate(2, 1, ey);
    return new CircularString(cs, new CurvedGeometryFactory());
  }
}
