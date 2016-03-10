
/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts;

import java.util.Date;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.NonRobustLineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;



/**
 * @version 1.7
 */
public class LineIntersectorBenchmark implements Runnable {

  public LineIntersectorBenchmark() {
  }

  public static void main(String[] args) {
    LineIntersectorBenchmark lineIntersectorBenchmark = new LineIntersectorBenchmark();
    lineIntersectorBenchmark.run();
  }

  public void run() {
    exercise(new NonRobustLineIntersector());
    exercise(new RobustLineIntersector());
  }

  private void exercise(LineIntersector lineIntersector) {
    System.out.println(lineIntersector.getClass().getName());
    Date start = new Date();
    for (int i = 0; i < 1000000; i++) {
      exerciseOnce(lineIntersector);
    }
    Date end = new Date();
    System.out.println("Milliseconds elapsed: " + (end.getTime() - start.getTime()));
    System.out.println();
  }

  private void exerciseOnce(LineIntersector lineIntersector) {
    Coordinate p1 = new Coordinate(10, 10);
    Coordinate p2 = new Coordinate(20, 20);
    Coordinate q1 = new Coordinate(20, 10);
    Coordinate q2 = new Coordinate(10, 20);
    Coordinate x = new Coordinate(15, 15);
    lineIntersector.computeIntersection(p1, p2, q1, q2);
    lineIntersector.getIntersectionNum();
    lineIntersector.getIntersection(0);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new Coordinate(10, 10);
    p2 = new Coordinate(20, 10);
    q1 = new Coordinate(22, 10);
    q2 = new Coordinate(30, 10);
    lineIntersector.computeIntersection(p1, p2, q1, q2);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new Coordinate(10, 10);
    p2 = new Coordinate(20, 10);
    q1 = new Coordinate(20, 10);
    q2 = new Coordinate(30, 10);
    lineIntersector.computeIntersection(p1, p2, q1, q2);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new Coordinate(10, 10);
    p2 = new Coordinate(20, 10);
    q1 = new Coordinate(15, 10);
    q2 = new Coordinate(30, 10);
    lineIntersector.computeIntersection(p1, p2, q1, q2);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new Coordinate(30, 10);
    p2 = new Coordinate(20, 10);
    q1 = new Coordinate(10, 10);
    q2 = new Coordinate(30, 10);
    lineIntersector.computeIntersection(p1, p2, q1, q2);
    lineIntersector.hasIntersection();

    lineIntersector.computeIntersection(new Coordinate(100, 100), new Coordinate(10, 100),
        new Coordinate(100, 10), new Coordinate(100, 100));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionNum();

    lineIntersector.computeIntersection(new Coordinate(190, 50), new Coordinate(120, 100),
        new Coordinate(120, 100), new Coordinate(50, 150));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionNum();
    lineIntersector.getIntersection(1);

    lineIntersector.computeIntersection(new Coordinate(180, 200), new Coordinate(160, 180),
        new Coordinate(220, 240), new Coordinate(140, 160));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionNum();

    lineIntersector.computeIntersection(new Coordinate(30, 10), new Coordinate(30, 30),
        new Coordinate(10, 10), new Coordinate(90, 11));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionNum();
    lineIntersector.isProper();

    lineIntersector.computeIntersection(new Coordinate(10, 30), new Coordinate(10, 0),
        new Coordinate(11, 90), new Coordinate(10, 10));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionNum();
    lineIntersector.isProper();
  }
}
