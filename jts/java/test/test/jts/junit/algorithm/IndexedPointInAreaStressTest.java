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
package test.jts.junit.algorithm;

import test.jts.perf.algorithm.PointInAreaStressTester;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.vividsolutions.jts.algorithm.locate.PointOnGeometryLocator;


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



