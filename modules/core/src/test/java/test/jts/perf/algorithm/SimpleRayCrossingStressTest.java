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

import org.locationtech.jts.algorithm.PerturbedGridPolygonBuilder;
import org.locationtech.jts.algorithm.RayCrossingCounter;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import junit.framework.TestCase;
import junit.textui.TestRunner;



public class SimpleRayCrossingStressTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(SimpleRayCrossingStressTest.class);
  }

	PrecisionModel pmFixed_1 = new PrecisionModel(1.0);
	
	public SimpleRayCrossingStressTest(String name) {
		super(name);
	}

	public void testGrid()
	{
		// Use fixed PM to try and get at least some points hitting the boundary
		GeometryFactory geomFactory = new GeometryFactory(pmFixed_1);
//		GeometryFactory geomFactory = new GeometryFactory();
		
		PerturbedGridPolygonBuilder gridBuilder = new PerturbedGridPolygonBuilder(geomFactory);
		gridBuilder.setNumLines(20);
		gridBuilder.setLineWidth(10.0);
		Geometry area = gridBuilder.getGeometry();
		
		SimpleRayCrossingPointInAreaLocator pia = new SimpleRayCrossingPointInAreaLocator(area); 

		PointInAreaStressTester gridTester = new PointInAreaStressTester(geomFactory, area);
		gridTester.setNumPoints(100000);
		gridTester.setPIA(pia);
		
		boolean isCorrect = gridTester.run();
		assertTrue(isCorrect);
	}
	
	static class SimpleRayCrossingPointInAreaLocator
	implements PointOnGeometryLocator
	{
		private Geometry geom;
		
		public SimpleRayCrossingPointInAreaLocator(Geometry geom)
		{
			this.geom = geom;
		}
		
		public int locate(Coordinate p)
		{
			RayCrossingCounter rcc = new RayCrossingCounter(p);
			RayCrossingSegmentFilter filter = new RayCrossingSegmentFilter(rcc);
			geom.apply(filter);
			return rcc.getLocation();
		}
		
		static class RayCrossingSegmentFilter implements CoordinateSequenceFilter
		{
			private RayCrossingCounter rcc;
			private Coordinate p0 = new Coordinate();
			private Coordinate p1 = new Coordinate();
			
			public RayCrossingSegmentFilter(RayCrossingCounter rcc)
			{
				this.rcc = rcc;
			}
			
		  public void filter(CoordinateSequence seq, int i)
		  {
		  	if (i == 0) return;
		  	seq.getCoordinate(i - 1, p0);
		  	seq.getCoordinate(i, p1);
		  	rcc.countSegment(p0, p1);
		  }
		  
		  public boolean isDone() { return rcc.isOnSegment(); }
		  
		  public boolean isGeometryChanged() { return false; }
		}
	}
}



