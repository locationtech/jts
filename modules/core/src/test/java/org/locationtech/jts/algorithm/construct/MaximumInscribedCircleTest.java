package org.locationtech.jts.algorithm.construct;

import org.junit.Assert;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import test.jts.GeometryTestCase;

public class MaximumInscribedCircleTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(MaximumInscribedCircleTest.class);
  }

  public MaximumInscribedCircleTest(String name) { super(name); }
  
  public void testSquare() {
    checkCircle("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))", 
       0.001, 150, 150, 50 );
  }

  public void testDiamond() {
    checkCircle("POLYGON ((150 250, 50 150, 150 50, 250 150, 150 250))", 
       0.001, 150, 150, 70.71 );
  }

  public void testCircle() {
    Geometry centre = read("POINT (100 100)");
    Geometry circle = centre.buffer(100, 20);
    // MIC radius is less than 100 because buffer boundary segments lie inside circle
    checkCircle(circle, 0.01, 100, 100, 99.92);
  }

  public void testKite() {
    checkCircle("POLYGON ((100 0, 200 200, 300 200, 300 100, 100 0))", 
       0.01, 238.19, 138.19, 61.80 );
  }

  public void testKiteWithHole() {
    checkCircle("POLYGON ((100 0, 200 200, 300 200, 300 100, 100 0), (200 150, 200 100, 260 100, 200 150))", 
       0.01, 257.47, 157.47, 42.52 );
  }

  public void testDoubleKite() {
    checkCircle("MULTIPOLYGON (((150 200, 100 150, 150 100, 250 150, 150 200)), ((400 250, 300 150, 400 50, 560 150, 400 250)))", 
       0.01, 411.38, 149.99, 78.75 );
  }

  /**
   * Invalid polygon collapsed to a line
   */
  public void testCollapsedLine() {
    checkCircle("POLYGON ((100 100, 200 200, 100 100, 100 100))", 
       0.01);
  }

  /**
   * Invalid polygon collapsed to a flat line
   * (originally caused infinite loop)
   */
  public void testCollapsedLineFlat() {
    checkCircle("POLYGON((1 2, 1 2, 1 2, 1 2, 3 2, 1 2))",
        0.01);
  }

  /**
   * Invalid polygon collapsed to a point
   */
  public void testCollapsedPoint() {
    checkCircle("POLYGON ((100 100, 100 100, 100 100, 100 100))", 
       0.01, 100, 100, 0 );
  }
  
  /**
   * Tests that a nearly flat geometry doesn't make the initial cell grid huge.
   * 
   * See https://github.com/libgeos/geos/issues/875
   */
  public void testNearlyFlat() {
    checkCircle("POLYGON ((59.3 100.00000000000001, 99.7 100.00000000000001, 99.7 100, 59.3 100, 59.3 100.00000000000001))", 
       0.01 );
  }
  
  public void testVeryThin() {
    checkCircle("POLYGON ((100 100, 200 300, 300 100, 450 250, 300 99.999999, 200 299.99999, 100 100))", 
       0.01 );
  }

  /**
   * This tests regression in GeoTools PolyLabellerTest.testDoubleDiamondHole when updating to JTS 1.20.0.
   */
  public void testDoubleDiamondHole() {
    Polygon polygon = (Polygon) read("Polygon ((0 5, 5 10, 10 6, 15 10, 20 5, 15 0, 10 4, 5 0, 0 5),(5.4267578125 6.68164062499999822, 3.7451171875 5.30761718749999822, 5.365234375 3.21582031249999822, 8.3388671875 5.08203124999999822, 5.4267578125 6.68164062499999822))");
    Point point = MaximumInscribedCircle.getCenter(polygon, 1);
    Point expected = (Point) read("Point (15 5)");

    // JTS 1.20.0 changed started cell from centroid to interior point resulting in a small difference from expected location
    double delta = point.distance(expected);
    Assert.assertTrue("close to expected label position: " + delta, delta < 1.0);
  }

  public void testDoubleDiamond() {
    Polygon polygon = (Polygon) read("POLYGON((0 5, 5 10, 10 6, 15 10, 20 5, 15 0, 10 4, 5 0, 0 5))");
    Point point = MaximumInscribedCircle.getCenter(polygon, 1);

    // There are two valid locations:
    // JTS 1.19.0 previously preferred this location
    Point expected1 = (Point) read("POINT(5 5)");
    // JTS 1.20.0 prefers this location
    Point expected2 = (Point) read("POINT(15 5)");

    // JTS 1.20.0 switched MaximumInscribedCircle grid start from centroid to interior point
    double delta1 = point.distance(expected1);
    double delta2 = point.distance(expected2);
    Assert.assertTrue("close to expected label position", delta1 < 1.0 || delta2 < 1.0 );
  }
  
  /**
   * A coarse distance check, mainly testing 
   * that there is not a huge number of iterations.
   * (This will be revealed by CI taking a very long time!)
   * 
   * @param wkt
   * @param tolerance
   */
  private void checkCircle(String wkt, double tolerance) {
    Geometry geom = read(wkt);
    MaximumInscribedCircle mic = new MaximumInscribedCircle(geom, tolerance); 
    Geometry centerPoint = mic.getCenter();
    double dist = geom.getBoundary().distance(centerPoint);
    assertTrue(dist < 2 * tolerance);
  }
  
  private void checkCircle(String wkt, double tolerance, 
      double x, double y, double expectedRadius) {
    checkCircle(read(wkt), tolerance, x, y, expectedRadius);
  }
  
  private void checkCircle(Geometry geom, double tolerance, 
      double x, double y, double expectedRadius) {
    MaximumInscribedCircle mic = new MaximumInscribedCircle(geom, tolerance); 
    Geometry centerPoint = mic.getCenter();
    Coordinate centerPt = centerPoint.getCoordinate();
    Coordinate expectedCenter = new Coordinate(x, y);
    checkEqualXY(expectedCenter, centerPt, 2 * tolerance);
    
    LineString radiusLine = mic.getRadiusLine();
    double actualRadius = radiusLine.getLength();
    assertEquals("Radius: ", expectedRadius, actualRadius, 2 * tolerance);
    
    checkEqualXY("Radius line center point: ", centerPt, radiusLine.getCoordinateN(0));
    Coordinate radiusPt = mic.getRadiusPoint().getCoordinate();
    checkEqualXY("Radius line endpoint point: ", radiusPt, radiusLine.getCoordinateN(1));

  }
}
