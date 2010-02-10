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
package test.jts.perf.algorithm;


import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.algorithm.locate.PointOnGeometryLocator;

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



