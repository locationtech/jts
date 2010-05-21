
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
package com.vividsolutions.jtsexample.geom.prep;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.prep.*;


/**
 * Shows use of {@link PreparedGeometry} in a batch (repeated) operation.
 * 
 * The example uses a Monte Carlo method to approximate the value of Pi.
 * Given a circle inscribed in a square and a large number of random points
 * in the square, the number of points which intersect the circle approximates Pi/4.  
 * This involves repeated Point-In-Polygon tests, which is one of the 
 * geometry tests optimized by the PreparedGeometry implementation for polygons.
 *
 * @version 1.7
 */
public class PreparedGeometryExample
{
	static GeometryFactory geomFact = new GeometryFactory();
	
	static final int MAX_ITER = 100000; 
		
  public static void main(String[] args)
      throws Exception
  {
  	Geometry circle = createCircle();
  	PreparedGeometry prepCircle = PreparedGeometryFactory.prepare(circle);
  	
  	int count = 0;
  	int inCount = 0;
  	for (int i = 0; i < MAX_ITER; i++) 
  	{
  		count++;
  		Point randPt = createRandomPoint();
  		if (prepCircle.intersects(randPt)) {
  			inCount++;
  		}
  		
  		//System.out.println("Approximation to PI: " + (4.0 * inCount / (double) count));
  	}
  	double approxPi = 4.0 * inCount / (double) count;
  	double approxDiffPct = 1.0 - approxPi/Math.PI;
  	
		System.out.println("Approximation to PI: " + approxPi
				+ "  ( % difference from actual = " + 100 * approxDiffPct + " )"
				); 

  }
  
  static Geometry createCircle()
  {
  	Geometry centrePt = geomFact.createPoint(new Coordinate(0.5, 0.5));
  	return centrePt.buffer(0.5, 20);
  }
  
  static Point createRandomPoint()
  {
  	return geomFact.createPoint(new Coordinate(Math.random(), Math.random()));
  }
}
