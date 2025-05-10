/*
 * Copyright (c) 2025 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.algorithm;

import org.locationtech.jts.algorithm.NonRobustCGAlgorithms;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.io.WKTWriter;

/**
 * Stress test for the Orientation Index implementation.
 * 
 * A robust orientation index implementation should be internally consistent
 * - i.e. it should produce the same result for the 3 possible 
 * permutations of the input coordinates which have the same orientation:
 * 
 * p0-p1 / p2    p1-p2 / p0    p2-p0 / p1     
 * 
 * Also, the reverse orientations should themselves be consistent, 
 * and be opposite in sign to the forward orientation.
 * 
 * The robust implementation uses DoubleDouble arithmetic and a filter to improve computation time. 
 * It is compared to the simple Floating-Point orientation computation, which is not robust.
 * 
 * @author mdavis
 *
 */
public class OrientationIndexStressTest {
  private static final double MAX_VAL = 1000;
  private static final int MAX_RUN = 10_000_000;
  
  private static char orientSym(int orientationIndex) {
    if (orientationIndex < 0) return '-';
    if (orientationIndex > 0) return '+';
    return '0';
  }

  public static void main(String args[]) {
    OrientationIndexStressTest test = new OrientationIndexStressTest();
    test.run();
  }

  private int failDD = 0;
  private int failFP = 0;
  private boolean isVerbose = false;

  private void run() {
    for (int i = 1; i <= MAX_RUN; i++) {
      runTest(i);
      
      if (i % 1000 == 0) {
        System.out.printf("Num tests: %,d  DD fail = %,d (%d%%)  FP fail = %,d (%d%%)\n", 
            i, 
            failDD, (int) (100 * failDD / (double) i),
            failFP, (int) (100 * failFP / (double) i) );
      }
    }
  }

  private void runTest(int i) {
    Coordinate p0 = randomCoord();
    Coordinate p1 = randomCoord();
    
    Coordinate p2 = LineSegment.midPoint(p0, p1);
    
    //-- test with offset point - no errors found
    /*
    LineSegment ls = new LineSegment(p0, p1);
    Coordinate p2 = ls.pointAlongOffset(0.4, 1);
    */
    
    boolean isCorrectDD = isConsistentDD(p0, p1, p2);
    boolean isCorrectFP = isConsistentFP(p0, p1, p2);
    
    if (! isCorrectDD) failDD++;
    if (! isCorrectFP) failFP++;
    
    if (isVerbose) {
      System.out.println(WKTWriter.toLineString(p0, p1) 
          + " - " + WKTWriter.toPoint(p2));
    }

  }

  interface OrientationFunction {
    int index(Coordinate p0, Coordinate p1, Coordinate p2);
  }
  
  boolean isConsistent(String tag, Coordinate p0, Coordinate p1, Coordinate p2, 
      OrientationFunction orientFunc )
    {
    int orient0 = orientFunc.index(p0, p1, p2);
    int orient1 = orientFunc.index(p1, p2, p0);
    int orient2 = orientFunc.index(p2, p0, p1);
    boolean isConsistentForward = orient0 == orient1 && orient0 == orient2;
  
    int orientRev0 = orientFunc.index(p1, p0, p2);
    int orientRev1 = orientFunc.index(p0, p2, p1);
    int orientRev2 = orientFunc.index(p2, p1, p0);
    boolean isConsistentRev = orientRev0 == orientRev1 && orientRev0 == orientRev2;
  
    boolean isConsistent = isConsistentForward && isConsistentRev;
    if (isConsistent) {
      boolean isOpposite = orient0 == -orientRev0;
        isConsistent &= isOpposite;
    }
  
    if (isVerbose) {
        String consistentInd = isConsistent ? "  " : "<!";
        System.out.format("%s %c%c%c %c%c%c %s ", tag, 
            orientSym(orient0), orientSym(orient1), orientSym(orient2),
            orientSym(orientRev0), orientSym(orientRev1), orientSym(orientRev2),
            consistentInd);
    }
    return isConsistent;
  }
  
  boolean isConsistentDD(Coordinate p0, Coordinate p1, Coordinate p2)
  {
    return isConsistent("DD", p0, p1, p2, 
    (pt0,  pt1,  pt2) -> {
        return Orientation.index(pt0, pt1, pt2);
    });
  }
  
  boolean isConsistentFP(Coordinate p0, Coordinate p1, Coordinate p2)
  {
    return isConsistent("FP", p0, p1, p2, 
    (pt0,  pt1,  pt2) -> {
        return NonRobustCGAlgorithms.orientationIndex(pt0, pt1, pt2);
    });
  }
  
  private Coordinate randomCoord() {
    double x = MAX_VAL * Math.random();
    double y = MAX_VAL * Math.random();
    return new Coordinate(x, y);
  }
}
