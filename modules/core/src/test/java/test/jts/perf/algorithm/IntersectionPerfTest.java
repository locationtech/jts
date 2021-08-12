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

import org.locationtech.jts.algorithm.CGAlgorithmsDD;
import org.locationtech.jts.algorithm.Intersection;
import org.locationtech.jts.geom.Coordinate;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

/**
 * Performance test for various line intersection implementations.
 * These include:
 * <ul>
 * <li>DP - a basic double-precision (DP) implementation, with no attempt at reducing the effects of numerical round-off
 * <li>DP-Cond - a DP implementation in which the inputs are conditioned by translating them to around the origin
 * <li>DP-CB - a DP implementation using the {@link org.locationtech.jts.precision.CommonBitsRemover} functionality
 * <li>DD - an implementation using extended-precision {@link org.locationtech.jts.math.DD} arithmetic
 * <li>DDFilter - an experimental implementation using extended-precision {@link org.locationtech.jts.math.DD} arithmetic
 * along with a filter that uses DP if the accuracy is sufficient
 * </ul>
 * <h2>Results</h2>
 * <ul>
 * <li>DP-Basic is the fastest but least accurate
 * <li>DP-Cond is fairly fast
 * <li>DP-CB is similar in performance to DP-Cond (but less accurate)
 * <li>DD is the slowest implementation
 * <li>the performance of DP-Filter is similar to DP or DD, depending on which method is chosen by the filter
 * <ul>
 * 
 * This test is evaluated together with the accuracy results from {@link IntersectionStressTest}.
 * The conclusion is that the best combination of accuracy and performance 
 * is provided by DP-Cond.
 * 
 * @author Martin Davis
 */
public class IntersectionPerfTest extends PerformanceTestCase {
  private static final int N_ITER = 1000000;
  
  public static void main(String args[]) {
    PerformanceTestRunner.run(IntersectionPerfTest.class);
  }
  
  public IntersectionPerfTest(String name)
  {
    super(name);
    setRunSize(new int[] { 1 });
    setRunIterations(N_ITER);
  }
  
  Coordinate a0 = new Coordinate(0, 0);
  Coordinate a1 = new Coordinate(10, 0);
  Coordinate b0 = new Coordinate(20, 10);
  Coordinate b1 = new Coordinate(20, 20);
  
  Coordinate p0;
  Coordinate p1;
  Coordinate q0;
  Coordinate q1;
  
  public void startRun(int npts)
  {
    p0 = new Coordinate(35613471.6165017, 4257145.3061322933);
    p1 = new Coordinate(35613477.7705378, 4257160.5282227108);
    q0 = new Coordinate(35613477.775057241, 4257160.5396535359);
    q1 = new Coordinate(35613479.856073894, 4257165.9236917039);
  }
  
  public void runDP()
  {
    Coordinate intPt = IntersectionAlgorithms.intersectionBasic(p0, p1, q0, q1);
  }
  
  public void runDD() 
  {
    Coordinate intPt = CGAlgorithmsDD.intersection(p0, p1, q0, q1);
  }
  
  public void runDDWithFilter() 
  {
    Coordinate intPt = IntersectionAlgorithms.intersectionDDWithFilter(p0, p1, q0, q1);
  }
  
  public void runCB()  
  {
    Coordinate intPt = IntersectionAlgorithms.intersectionCB(p0, p1, q0, q1);
  }
  
  public void runCond()  
  {
    Coordinate intPt = Intersection.intersection(p0, p1, q0, q1);
  }
  
  public void runDP_easy() 
  {
    Coordinate intPt = IntersectionAlgorithms.intersectionBasic(a0, a1, b0, b1);
  }
  
  public void runCond_easy() 
  {
    Coordinate intPt = Intersection.intersection(a0, a1, b0, b1);
  }
  
  public void runDD_easy() 
  {
    Coordinate intPt = CGAlgorithmsDD.intersection(a0, a1, b0, b1);
  }
  
  public void runDDWithFilter_easy()  
  {
    Coordinate intPt = IntersectionAlgorithms.intersectionDDWithFilter(a0, a1, b0, b1);
  }
  

}
