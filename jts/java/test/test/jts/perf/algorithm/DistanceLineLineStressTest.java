/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Martin Davis
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Martin Davis BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package test.jts.perf.algorithm;

import java.util.Random;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.WKTWriter;

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
