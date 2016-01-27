/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jts.geom.prep;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.SineStarFactory;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.util.GeometricShapeFactory;

/**
 * Stress tests {@link PreparedPolygon#intersects(Geometry)}
 * to confirm it finds intersections correctly.
 * 
 * @author Martin Davis
 *
 */
public class PreparedPolygonIntersectsStressTest extends TestCase
{
  static final int MAX_ITER = 10000;

  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);
  static WKTWriter wktWriter = new WKTWriter();

  public static void main(String args[]) {
    TestRunner.run(PreparedPolygonIntersectsStressTest.class);
  }

  boolean testFailed = false;

  public PreparedPolygonIntersectsStressTest(String name) {
    super(name);
  }

  public void test()
  {
    run(1000);
  }
  
  public void run(int nPts)
  {
//  	Geometry poly = createCircle(new Coordinate(0, 0), 100, nPts);
  	Geometry poly = createSineStar(new Coordinate(0, 0), 100, nPts);
  	System.out.println(poly);
  	
    System.out.println();
    //System.out.println("Running with " + nPts + " points");
    test(poly);
  }

  Geometry createCircle(Coordinate origin, double size, int nPts) {
		GeometricShapeFactory gsf = new GeometricShapeFactory();
		gsf.setCentre(origin);
		gsf.setSize(size);
		gsf.setNumPoints(nPts);
		Geometry circle = gsf.createCircle();
		// Polygon gRect = gsf.createRectangle();
		// Geometry g = gRect.getExteriorRing();
		return circle;
	}
  
  Geometry createSineStar(Coordinate origin, double size, int nPts) {
		SineStarFactory gsf = new SineStarFactory();
		gsf.setCentre(origin);
		gsf.setSize(size);
		gsf.setNumPoints(nPts);
		gsf.setArmLengthRatio(0.1);
		gsf.setNumArms(20);
		Geometry poly = gsf.createSineStar();
		return poly;
	}
  
  LineString createTestLine(Envelope env, double size, int nPts)
  {
  	double width = env.getWidth();
  	double xOffset = width * Math.random();
  	double yOffset = env.getHeight() * Math.random();
    Coordinate basePt = new Coordinate(
    				env.getMinX() + xOffset,
    				env.getMinY() + yOffset);
    LineString line = createTestLine(basePt, size, nPts);
    return line;
  }
  
  LineString createTestLine(Coordinate base, double size, int nPts)
  {
    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    Geometry circle = gsf.createCircle();
//    System.out.println(circle);
    return (LineString) circle.getBoundary();
  }
  
  public void test(Geometry g) {
  	int count = 0;
  	while (count < MAX_ITER) {
  		count++;
  		LineString line = createTestLine(g.getEnvelopeInternal(), 10, 20);
      
//      System.out.println("Test # " + count);
//  		System.out.println(line);
  		testResultsEqual(g, line);
  	}
	}
  
  public void testResultsEqual(Geometry g, LineString line) 
  {
		boolean slowIntersects = g.intersects(line);

    PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
    PreparedGeometry prepGeom = pgFact.create(g);
    
		boolean fastIntersects = prepGeom.intersects(line);

		if (slowIntersects != fastIntersects) {
			System.out.println(line);
			System.out.println("Slow = " + slowIntersects + ", Fast = " + fastIntersects);
			throw new RuntimeException("Different results found for intersects() !");
		}
	}  
}
