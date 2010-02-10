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
package test.jts.junit.geom.prep;

import junit.framework.TestCase;
import junit.textui.TestRunner;


import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.prep.*;
import com.vividsolutions.jts.geom.util.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import com.vividsolutions.jts.util.Stopwatch;

/**
 * Stress tests fast intersector to confirm it find intersections correctly.
 * 
 * @author Owner
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
