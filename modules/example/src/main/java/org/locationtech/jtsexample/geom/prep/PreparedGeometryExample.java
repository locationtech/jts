
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
package org.locationtech.jtsexample.geom.prep;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.*;


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
