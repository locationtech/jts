package org.locationtech.jts.algorithm.construct;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class LargestEmptyCircleTest extends GeometryTestCase {
  
  public static void main(String args[]) {
    TestRunner.run(LargestEmptyCircleTest.class);
  }

  public LargestEmptyCircleTest(String name) { super(name); }
  
  public void testPointsSquare() {
    checkCircle("MULTIPOINT ((100 100), (100 200), (200 200), (200 100))", 
       0.01, 150, 150, 70.71 );
  }

  public void testPointsTriangleOnHull() {
    checkCircle("MULTIPOINT ((100 100), (300 100), (150 50))", 
       0.01, 216.66, 99.99, 83.33 );
  }

  public void testPointsTriangleInterior() {
    checkCircle("MULTIPOINT ((100 100), (300 100), (200 250))", 
       0.01, 200.00, 141.66, 108.33 );
  }

  public void testLinesOpenDiamond() {
    checkCircle("MULTILINESTRING ((50 100, 150 50), (250 50, 350 100), (350 150, 250 200), (50 150, 150 200))", 
       0.01, 200, 125, 90.13 );
  }

  public void testLinesCrossed() {
    checkCircle("MULTILINESTRING ((100 100, 300 300), (100 200, 300 0))", 
       0.01, 299.99, 150.00, 106.05 );
  }

  public void testLinesZigzag() {
    checkCircle("MULTILINESTRING ((100 100, 200 150, 100 200, 250 250, 100 300, 300 350, 100 400), (50 400, 0 350, 50 300, 0 250, 50 200, 0 150, 50 100))", 
       0.01, 77.52, 349.99, 54.81 );
  }

  public void testPointsLinesTriangle() {
    checkCircle("GEOMETRYCOLLECTION (LINESTRING (100 100, 300 100), POINT (250 200))", 
       0.01, 196.49, 164.31, 64.31 );
  }

  public void testPoint() {
    checkCircleZeroRadius("POINT (100 100)", 
       0.01 );
  }

  public void testLineFlat() {
    checkCircleZeroRadius("LINESTRING (0 0, 50 50)", 
       0.01 );
  }

  
  private void checkCircle(String wkt, double tolerance, 
      double x, double y, double expectedRadius) {
    checkCircle(read(wkt), tolerance, x, y, expectedRadius);
  }
  
  private void checkCircle(Geometry geom, double tolerance, 
      double x, double y, double expectedRadius) {
    LargestEmptyCircle lec = new LargestEmptyCircle(geom, tolerance); 
    Geometry centerPoint = lec.getCenter();
    Coordinate centerPt = centerPoint.getCoordinate();
    Coordinate expectedCenter = new Coordinate(x, y);
    checkEqualXY(expectedCenter, centerPt, tolerance);
    
    LineString radiusLine = lec.getRadiusLine();
    double actualRadius = radiusLine.getLength();
    assertEquals("Radius: ", expectedRadius, actualRadius, tolerance);
    
    checkEqualXY("Radius line center point: ", centerPt, radiusLine.getCoordinateN(0));
    Coordinate radiusPt = lec.getRadiusPoint().getCoordinate();
    checkEqualXY("Radius line endpoint point: ", radiusPt, radiusLine.getCoordinateN(1));
  }
  
  private void checkCircleZeroRadius(String wkt, double tolerance) {
    checkCircleZeroRadius(read(wkt), tolerance);
  }

  private void checkCircleZeroRadius(Geometry geom, double tolerance) {
    LargestEmptyCircle lec = new LargestEmptyCircle(geom, tolerance); 

    LineString radiusLine = lec.getRadiusLine();
    double actualRadius = radiusLine.getLength();
    assertEquals("Radius: ", 0.0, actualRadius, tolerance);
    
    Coordinate centerPt = lec.getCenter().getCoordinate();
    checkEqualXY("Radius line center point: ", centerPt, radiusLine.getCoordinateN(0));
    Coordinate radiusPt = lec.getRadiusPoint().getCoordinate();
    checkEqualXY("Radius line endpoint point: ", radiusPt, radiusLine.getCoordinateN(1));
  }
}
