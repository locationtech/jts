package test.jts.perf.algorithm;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.algorithm.locate.PointOnGeometryLocator;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.util.*;

/**
 * Creates a perturbed, buffered grid and tests a set
 * of points against using two PointInArea classes.
 * 
 * @author mbdavis
 *
 */
public class PointInAreaPerfTester 
{
	private GeometryFactory geomFactory;
	private Geometry area;
	
	private int numPts = 10000;
	private PointOnGeometryLocator pia1;
	private int[] locationCount = new int[3];
		
	public PointInAreaPerfTester(GeometryFactory geomFactory, Geometry area)
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
	
	/**
	 * 
	 * @return true if all point locations were computed correctly
	 */
	public boolean run()
	{ 
		Stopwatch sw = new Stopwatch();
		
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
				
				int loc = pia1.locate(pt);
				locationCount[loc]++;
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

	
}

