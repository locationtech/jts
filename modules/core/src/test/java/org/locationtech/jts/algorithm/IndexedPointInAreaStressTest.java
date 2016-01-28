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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.*;

import test.jts.perf.algorithm.PointInAreaStressTester;
import junit.framework.TestCase;
import junit.textui.TestRunner;



public class IndexedPointInAreaStressTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(IndexedPointInAreaStressTest.class);
  }

	PrecisionModel pmFixed_1 = new PrecisionModel(1.0);
	
	public IndexedPointInAreaStressTest(String name) {
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
    gridBuilder.setSeed(1185072199562L);
		Geometry area = gridBuilder.getGeometry();
		
//    PointInAreaLocator pia = new IndexedPointInAreaLocator(area); 
    PointOnGeometryLocator pia = new IndexedPointInAreaLocator(area); 

		PointInAreaStressTester gridTester = new PointInAreaStressTester(geomFactory, area);
		gridTester.setNumPoints(100000);
		gridTester.setPIA(pia);
		
		boolean isCorrect = gridTester.run();
		assertTrue(isCorrect);
	}
}



