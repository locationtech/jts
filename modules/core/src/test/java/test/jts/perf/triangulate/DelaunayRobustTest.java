/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Martin Davis
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Martin Davis BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package test.jts.perf.triangulate;

import java.util.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.*;
import org.locationtech.jts.util.*;

/**
 * Test robustness of Delaunay computation.
 * Test dataset is constructed to have many points
 * with a large base offset.  This reduces 
 * the precision available for the arithmetic in the inCircle test.
 * This causes incorrect values to be computed by using
 * the simple double-precision approach.
 * 
 * @author Martin Davis
 *
 */
public class DelaunayRobustTest 
{
  public static void main(String args[]) {
  	DelaunayRobustTest test = new DelaunayRobustTest();
  	test.run();
  }
  
	public void run()
	{
		run(100000);
	}
	
	final static GeometryFactory geomFact = new GeometryFactory();
	
  final static double SIDE_LEN = 1.0;
  final static double BASE_OFFSET = 1.0e7;
	
	public void run(int nPts)
	{
    System.out.println("Base offset: " + BASE_OFFSET);
    
    
		List pts = randomPointsInGrid(nPts, BASE_OFFSET, BASE_OFFSET);
		System.out.println("# pts: " + pts.size());
		Stopwatch sw = new Stopwatch();
		DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
		builder.setSites(pts);
		
//		Geometry g = builder.getEdges(geomFact);
		// don't actually form output geometry, to save time and memory
		builder.getSubdivision();
		
		System.out.println("  --  Time: " + sw.getTimeString()
				+ "  Mem: " + Memory.usedTotalString());
//		System.out.println(g);
	}
	
	List randomPointsInGrid(int nPts, double basex, double basey)
	{
		List pts = new ArrayList();
		
		int nSide = (int) Math.sqrt(nPts) + 1;
		
		for (int i = 0; i < nSide; i++) {
			for (int j = 0; j < nSide; j++) {
				double x = basex + i * SIDE_LEN + SIDE_LEN * Math.random();
				double y = basey + j * SIDE_LEN + SIDE_LEN * Math.random();
				pts.add(new Coordinate(x, y));
			}
		}
		return pts;
	}
	
	List randomPoints(int nPts)
	{
		List pts = new ArrayList();
		
		for (int i = 0; i < nPts; i++) {
				double x = SIDE_LEN * Math.random();
				double y = SIDE_LEN * Math.random();
				pts.add(new Coordinate(x, y));
		}
		return pts;
	}
}
