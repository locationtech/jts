/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.algorithm;

import org.locationtech.jts.algorithm.MinimumBoundingCircle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.util.Assert;

public class MinimumBoundingCircleStressTest 
{
	GeometryFactory geomFact = new GeometryFactory();
	
  public static void main(String args[]) {
  	try {
  		(new MinimumBoundingCircleStressTest()).run();
  	}
  	catch (Exception ex) {
  		ex.printStackTrace();
  	}
  
  }

  public MinimumBoundingCircleStressTest()
  {
    
  }
  
  void run()
  {
  	while (true) {
  		int n = (int) ( 10000 * Math.random());
  		run(n);
  	}
  }
  
  void run(int nPts)
  {
  	Coordinate[] randPts = createRandomPoints(nPts);
  	Geometry mp = geomFact.createMultiPoint(randPts);
  	MinimumBoundingCircle mbc = new MinimumBoundingCircle(mp);
  	Coordinate centre = mbc.getCentre();
  	double radius = mbc.getRadius();
   	System.out.println("Testing " + nPts + " random points.  Radius = " + radius);
  	
  	checkWithinCircle(randPts, centre, radius, 0.0001);
  }
  
  void checkWithinCircle(Coordinate[] pts, Coordinate centre, double radius, double tolerance)
  {
  	for (int i = 0; i < pts.length; i++ ) {
  		Coordinate p = pts[i];
  		double ptRadius = centre.distance(p);
  		double error = ptRadius - radius;
  		if (error > tolerance) {
  			Assert.shouldNeverReachHere();
  		}
  	}
  }
  Coordinate[] createRandomPoints(int n)
  {
  	Coordinate[] pts = new Coordinate[n];
  	for(int i = 0; i < n; i++) {
  		double x = 100 * Math.random();
  		double y = 100 * Math.random();
  		pts[i] = new Coordinate(x, y);
  	}
  	return pts;
  }
}
