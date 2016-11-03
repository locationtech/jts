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

import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.util.Stopwatch;

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


