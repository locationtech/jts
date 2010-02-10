
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

package test.jts.junit.algorithm;
import junit.framework.TestCase;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.NonRobustCGAlgorithms;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * @version 1.7
 */
public class RobustLineIntersectorTest extends TestCase {

  RobustLineIntersector i = new RobustLineIntersector();

  public RobustLineIntersectorTest(String Name_) {
    super(Name_);
  }//public RobustLineIntersectorTest(String Name_)

  public static void main(String[] args) {
    String[] testCaseName = {RobustLineIntersectorTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }//public static void main(String[] args)

  public void test2Lines() {
    RobustLineIntersector i = new RobustLineIntersector();
    Coordinate p1 = new Coordinate(10, 10);
    Coordinate p2 = new Coordinate(20, 20);
    Coordinate q1 = new Coordinate(20, 10);
    Coordinate q2 = new Coordinate(10, 20);
    Coordinate x = new Coordinate(15, 15);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(RobustLineIntersector.POINT_INTERSECTION, i.getIntersectionNum());
    assertEquals(1, i.getIntersectionNum());
    assertEquals(x, i.getIntersection(0));
    assertTrue(i.isProper());
    assertTrue(i.hasIntersection());
  }

  public void testCollinear1() {
    RobustLineIntersector i = new RobustLineIntersector();
    Coordinate p1 = new Coordinate(10, 10);
    Coordinate p2 = new Coordinate(20, 10);
    Coordinate q1 = new Coordinate(22, 10);
    Coordinate q2 = new Coordinate(30, 10);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(RobustLineIntersector.NO_INTERSECTION, i.getIntersectionNum());
    assertTrue(!i.isProper());
    assertTrue(!i.hasIntersection());
  }

  public void testCollinear2() {
    RobustLineIntersector i = new RobustLineIntersector();
    Coordinate p1 = new Coordinate(10, 10);
    Coordinate p2 = new Coordinate(20, 10);
    Coordinate q1 = new Coordinate(20, 10);
    Coordinate q2 = new Coordinate(30, 10);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(RobustLineIntersector.POINT_INTERSECTION, i.getIntersectionNum());
    assertTrue(!i.isProper());
    assertTrue(i.hasIntersection());
  }

  public void testCollinear3() {
    RobustLineIntersector i = new RobustLineIntersector();
    Coordinate p1 = new Coordinate(10, 10);
    Coordinate p2 = new Coordinate(20, 10);
    Coordinate q1 = new Coordinate(15, 10);
    Coordinate q2 = new Coordinate(30, 10);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(RobustLineIntersector.COLLINEAR_INTERSECTION, i.getIntersectionNum());
    assertTrue(!i.isProper());
    assertTrue(i.hasIntersection());
  }

  public void testCollinear4() {
    RobustLineIntersector i = new RobustLineIntersector();
    Coordinate p1 = new Coordinate(30, 10);
    Coordinate p2 = new Coordinate(20, 10);
    Coordinate q1 = new Coordinate(10, 10);
    Coordinate q2 = new Coordinate(30, 10);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(RobustLineIntersector.COLLINEAR_INTERSECTION, i.getIntersectionNum());
    assertTrue(i.hasIntersection());
  }

  public void testEndpointIntersection() {
    i.computeIntersection(new Coordinate(100, 100), new Coordinate(10, 100),
        new Coordinate(100, 10), new Coordinate(100, 100));
    assertTrue(i.hasIntersection());
    assertEquals(1, i.getIntersectionNum());
  }

  public void testEndpointIntersection2() {
    i.computeIntersection(new Coordinate(190, 50), new Coordinate(120, 100),
        new Coordinate(120, 100), new Coordinate(50, 150));
    assertTrue(i.hasIntersection());
    assertEquals(1, i.getIntersectionNum());
    assertEquals(new Coordinate(120, 100), i.getIntersection(1));
  }

  public void testOverlap() {
    i.computeIntersection(new Coordinate(180, 200), new Coordinate(160, 180),
        new Coordinate(220, 240), new Coordinate(140, 160));
    assertTrue(i.hasIntersection());
    assertEquals(2, i.getIntersectionNum());
  }

  public void testIsProper1() {
    i.computeIntersection(new Coordinate(30, 10), new Coordinate(30, 30),
        new Coordinate(10, 10), new Coordinate(90, 11));
    assertTrue(i.hasIntersection());
    assertEquals(1, i.getIntersectionNum());
    assertTrue(i.isProper());
  }

  public void testIsProper2() {
    i.computeIntersection(new Coordinate(10, 30), new Coordinate(10, 0),
        new Coordinate(11, 90), new Coordinate(10, 10));
    assertTrue(i.hasIntersection());
    assertEquals(1, i.getIntersectionNum());
    assertTrue(! i.isProper());
  }

  public void testIsCCW() {
    assertEquals(1, CGAlgorithms.computeOrientation(
        new Coordinate(-123456789, -40),
        new Coordinate(0, 0),
        new Coordinate(381039468754763d, 123456789)));
  }

  public void testIsCCW2() {
    assertEquals(0, CGAlgorithms.computeOrientation(
        new Coordinate(10, 10),
        new Coordinate(20, 20),
        new Coordinate(0, 0)));
    assertEquals(0, NonRobustCGAlgorithms.computeOrientation(
        new Coordinate(10, 10),
        new Coordinate(20, 20),
        new Coordinate(0, 0)));
  }

  public void testA() {
    Coordinate p1 = new Coordinate(-123456789, -40);
    Coordinate p2 = new Coordinate(381039468754763d, 123456789);
    Coordinate q  = new Coordinate(0, 0);
    LineString l = new GeometryFactory().createLineString(new Coordinate[] {p1, p2});
    Point p = new GeometryFactory().createPoint(q);
    assertEquals(false, l.intersects(p));
    assertEquals(false, CGAlgorithms.isOnLine(q, new Coordinate[] {p1, p2}));
    assertEquals(-1, CGAlgorithms.computeOrientation(p1, p2, q));
  }

}
