
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package test.jts;

import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.Date;



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
