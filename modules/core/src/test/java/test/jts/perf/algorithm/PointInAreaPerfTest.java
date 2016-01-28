/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package test.jts.perf.algorithm;


import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.util.Stopwatch;

public class PointInAreaPerfTest  {

  public static void main(String args[]) {
  	PointInAreaPerfTest test = new PointInAreaPerfTest();
  	test.run();
  }

	PrecisionModel pmFixed_1 = new PrecisionModel(1.0);
	
	public PointInAreaPerfTest() {
	}

	public void run()
	{
		GeometryFactory geomFactory = new GeometryFactory();
		
		SineStarFactory ssFact = new SineStarFactory();
		ssFact.setSize(1000.0);
		ssFact.setNumPoints(2000);
		ssFact.setArmLengthRatio(0.1);
		ssFact.setNumArms(100);

		Geometry area = ssFact.createSineStar();
		System.out.println(area);
		
    Stopwatch sw = new Stopwatch();

    PointOnGeometryLocator pia = new MCIndexedPointInAreaLocator(area); 
//    PointInAreaLocator pia = new IntervalIndexedPointInAreaLocator(area); 
//		PointInAreaLocator pia = new SimplePointInAreaLocator(area); 

		PointInAreaPerfTester perfTester = new PointInAreaPerfTester(geomFactory, area);
		perfTester.setNumPoints(50000);
		perfTester.setPIA(pia);
		perfTester.run();
    
    System.out.println("Overall time: " + sw.getTimeString());
	}
}



