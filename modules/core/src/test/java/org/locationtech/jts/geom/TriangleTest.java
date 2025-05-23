/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;


/**
 * @version 1.7
 */
public class TriangleTest extends GeometryTestCase
{

  private PrecisionModel precisionModel = new PrecisionModel();

  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel,
      0);

  WKTReader reader = new WKTReader(geometryFactory);

  private static final double TOLERANCE = 1E-5;

  public static void main(String args[])
  {
    TestRunner.run(TriangleTest.class);
  }

  public TriangleTest(String name)
  {
    super(name);
  }

  public void testInterpolateZ() throws Exception
  {
    checkInterpolateZ("LINESTRING(1 1 0, 2 1 0, 1 2 10)", new Coordinate(1.5,
        1.5), 5);
    checkInterpolateZ("LINESTRING(1 1 0, 2 1 0, 1 2 10)", new Coordinate(1.2,
        1.2), 2);
    checkInterpolateZ("LINESTRING(1 1 0, 2 1 0, 1 2 10)", new Coordinate(0, 0),
        -10);
  }

  public void checkInterpolateZ(String wkt, Coordinate p, double expectedValue)
      throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();

    Triangle t = new Triangle(pt[0], pt[1], pt[2]);
    double z = t.interpolateZ(p);
    //System.out.println("Z = " + z);
    assertEquals(expectedValue, z, 0.000001);
  }

  public void testArea3D() throws Exception
  {
    checkArea3D("POLYGON((0 0 10, 100 0 110, 100 100 110, 0 0 10))",
        7071.067811865475);
    checkArea3D("POLYGON((0 0 10, 100 0 10, 50 100 110, 0 0 10))",
        7071.067811865475);
  }

  public void checkArea3D(String wkt, double expectedValue) throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();
    Triangle t = new Triangle(pt[0], pt[1], pt[2]);
    double area3D = t.area3D();
    // System.out.println("area3D = " + area3D);
    assertEquals(expectedValue, area3D, TOLERANCE);
  }

  public void testArea() throws Exception
  {
    // CW
    checkArea("POLYGON((10 10, 20 20, 20 10, 10 10))", 50);
    // CCW
    checkArea("POLYGON((10 10, 20 10, 20 20, 10 10))", -50);
    // degenerate point triangle
    checkArea("POLYGON((10 10, 10 10, 10 10, 10 10))", 0);
    // degenerate line triangle
    checkArea("POLYGON((10 10, 20 10, 15 10, 10 10))", 0);
  }

  public void checkArea(String wkt, double expectedValue) throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();

    Triangle t = new Triangle(pt[0], pt[1], pt[2]);
    double signedArea = t.signedArea();
    //System.out.println("signed area = " + signedArea);
    assertEquals(expectedValue, signedArea, TOLERANCE);

    double area = t.area();
    assertEquals(Math.abs(expectedValue), area, TOLERANCE);

  }

  public void testAcute() throws Exception
  {
    // right triangle
    checkAcute("POLYGON((10 10, 20 20, 20 10, 10 10))", false);
    // CCW right tri
    checkAcute("POLYGON((10 10, 20 10, 20 20, 10 10))", false);
    // acute
    checkAcute("POLYGON((10 10, 20 10, 15 20, 10 10))", true);
  }

  public void checkAcute(String wkt, boolean expectedValue) throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();

    Triangle t = new Triangle(pt[0], pt[1], pt[2]);
    boolean isAcute = t.isAcute();
    //System.out.println("isAcute = " + isAcute);
    assertEquals(expectedValue, isAcute);
  }

  public void testCircumCentre() throws Exception
  {
    // right triangle
    checkCircumCentre("POLYGON((10 10, 20 20, 20 10, 10 10))", new Coordinate(
        15.0, 15.0));
    // CCW right tri
    checkCircumCentre("POLYGON((10 10, 20 10, 20 20, 10 10))", new Coordinate(
        15.0, 15.0));
    // acute
    checkCircumCentre("POLYGON((10 10, 20 10, 15 20, 10 10))", new Coordinate(
        15.0, 13.75));
  }

  public void testCircumradius() throws Exception
  {
    // right triangle
    checkCircumradius("POLYGON((10 10, 20 20, 20 10, 10 10))");
    // CCW right tri
    checkCircumradius("POLYGON((10 10, 20 10, 20 20, 10 10))");
    // acute
    checkCircumradius("POLYGON((10 10, 20 10, 15 20, 10 10))");
  }

  public void testCentroid() throws Exception
  {
    // right triangle
    checkCentroid("POLYGON((10 10, 20 20, 20 10, 10 10))", new Coordinate(
        (10.0 + 20.0 + 20.0) / 3.0, (10.0 + 20.0 + 10.0) / 3.0));
    // CCW right tri
    checkCentroid("POLYGON((10 10, 20 10, 20 20, 10 10))", new Coordinate(
        (10.0 + 20.0 + 20.0) / 3.0, (10.0 + 10.0 + 20.0) / 3.0));
    // acute
    checkCentroid("POLYGON((10 10, 20 10, 15 20, 10 10))", new Coordinate(
        (10.0 + 20.0 + 15.0) / 3.0, (10.0 + 10.0 + 20.0) / 3.0));
  }
  
  public void testInCentre() throws Exception
  {
    // right triangle
    checkInCentre("POLYGON((10 10, 20 20, 20 10, 10 10))", 
        new Coordinate(17.071067811865476, 12.928932188134524) );
    // CCW right tri
    checkInCentre("POLYGON((10 10, 20 10, 20 20, 10 10))", 
        new Coordinate(17.071067811865476, 12.928932188134524) );
    // acute
    checkInCentre("POLYGON((10 10, 20 10, 15 20, 10 10))", 
        new Coordinate(14.999999999999998, 13.090169943749475));
    // obtuse
    checkInCentre("POLYGON ((10 10, 20 10, 40 20, 10 10))", 
        new Coordinate(19.63104841334295, 11.56290400148567));
  }
  
  //==========================================================
  
  public void checkCentroid(String wkt, Coordinate expectedValue)
      throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();

    Coordinate centroid = Triangle.centroid(pt[0], pt[1], pt[2]);
    //System.out.println("(Static) centroid = " + centroid);
    assertEquals(expectedValue.toString(), centroid.toString());

    // Test Instance version
    //
    Triangle t = new Triangle(pt[0], pt[1], pt[2]);
    centroid = t.centroid();
    //System.out.println("(Instance) centroid = " + centroid.toString());
    assertEquals(expectedValue.toString(), centroid.toString());
  }

  public void checkInCentre(String wkt, Coordinate expectedValue)
      throws Exception
  {
    double tolerance = 0.00001;
    
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();

    Coordinate centre = Triangle.inCentre(pt[0], pt[1], pt[2]);
    //System.out.println("(Static) centroid = " + centroid);
    checkEqualXY(expectedValue, centre, tolerance);
    checkEquidistantToEdges(centre, pt, tolerance);
    
    // Test Instance version
    //
    Triangle t = new Triangle(pt[0], pt[1], pt[2]);
    Coordinate centreTri = t.inCentre();
    checkEqualXY(expectedValue, centreTri, tolerance);
  }

  private void checkEquidistantToEdges(Coordinate centre, Coordinate[] pt, double tolerance) {
    //-- inCenter must be equidistant from edges
    double radius0 = Distance.pointToLinePerpendicular(centre, pt[0], pt[1]);
    double radius1 = Distance.pointToLinePerpendicular(centre, pt[1], pt[2]);
    double radius2 = Distance.pointToLinePerpendicular(centre, pt[2], pt[3]);
    assertEquals(radius0, radius1, tolerance);
    assertEquals(radius0, radius2, tolerance);
  }
  
  public void checkCircumCentre(String wkt, Coordinate expectedValue)
      throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();

    Coordinate circumcentre = Triangle.circumcentre(pt[0], pt[1], pt[2]);
    //System.out.println("(Static) circumcentre = " + circumcentre);
    assertEquals(expectedValue.toString(), circumcentre.toString());

    // Test Instance version
    //
    Triangle t = new Triangle(pt[0], pt[1], pt[2]);
    circumcentre = t.circumcentre();
    //System.out.println("(Instance) circumcentre = " + circumcentre.toString());
    assertEquals(expectedValue.toString(), circumcentre.toString());
  }

  public void checkCircumradius(String wkt)
      throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();

    Coordinate circumcentre = Triangle.circumcentre(pt[0], pt[1], pt[2]);
    double circumradius = Triangle.circumradius(pt[0], pt[1], pt[2]);
    //System.out.println("(Static) circumcentre = " + circumcentre);
    double rad0 = pt[0].distance(circumcentre);
    double rad1 = pt[1].distance(circumcentre);
    double rad2 = pt[2].distance(circumcentre);
    assertEquals(rad0, circumradius, 0.00001);
    assertEquals(rad1, circumradius, 0.00001);
    assertEquals(rad2, circumradius, 0.00001);
  }
  
  public void testLongestSideLength() throws Exception
  {
    // right triangle
    checkLongestSideLength("POLYGON((10 10 1, 20 20 2, 20 10 3, 10 10 1))",
        14.142135623730951);
    // CCW right tri
    checkLongestSideLength("POLYGON((10 10 1, 20 10 2, 20 20 3, 10 10 1))",
        14.142135623730951);
    // acute
    checkLongestSideLength("POLYGON((10 10 1, 20 10 2, 15 20 3, 10 10 1))",
        11.180339887498949);
  }

  public void checkLongestSideLength(String wkt, double expectedValue)
      throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();

    double length = Triangle.longestSideLength(pt[0], pt[1], pt[2]);
    //System.out.println("(Static) longestSideLength = " + length);
    assertEquals(expectedValue, length, 0.00000001); 

    // Test Instance version
    //
    Triangle t = new Triangle(pt[0], pt[1], pt[2]);
    length = t.longestSideLength();
    //System.out.println("(Instance) longestSideLength = " + length);
    assertEquals(expectedValue, length, 0.00000001);
  }
  
  //===============================================================
  
  public void testIsCCW() {
    checkIsCCW("POLYGON ((30 90, 80 50, 20 20, 30 90))", false);
    checkIsCCW("POLYGON ((90 90, 20 40, 10 10, 90 90))", true);
  }
  
  public void checkIsCCW(String wkt, boolean expectedValue)
  {
    Coordinate[] pt = read(wkt).getCoordinates();
    boolean actual = Triangle.isCCW(pt[0], pt[1], pt[2]);
    assertEquals(expectedValue, actual);
  }

  //===============================================================
  
  public void testIntersects() {
    checkIntersects("POLYGON ((30 90, 80 50, 20 20, 30 90))", "POINT (70 20)", false);
    // triangle vertex
    checkIntersects("POLYGON ((30 90, 80 50, 20 20, 30 90))", "POINT (30 90)", true);
    checkIntersects("POLYGON ((30 90, 80 50, 20 20, 30 90))", "POINT (40 40)", true);
    
    // on an edge
    checkIntersects("POLYGON ((30 90, 70 50, 71.5 16.5, 30 90))", "POINT (50 70)", true);
  }
  
  public void checkIntersects(String wktTri, String wktPt, boolean expectedValue)
  {
    Coordinate[] tri = read(wktTri).getCoordinates();
    Coordinate pt = read(wktPt).getCoordinate();
    
    boolean actual = Triangle.intersects(tri[0], tri[1], tri[2], pt);
    assertEquals(expectedValue, actual);
  }

}
