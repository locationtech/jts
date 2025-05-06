package org.locationtech.jts.algorithm.construct;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class MaximumInscribedCircleTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(MaximumInscribedCircleTest.class);
  }

  public MaximumInscribedCircleTest(String name) { super(name); }
  
  public void testTriangleRight() {
    checkCircle("POLYGON ((1 1, 1 7, 9 1, 1 1))", 
       0.001, 3.0, 3.0, 2.0 );
  }

  public void testTriangleObtuse() {
    checkCircle("POLYGON ((1 1, 1 9, 2 2, 1 1))", 
       0.001, 1.4852813742385702, 2.17157287525381, 0.4852813742385702 );
  }
  
  public void testSquare() {
    checkCircle("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))", 
       0.001, 150, 150, 50 );
  }

  public void testThinQuad() {
    checkCircle("POLYGON ((1 2, 9 3, 9 1, 1 1, 1 2))", 
       0.001, 8.06225774829855, 1.9377422517014502, 0.937742251701450 );
  }

  public void testDiamond() {
    checkCircle("POLYGON ((150 250, 50 150, 150 50, 250 150, 150 250))", 
       0.001, 150, 150, 70.71 );
  }

  public void testChevron() {
    checkCircle("POLYGON ((1 1, 6 9, 3.7 2.5, 9 1, 1 1))", 
       0.001, 2.82, 2.008, 1.008 );
  }

  public void testChevronFat() {
    checkCircle("POLYGON ((1 1, 6 9, 5.9 5, 9 1, 1 1))", 
       0.001, 4.7545, 3.0809, 2.081 );
  }

  public void testCircle() {
    Geometry centre = read("POINT (100 100)");
    Geometry circle = centre.buffer(100, 20);
    // MIC radius is less than 100 because buffer boundary segments lie inside circle
    checkCircle(circle, 0.01, 100, 100, 99.92);
  }

  public void testKite() {
    checkCircle("POLYGON ((100 0, 200 200, 300 200, 300 100, 100 0))", 
       0.01, 238.19660112501052, 138.19660112501052, 61.803398874989476 );
  }

  public void testKiteWithHole() {
    String wkt = "POLYGON ((100 0, 200 200, 300 200, 300 100, 100 0), (200 150, 200 100, 260 100, 200 150))";
    checkCircle(wkt, 0.01, 257.47, 157.47, 42.529 );
    checkCircleAutoTol(wkt, 0.001, 257.47, 157.47, 42.529 );
  }

  public void testDoubleKite() {
    String wkt = "MULTIPOLYGON (((150 200, 100 150, 150 100, 250 150, 150 200)), ((400 250, 300 150, 400 50, 560 150, 400 250)))";
    checkCircle(wkt, 0.01, 411.38, 149.99, 78.75 );
    checkCircleAutoTol(wkt, 0.001, 411.392, 149.971, 78.7378 );
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
   * Invalid triangle polygon collapsed to a point
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
  
  public void testQuadWithCollinearVertex() {
    checkCircle("POLYGON ((1 5, 5 5, 9 5, 5 1, 1 5))", 
       0.001, 5.0, 3.34314575050762, 1.6568542494923801 );
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
  
  private void checkCircleAutoTol(String wkt, double tolerance, 
      double x, double y, double expectedRadius) {
    checkCircleAutoTol(read(wkt), tolerance, x, y, expectedRadius);
  }
  
  private void checkCircle(Geometry geom, double tolerance, 
      double x, double y, double expectedRadius) {
    MaximumInscribedCircle mic = new MaximumInscribedCircle(geom, tolerance); 
    checkMIC(mic, tolerance, x, y, expectedRadius);
  }

  private void checkCircleAutoTol(Geometry geom, double tolerance, 
      double x, double y, double expectedRadius) {
    MaximumInscribedCircle mic = new MaximumInscribedCircle(geom); 
    checkMIC(mic, tolerance, x, y, expectedRadius);
  }

  private void checkMIC(MaximumInscribedCircle mic, double tolerance, double x, double y, double expectedRadius) {
    Geometry centerPoint = mic.getCenter();
    LineString radiusLine = mic.getRadiusLine();
    Coordinate radiusPt = mic.getRadiusPoint().getCoordinate();
    
    Coordinate centerPt = centerPoint.getCoordinate();
    Coordinate expectedCenter = new Coordinate(x, y);
    checkEqualXY(expectedCenter, centerPt, 2 * tolerance);
    
    double actualRadius = radiusLine.getLength();
    assertEquals("Radius: ", expectedRadius, actualRadius, 2 * tolerance);
    
    checkEqualXY("Radius line center point: ", centerPt, radiusLine.getCoordinateN(0));
    checkEqualXY("Radius line endpoint point: ", radiusPt, radiusLine.getCoordinateN(1));
  }
}
