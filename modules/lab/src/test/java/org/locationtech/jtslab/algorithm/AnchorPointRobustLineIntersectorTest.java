package org.locationtech.jtslab.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jtslab.noding.anchorpoint.AnchorPointNodingTest;

public class AnchorPointRobustLineIntersectorTest extends TestCase
{

  public static void main(String[] args) {
    TestRunner.run(AnchorPointNodingTest.class);
  }

  public AnchorPointRobustLineIntersectorTest(String name) { super(name); }

  public void test1() {

    Coordinate p0 = new Coordinate(0, 0);
    Coordinate p1 = new Coordinate(10, 0);
    Coordinate q0 = new Coordinate(8, 5);
    Coordinate q1 = new Coordinate(7, -5);

    AnchorPointRobustLineIntersector li = new AnchorPointRobustLineIntersector(3);
    AddVertexAnchorPoints(li.getAnchorPoints(),new Coordinate[] { p0, p1, q0, q1 });

    li.setPrecisionModel(new PrecisionModel(10));
    Coordinate res = li.intersection(p0, p1, q0, q1);
    assertEquals(p1, res);
  }

  public void test2() {

    Coordinate p0 = new Coordinate(0, 0);
    Coordinate p1 = new Coordinate(10, 0);
    Coordinate q0 = new Coordinate(8, 5);
    Coordinate q1 = new Coordinate(7, -5);

    AnchorPointRobustLineIntersector li = new AnchorPointRobustLineIntersector(1);
    AddVertexAnchorPoints(li.getAnchorPoints(),new Coordinate[] { p0, p1, q0, q1 });

    li.setPrecisionModel(new PrecisionModel(10));
    Coordinate res = li.intersection(p0, p1, q0, q1);
    assertEquals(new Coordinate(7.5, 0), res);
  }

  private static void AddVertexAnchorPoints(KdTree res, Coordinate[] coordinates) {
    for (int i = 0; i < coordinates.length; i++)
      res.insert(coordinates[i], new AnchorPoint(coordinates[i], true));
  }
}
