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
package org.locationtech.jts.geom.prep;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.GeometricShapeFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;


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
  	//System.out.println(poly);
  	
    //System.out.println();
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
