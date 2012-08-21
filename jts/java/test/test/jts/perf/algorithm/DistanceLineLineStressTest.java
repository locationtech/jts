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
