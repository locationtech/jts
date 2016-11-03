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
package test.jts.perf.geom.prep;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.locationtech.jts.util.Stopwatch;


public class PreparedPolygonIntersectsPerfTest 
{
  static final int MAX_ITER = 10;
  
  static final int NUM_AOI_PTS = 2000;
  static final int NUM_LINES = 10000;
  static final int NUM_LINE_PTS = 100;
  
  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);
  static WKTWriter wktWriter = new WKTWriter();

  Stopwatch sw = new Stopwatch();

  public static void main(String[] args) {
  	PreparedPolygonIntersectsPerfTest test = new PreparedPolygonIntersectsPerfTest();
    test.test();
  }

  boolean testFailed = false;

  public PreparedPolygonIntersectsPerfTest() {
  }

  public void test()
  {
    test(5);
    test(10);
    test(500);
    test(1000);
    test(2000);
    test(4000);
    /*
    test(4000);
    test(8000);
    */
  }
  
  public void test(int nPts)
  {
//  	Geometry poly = createCircle(new Coordinate(0, 0), 100, nPts);
  	Geometry sinePoly = createSineStar(new Coordinate(0, 0), 100, nPts);
//  	System.out.println(poly);
//  	Geometry target = sinePoly.getBoundary();
  	Geometry target = sinePoly;
  	
    List lines = createLines(target.getEnvelopeInternal(), NUM_LINES, 1.0, NUM_LINE_PTS);
    
    System.out.println();
    //System.out.println("Running with " + nPts + " points");
    test(target, lines);
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
		gsf.setNumArms(50);
		Geometry poly = gsf.createSineStar();
		return poly;
	}
  
  List createLines(Envelope env, int nItems, double size, int nPts)
  {
    int nCells = (int) Math.sqrt(nItems);

  	List geoms = new ArrayList();
  	double width = env.getWidth();
  	double xInc = width / nCells;
  	double yInc = width / nCells;
  	for (int i = 0; i < nCells; i++) {
    	for (int j = 0; j < nCells; j++) {
    		Coordinate base = new Coordinate(
    				env.getMinX() + i * xInc,
    				env.getMinY() + j * yInc);
    		Geometry line = createLine(base, size, nPts);
    		geoms.add(line);
    	}
  	}
  	return geoms;
  }
  
  Geometry createLine(Coordinate base, double size, int nPts)
  {
    SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    Geometry circle = gsf.createSineStar();
//    System.out.println(circle);
    return circle.getBoundary();
  }
  
  public void test(Geometry g, List lines)
  {
    System.out.println("AOI # pts: " + g.getNumPoints()
    		+ "      # lines: " + lines.size()
    		+ "   # pts in line: " + NUM_LINE_PTS
    		);

    Stopwatch sw = new Stopwatch();
    int count = 0;
    for (int i = 0; i < MAX_ITER; i++) {
//    	count = testPrepGeomNotCached(i, g, lines);
   	count = testPrepGeomCached(i, g, lines);
//    	count = testOriginal(i, g, lines);
    }
    System.out.println("Count of intersections = " + count);
    System.out.println("Finished in " + sw.getTimeString());
  }


  public int testOriginal(int iter, Geometry g, List lines)
  { 
	  if (iter == 0) System.out.println("Using orginal JTS algorithm");
  	int count = 0;
  	for (Iterator i = lines.iterator(); i.hasNext(); ) {
  		LineString line = (LineString) i.next();
  		if (g.intersects(line))
  			count++;
  	}
  	return count;
  }
  
  public int testPrepGeomCached(int iter, Geometry g, List lines)
  { 
	  if (iter == 0) System.out.println("Using cached Prepared Geometry");
   PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
    PreparedGeometry prepGeom = pgFact.create(g);
    
  	int count = 0;
  	for (Iterator i = lines.iterator(); i.hasNext(); ) {
  		LineString line = (LineString) i.next();
  		
  		if (prepGeom.intersects(line))
  			count++;
  	}
  	return count;
  }
  
  /**
   * Tests using PreparedGeometry, but creating a new
   * PreparedGeometry object each time.
   * This tests whether there is a penalty for using 
   * the PG algorithm as a complete replacement for 
   * the original algorithm.
   *  
   * @param g
   * @param lines
   * @return the count
   */
  public int testPrepGeomNotCached(int iter, Geometry g, List lines)
  { 
  	if (iter == 0) System.out.println("Using NON-CACHED Prepared Geometry");
    PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
//    PreparedGeometry prepGeom = pgFact.create(g);
    
  	int count = 0;
  	for (Iterator i = lines.iterator(); i.hasNext(); ) {
  		LineString line = (LineString) i.next();
  		
  		// test performance of creating the prepared geometry each time
      PreparedGeometry prepGeom = pgFact.create(g);
      
  		if (prepGeom.intersects(line))
  			count++;
  	}
  	return count;
  }
  
}
