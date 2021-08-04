/*
 * Copyright (c) 2019 Martin Davis
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.locationtech.jts.algorithm.CGAlgorithmsDD;
import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.algorithm.Intersection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTWriter;

/**
 * Stress test for accuracy of various line intersection implementations.
 * The test is to compute the intersection point of pairs of line segments
 * with realistically large ordinate values, 
 * for angles of incidence which become increasingly close to parallel.
 * The measure of accuracy is the sum of the distances of the computed point from the two lines.
 * <p>
 * The intersection algorithms are:
 * <ul>
 * <li>DP - a basic double-precision (DP) implementation, with no attempt at reducing the effects of numerical round-off
 * <li>DP-Cond - a DP implementation in which the inputs are conditioned by translating them to around the origin
 * <li>DP-CB - a DP implementation using the {@link org.locationtech.jts.precision.CommonBitsRemover} functionality
 * <li>DD - an implementation using extended-precision {@link org.locationtech.jts.math.DD} arithmetic
 * </ul>
 * <h2>Results</h2>
 * <ul>
 * <li>DP-Basic is the least accurate
 * <li>DP-Cond has similar accuracy to DD
 * <li>DP-CB accuracy is better than DP, but degrades significantly as angle becomes closer to parallel
 * <li>DD is (presumably) the most accurate
 * <ul>
 * 
 * 
 * @author Martin Davis
 *
 */
public class IntersectionStressTest {
    
  private static final int MAX_ITER = 1000;
  private static final double ORDINATE_MAGNITUDE = 1000000;
  private static final double SEG_LEN = 100;
  // make results reproducible
  static Random randGen = new Random(123456);
  
  Map<String, Double> distMap = new HashMap<String, Double>();

  private boolean verbose = false;

  public static void main(String args[]) {
    IntersectionStressTest test = new IntersectionStressTest();
    test.run();
  }

  private void run() {
    run(0.9);
    run(0.999);
    run(0.999999);
    run(0.99999999);
  }
  
  /**
   * Run tests for a given incident angle factor.
   * The angle between the segments is <code>PI * incidentAngleFactor</code>.
   * A factor closer to 1 means the segments are more nearly parallel.
   * A factor of 1 means they are parallel.
   * 
   * @param incidentAngleFactor the factor of PI between the two segments
   */
  private void run(double incidentAngleFactor) {
    for (int i = 0; i < MAX_ITER; i++) {
      doIntersectionTest(i, incidentAngleFactor);
    }
    System.out.println("\nIncident angle factor = " + incidentAngleFactor);
    printAverage();
  }

  private void doIntersectionTest(int i, double incidentAngleFactor) {
    Coordinate basePt = randomCoordinate();
    
    double baseAngle = 2 * Math.PI * randGen.nextDouble();
    
    Coordinate p1 = computeVector(basePt, baseAngle, 0.1 * SEG_LEN);
    Coordinate p2 = computeVector(basePt, baseAngle, 1.1 * SEG_LEN);
    
    double angleBetween = baseAngle + incidentAngleFactor * Math.PI; 
    
    Coordinate q1 = computeVector(basePt, angleBetween, 0.1 * SEG_LEN);
    Coordinate q2 = computeVector(basePt, angleBetween, 1.1 * SEG_LEN);
    
    Coordinate intPt = IntersectionAlgorithms.intersectionBasic(p1, p2, q1, q2);
    Coordinate intPtDD = CGAlgorithmsDD.intersection(p1, p2, q1, q2);
    Coordinate intPtCB = IntersectionAlgorithms.intersectionCB(p1, p2, q1, q2);
    Coordinate intPtCond = Intersection.intersection(p1, p2, q1, q2);
    if (verbose ) {
      System.out.println(i + ":  Lines: "
          + WKTWriter.toLineString(p1, p2) + "  -  "
          + WKTWriter.toLineString(q1, q2 ) );
    }
    printStats("DP    ", intPt, p1, p2, q1, q2);
    printStats("CB    ", intPtCB, p1, p2, q1, q2);
    printStats("Cond  ", intPtCond, p1, p2, q1, q2);
    printStats("DD    ", intPtDD, p1, p2, q1, q2);
  }
  
  private void printStats(String tag, Coordinate intPt, Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
    double distP = Distance.pointToLinePerpendicular(intPt, p1, p2);    
    double distQ = Distance.pointToLinePerpendicular(intPt, q1, q2);
    addStat(tag, distP);
    addStat(tag, distQ);
    if (verbose ) {
      System.out.println(tag + " : " 
          + WKTWriter.toPoint(intPt)
          + " -- Dist P = " + distP + "    Dist Q = " + distQ);
    }
  }

  private void addStat(String tag, double dist) {
    double distTotal = 0.0;
    if (distMap.containsKey(tag)) {
      distTotal = distMap.get(tag);
    }
    distTotal += dist;
    distMap.put(tag, distTotal);
  }
  
  private void printAverage() {
    System.out.println("Average distance from lines");
    for (String key : distMap.keySet()) {
      double distTotal = distMap.get(key);
      double avg = distTotal / MAX_ITER;
      System.out.println(key + " : " + avg );
    }
  }
  private Coordinate computeVector(Coordinate basePt, double angle, double len) {
    double x = basePt.getX() + len * Math.cos(angle);
    double y = basePt.getY() + len * Math.sin(angle);
    return new Coordinate(x, y);
  }

  private Coordinate randomCoordinate() {
    double x = ORDINATE_MAGNITUDE * randGen.nextDouble();
    double y = ORDINATE_MAGNITUDE * randGen.nextDouble();
    return new Coordinate(x, y);
  }
}
