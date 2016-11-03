/*
 * Copyright (c) 2016 Martin Davis.
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

import java.util.Random;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;


public class DistanceLineLineStressTest extends TestCase
{

  public static void main(String args[])
  {
    TestRunner.run(DistanceLineLineStressTest.class);
  }

  public DistanceLineLineStressTest(String name)
  {
    super(name);
  }

  public void testRandomDisjointCollinearSegments() throws Exception
  {
    int n = 1000000;
    int failCount = 0;
    for (int i = 0; i < n; i++) {
      //System.out.println(i);
      Coordinate[] seg = randomDisjointCollinearSegments();
      if (0 == CGAlgorithms.distanceLineLine(seg[0], seg[1], seg[2], seg[3])) {
        /*
        System.out.println("FAILED! - "
            + WKTWriter.toLineString(seg[0], seg[1]) + "  -  "
            + WKTWriter.toLineString(seg[2], seg[3]));
            */
        failCount++;
      }
    }
    System.out.println("# failed = " + failCount + " out of " + n);
  }

  // make results reproducible
  static Random randGen = new Random(123456);
  
  private static Coordinate[] randomDisjointCollinearSegments()
  {
    double slope = randGen.nextDouble();
    Coordinate[] seg = new Coordinate[4];

    double gap = 1;
    double x1 = 10;
    double x2 = x1 + gap;
    double x3 = x1 + gap + 10;
    seg[0] = new Coordinate(0, 0);
    seg[1] = new Coordinate(x1, slope * x1);
    seg[2] = new Coordinate(x2, slope * x2);
    seg[3] = new Coordinate(x3, slope * x3);

    return seg;
  }

}
