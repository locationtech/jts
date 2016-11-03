
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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * @version 1.7
 */
public class MinimumBoundingCircleTest extends TestCase {

  private PrecisionModel precisionModel = new PrecisionModel(1);
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(MinimumBoundingCircleTest.class);
  }

  public MinimumBoundingCircleTest(String name) { super(name); }

  public void testEmptyPoint() throws Exception {
  	doMinimumBoundingCircleTest("POINT EMPTY", "MULTIPOINT EMPTY");
  }

  public void testPoint() throws Exception {
  	doMinimumBoundingCircleTest("POINT (10 10)", "POINT (10 10)", new Coordinate(10, 10), 0);
  }

  public void testPoints2() throws Exception {
  	doMinimumBoundingCircleTest("MULTIPOINT ((10 10), (20 20))", "MULTIPOINT ((10 10), (20 20))", new Coordinate(15, 15), 7.0710678118654755);
  }

  public void testPointsInLine() throws Exception {
  	doMinimumBoundingCircleTest("MULTIPOINT ((10 10), (20 20), (30 30))", "MULTIPOINT ((10 10), (30 30))",
  			new Coordinate(20, 20), 14.142135623730951);
  }

  public void testPoints3() throws Exception {
  	doMinimumBoundingCircleTest("MULTIPOINT ((10 10), (20 20), (10 20))", "MULTIPOINT ((10 10), (20 20), (10 20))",
  			new Coordinate(15, 15), 7.0710678118654755);
  }

  public void testObtuseTriangle() throws Exception {
    doMinimumBoundingCircleTest("POLYGON ((100 100, 200 100, 150 90, 100 100))", "MULTIPOINT ((100 100), (200 100))",
        new Coordinate(150, 100), 50);
  }

  public void testTriangleWithMiddlePoint() throws Exception {
    doMinimumBoundingCircleTest("MULTIPOINT ((10 10), (20 20), (10 20), (15 19))", "MULTIPOINT ((10 10), (20 20), (10 20))",
        new Coordinate(15, 15), 7.0710678118654755);
  }

  public void testQuadrilateral() throws Exception {
    doMinimumBoundingCircleTest("POLYGON ((26426 65078, 26531 65242, 26096 65427, 26075 65136, 26426 65078))", "MULTIPOINT ((26531 65242), (26075 65136), (26096 65427))",
        new Coordinate(26284.84180271327, 65267.114509082545), 247.4360455914027 );
  }

  static final double TOLERANCE = 1.0e-5;
  
  private void doMinimumBoundingCircleTest(String wkt, String expectedWKT) throws ParseException 
  {
  	doMinimumBoundingCircleTest(wkt, expectedWKT, null, -1);
  }
  
  private void doMinimumBoundingCircleTest(String wkt, String expectedWKT,
  		Coordinate expectedCentre, double expectedRadius)
  throws ParseException 
  {
  	MinimumBoundingCircle mbc = new MinimumBoundingCircle(reader.read(wkt));
  	Coordinate[] exPts = mbc.getExtremalPoints();
  	Geometry actual = geometryFactory.createMultiPoint(exPts);
  	double actualRadius = mbc.getRadius();
  	Coordinate actualCentre = mbc.getCentre();
  	//System.out.println( 
  	//		"   Centre = " + actualCentre
  	//		+ "   Radius = " + actualRadius);

  	Geometry expected = reader.read(expectedWKT);
  	boolean isEqual = actual.equals(expected);
  	// need this hack because apparently equals does not work for MULTIPOINT EMPTY
  	if (actual.isEmpty() && expected.isEmpty())
  		isEqual = true;
  	if (!isEqual) {
  	  System.out.println("Actual = " + actual + ", Expected = " + expected);
  	}
  	assertTrue(isEqual);
  	
  	if (expectedCentre != null) {
  		assertTrue(expectedCentre.distance(actualCentre) < TOLERANCE);
  	}
  	if (expectedRadius >= 0) {
  		assertTrue(Math.abs(expectedRadius - actualRadius) < TOLERANCE);
  	}
  }
  

}
