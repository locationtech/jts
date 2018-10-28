package org.locationtech.jts.algorithm;

import junit.textui.TestRunner;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import test.jts.GeometryTestCase;

public class PointLocationOn4DLineTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(PointLocationOn4DLineTest.class);
  }

  public PointLocationOn4DLineTest(String name) {
    super(name);
  }

  public void testOnVertex() throws Exception {
    checkOnLine(20, 20, "LINESTRINGZM (0 0 0 0, 20 20 20 20, 30 30 30 30)", true);
  }

  public void testOnSegment() throws Exception {
    checkOnLine(10, 10, "LINESTRINGZM (0 0 0 0, 20 20 20 20, 0 40 40 40)", true);
    checkOnLine(10, 30, "LINESTRINGZM (0 0 0 0, 20 20 20 20, 0 40 40 40)", true);
  }

  public void testNotOnLine() throws Exception {
    checkOnLine(0, 100, "LINESTRINGZM (10 10 10 10, 20 10 10 10, 30 10 10 10)", false);
  }

  void checkOnLine(double x, double y, String wktLine, boolean expected) {
    LineString line = (LineString) read(wktLine);
    assertTrue(expected == PointLocation.isOnLine(new Coordinate(x,y), line.getCoordinates()));
    assertTrue(expected == PointLocation.isOnLine(new Coordinate(x,y), line.getCoordinateSequence()));
  }

}
