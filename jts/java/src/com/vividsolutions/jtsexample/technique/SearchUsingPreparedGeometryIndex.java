
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
package com.vividsolutions.jtsexample.technique;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.prep.*;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.util.Stopwatch;


/**
 * Demonstrates use of {@link PreparedGeometry}s in a spatial index
 * to optimize spatial search.
 * 
 * The example creates a grid of circular polygons, packed into the 1 x 1 square.
 * This set of polygons is spatially indexed as PreparedGeometrys in an STRtree index.
 * A series of random points in the square is generated, and the index
 * is used to determine whether each point intersects any circles.
 * The fraction of points which intersect will approximate the 
 * fraction of area covered by the circles.
 * 
 * @version 1.12
 */
public class SearchUsingPreparedGeometryIndex
{
	static GeometryFactory geomFact = new GeometryFactory();
	
	static final int MAX_ITER = 200000; 
	static final int GRID_SIZE = 10; 
	static final int POLYGON_SIZE = 100;

  public static void main(String[] args)
      throws Exception
  {
  	List circleGrid = createCircleGrid(GRID_SIZE);
  	
  	PreparedGeometryIndex pgIndex = new PreparedGeometryIndex();
  	pgIndex.insert(circleGrid);
  	
  	Stopwatch sw = new Stopwatch();
  	int inCount = runIndexedQuery(pgIndex);
  	String indexTime = sw.getTimeString();
  	
		System.out.println("Number of iterations       = " + MAX_ITER ); 
		System.out.println("Number of circles in grid  = " + circleGrid.size() ); 
		System.out.println();
  	System.out.println("The fraction of intersecting points should approximate the total area of the circles:");
  	System.out.println();
		System.out.println("Area of circles                = " + area(circleGrid) ); 
		System.out.println("Fraction of points in circles  = " + inCount / (double) MAX_ITER ); 
		System.out.println(); 
		System.out.println("Indexed Execution time: " + indexTime ); 
		
		/**
		 * For comparison purposes run the same query without using an index
		 */ 
  	Stopwatch sw2 = new Stopwatch();
  	int inCount2 = runBruteForceQuery(circleGrid);
  	String bruteForceTime = sw2.getTimeString();
  	
		System.out.println(); 
		System.out.println("Execution time: " + bruteForceTime ); 

  }
  
  static int runIndexedQuery(PreparedGeometryIndex pgIndex)
  {
  	int inCount = 0;
  	for (int i = 0; i < MAX_ITER; i++) 
  	{
  		Point randPt = createRandomPoint();
  		if (pgIndex.intersects(randPt).size() > 0) {
				inCount++;
  		}
  	}
  	return inCount;
  }
  
  static int runBruteForceQuery(Collection geoms)
  {
  	int inCount = 0;
  	for (int i = 0; i < MAX_ITER; i++) 
  	{
  		Point randPt = createRandomPoint();
  		if (findIntersecting(geoms, randPt).size() > 0) {
				inCount++;
  		}
  	}
  	return inCount;
  }
  
  static double area(Collection geoms)
  {
  	double area = 0.0;
		for (Iterator i = geoms.iterator(); i.hasNext(); ) {
			Geometry geom = (Geometry) i.next();
			area += geom.getArea();
		}
		return area;
  }
  
  static List createCircleGrid(int gridSize)
  {
  	double diameter = 1.0 / gridSize;
  	double radius = diameter / 2;
  	
  	List circles = new ArrayList();
  	for (int i = 0; i < gridSize; i++) {
    	for (int j = 0; j < gridSize; j++) {
    		Coordinate centre = new Coordinate(radius + i * diameter, radius + j * diameter);
    		Geometry circle = createCircle(centre, radius);
    		circles.add(circle);
    	}
  	}
  	return circles;
  }
  
  static Geometry createCircle(Coordinate centre, double radius)
  {
  	Geometry centrePt = geomFact.createPoint(centre);
  	return centrePt.buffer(radius, POLYGON_SIZE);
  }

  static Point createRandomPoint()
  {
  	return geomFact.createPoint(new Coordinate(Math.random(), Math.random()));
  }
   
  static List findIntersecting(Collection targetGeoms, Geometry queryGeom)
  {
		List result = new ArrayList();
		for (Iterator it = targetGeoms.iterator(); it.hasNext(); ) {
			Geometry test = (Geometry) it.next();
			if (test.intersects(queryGeom)) {
				result.add(test);
			}
		}
  	return result;
  }
}

/**
 * A spatial index which indexes {@link PreparedGeometry}s 
 * created from a set of {@link Geometry}s.
 * This can be used for efficient testing
 * for intersection with a series of target geomtries. 
 *  
 * @author Martin Davis
 *
 */
class PreparedGeometryIndex
{
	private SpatialIndex index = new STRtree();
	
	/**
	 * Creates a new index
	 *
	 */
	public PreparedGeometryIndex()
	{
		
	}
	
	/**
	 * Inserts a collection of Geometrys into the index.
	 * 
	 * @param geoms a collection of Geometrys to insert
	 */
	public void insert(Collection geoms)
	{
		for (Iterator i = geoms.iterator(); i.hasNext(); ) {
			Geometry geom = (Geometry) i.next();
			index.insert(geom.getEnvelopeInternal(), PreparedGeometryFactory.prepare(geom));
		}
	}
	
	/**
	 * Finds all {@link PreparedGeometry}s which might 
	 * interact with a query {@link Geometry}.
	 * 
	 * @param g the geometry to query by
	 * @return a list of candidate PreparedGeometrys
	 */
	public List query(Geometry g)
	{
		return index.query(g.getEnvelopeInternal());
	}
	
	/**
	 * Finds all {@link PreparedGeometry}s which intersect a given {@link Geometry}
	 * 
	 * @param g the geometry to query by
	 * @return a list of intersecting PreparedGeometrys 
	 */
	public List intersects(Geometry g)
	{
		List result = new ArrayList();
		List candidates = query(g);
		for (Iterator it = candidates.iterator(); it.hasNext(); ) {
			PreparedGeometry prepGeom = (PreparedGeometry) it.next();
			if (prepGeom.intersects(g)) {
				result.add(prepGeom);
			}
		}
		return result;
	}
}
