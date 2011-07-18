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

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.algorithm.locate.PointOnGeometryLocator;
import com.vividsolutions.jts.algorithm.locate.SimplePointInAreaLocator;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.util.*;

/**
 * Creates a perturbed, buffered grid and tests a set
 * of points against using two PointInArea classes.
 * 
 * @author mbdavis
 *
 */
public class PointInAreaStressTester 
{
	private GeometryFactory geomFactory;
	private Geometry area;
	private boolean ignoreBoundaryResults = true;
	
	private int numPts = 10000;
	private PointOnGeometryLocator pia1;
	private PointOnGeometryLocator pia2;
	private int[] locationCount = new int[3];
		
	public PointInAreaStressTester(GeometryFactory geomFactory, Geometry area)
	{
		this.geomFactory = geomFactory;
		this.area = area;
		
	}
	
	public void setNumPoints(int numPoints)
	{
		this.numPts = numPoints;
	}
	
	
	public void setPIA(PointOnGeometryLocator pia)
	{
		this.pia1 = pia;
	}
	
  public void setExpected(PointOnGeometryLocator pia)
  {
    this.pia2 = pia;
  }
  
	public void setIgnoreBoundaryResults(boolean ignoreBoundaryResults)
	{
		this.ignoreBoundaryResults = ignoreBoundaryResults;
	}
	
	/**
	 * 
	 * @return true if all point locations were computed correctly
	 */
	public boolean run()
	{ 
		Stopwatch sw = new Stopwatch();
		
		// default is to use the simple, non-indexed tester
    if (pia2 == null)
      pia2 = new SimplePointInAreaLocator(area);
		
		
		int ptGridWidth = (int) Math.sqrt(numPts);
		
		Envelope areaEnv = area.getEnvelopeInternal();
		double xStep = areaEnv.getWidth() / (ptGridWidth - 1);
		double yStep = areaEnv.getHeight() / (ptGridWidth - 1);

		for (int i = 0; i < ptGridWidth; i++) {
			for (int j = 0; j < ptGridWidth; j++) {
				
				// compute test point
				double x = areaEnv.getMinX() +  i * xStep;
				double y = areaEnv.getMinY() + j * yStep;
				Coordinate pt = new Coordinate(x, y);
				geomFactory.getPrecisionModel().makePrecise(pt);
				
				boolean isEqual = testPIA(pt);
				if (! isEqual)
					return false;
			}
		}
		System.out.println("Test completed in " + sw.getTimeString());
		printStats();
		return true;
	}
	
	public void printStats()
	{
		System.out.println("Location counts: "
				+ " Boundary = "	+ locationCount[Location.BOUNDARY]
				+ " Interior = "	+ locationCount[Location.INTERIOR]
				+ " Exterior = "	+ locationCount[Location.EXTERIOR]
				                );
	}
	/**
	 * 
	 * @param p
	 * @return true if the point location is determined to be the same by both PIA locaters
	 */
	private boolean testPIA(Coordinate p)
	{
		//System.out.println(WKTWriter.toPoint(p));
		
		int loc1 = pia1.locate(p);
		int loc2 = pia2.locate(p);
		
		locationCount[loc1]++;
		
		if ((loc1 == Location.BOUNDARY || loc2 == Location.BOUNDARY)
				&& ignoreBoundaryResults)
			return true;
		
		return loc1 == loc2;
	}
	
}


