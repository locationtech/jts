package test.jts.perf.algorithm;

import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.*;

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
