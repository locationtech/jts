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

import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.util.Stopwatch;


public class PreparedLineIntersectsPerfTest 
{
  static final int MAX_ITER = 1;
  
  static final int NUM_AOI_PTS = 2000;
  static final int NUM_LINES = 50000;
  static final int NUM_LINE_PTS = 10;
  
  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);

  TestDataBuilder builder = new TestDataBuilder();
  
  Stopwatch sw = new Stopwatch();

  public static void main(String[] args) {
  	PreparedLineIntersectsPerfTest test = new PreparedLineIntersectsPerfTest();
    test.test();
  }

  boolean testFailed = false;

  public PreparedLineIntersectsPerfTest() {
  }

  public void test()
  {
    test(5);
    test(10);
    test(500);
    test(1000);
    test(2000);
    /*
    test(100);
    test(1000);
    test(2000);
    test(4000);
    test(8000);
    */
  }
  
  public void test(int nPts)
  {
  	builder.setTestDimension(1);
  	Geometry target = builder.createSineStar(nPts).getBoundary();
  	
    List lines = builder.createTestGeoms(target.getEnvelopeInternal(), 
    		NUM_LINES, 1.0, NUM_LINE_PTS);
    
    System.out.println();
    //System.out.println("Running with " + nPts + " points");
    test(target, lines);
  }

  public void test(Geometry g, List lines)
  {
    System.out.println("AOI # pts: " + g.getNumPoints()
    		+ "      # lines: " + lines.size()
    		+ "   # pts in line: " + NUM_LINE_PTS
    		);

    Stopwatch sw = new Stopwatch();
    int count = 0;
    for (int i = 0; i < MAX_ITER; i++) 
    {
    
    	
//    	count = testPrepGeomNotCached(g, lines);
   	count = testPrepGeomCached(g, lines);
//    	count = testOriginal(g, lines);
   	
   	
    }
    System.out.println("Count of intersections = " + count);
    System.out.println("Finished in " + sw.getTimeString());
  }

  public int testOriginal(Geometry g, List lines)
  { 
  	System.out.println("Using orginal JTS algorithm");
  	int count = 0;
  	for (Iterator i = lines.iterator(); i.hasNext(); ) {
  		LineString line = (LineString) i.next();
  		if (g.intersects(line))
  			count++;
  	}
  	return count;
  }
  
  public int testPrepGeomCached(Geometry g, List lines)
  { 
  	System.out.println("Using cached Prepared Geometry");
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
  public int testPrepGeomNotCached(Geometry g, List lines)
  { 
  	System.out.println("Using NON-CACHED Prepared Geometry");
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
