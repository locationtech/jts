/*
 * Copyright (c) 2019 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
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

public class IntersectionStressTest {
  
  /**
   * 1 is fully parallel
   */
  private static final double PARALLEL_FACTOR = 0.9999999999;
  
  private static final int MAX_ITER = 1000;
  private static final double MAX_ORD = 1000000;
  private static final double SEG_LEN = 100;
  // make results reproducible
  static Random randGen = new Random(123456);
  
  Map<String, Double> distMap = new HashMap<String, Double>();

  public static void main(String args[]) {
    IntersectionStressTest test = new IntersectionStressTest();
    test.run();
  }

  private void run() {
    for (int i = 0; i < MAX_ITER; i++) {
      doIntersectionTest(i);
    }
    printAverage();
  }

  private void doIntersectionTest(int i) {
    Coordinate basePt = randomCoordinate();
    
    double baseAngle = 2 * Math.PI * randGen.nextDouble();
    
    Coordinate p1 = computeVector(basePt, baseAngle, 0.1 * SEG_LEN);
    Coordinate p2 = computeVector(basePt, baseAngle, 1.1 * SEG_LEN);
    
    double angleTest = baseAngle + PARALLEL_FACTOR * Math.PI; 
    
    Coordinate q1 = computeVector(basePt, angleTest, 0.1 * SEG_LEN);
    Coordinate q2 = computeVector(basePt, angleTest, 1.1 * SEG_LEN);
    
    System.out.println(i + ":  Lines: "
        + WKTWriter.toLineString(p1, p2) + "  -  "
        + WKTWriter.toLineString(q1, q2 ) );
    
    Coordinate intPt = IntersectionAlgorithms.intersectionBasic(p1, p2, q1, q2);
    Coordinate intPtDD = CGAlgorithmsDD.intersection(p1, p2, q1, q2);
    Coordinate intPtCB = IntersectionAlgorithms.intersectionCB(p1, p2, q1, q2);
    Coordinate intPtCond = Intersection.intersection(p1, p2, q1, q2);
    
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
    System.out.println(tag + " : " 
        + WKTWriter.toPoint(intPt)
        + " -- Dist P = " + distP + "    Dist Q = " + distQ);
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
    double x = MAX_ORD * randGen.nextDouble();
    double y = MAX_ORD * randGen.nextDouble();
    return new Coordinate(x, y);
  }
}
