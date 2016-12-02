/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testrunner;

import org.locationtech.jts.algorithm.distance.DiscreteHausdorffDistance;
import org.locationtech.jts.geom.Geometry;

/**
 * A {@link ResultMatcher} which compares the results of
 * buffer operations for equality, up to the given tolerance.
 * All other operations are delagated to the 
 * standard {@link EqualityResultMatcher} algorithm.
 * 
 * @author mbdavis
 *
 */
public class BufferResultMatcher
 implements ResultMatcher
{
	private ResultMatcher defaultMatcher = new EqualityResultMatcher();
	
	/**
	 * Tests whether the two results are equal within the given
	 * tolerance.  The input parameters are not considered.
	 * 
	 * @return true if the actual and expected results are considered equal
	 */
	public boolean isMatch(Geometry geom, String opName, Object[] args, 
			Result actualResult, Result expectedResult,
			double tolerance)
	{
		if (! opName.equalsIgnoreCase("buffer"))
			return defaultMatcher.isMatch(geom, opName, args, actualResult, expectedResult, tolerance);
		
		double distance = Double.parseDouble((String) args[0]);
		return isBufferResultMatch(
				((GeometryResult) actualResult).getGeometry(),
				((GeometryResult) expectedResult).getGeometry(),
				distance);
	}

	private static final double MAX_RELATIVE_AREA_DIFFERENCE = 1.0E-3; 
	private static final double MAX_HAUSDORFF_DISTANCE_FACTOR = 100; 
	/**
	 * The minimum distance tolerance which will be used.
	 * This is required because densified vertices do no lie precisely on their parent segment.
	 */
	private static final double MIN_DISTANCE_TOLERANCE = 1.0e-8; 
	
	public boolean isBufferResultMatch(Geometry actualBuffer, Geometry expectedBuffer, double distance)
	{
		if (actualBuffer.isEmpty() && expectedBuffer.isEmpty())
			return true;
		
		/**
		 * MD - need some more checks here - symDiffArea won't catch very small holes ("tears") 
		 * near the edge of computed buffers (which can happen in current version of JTS (1.8)).  
		 * This can probably be handled by testing
		 * that every point of the actual buffer is at least a certain distance away from the 
		 * geometry boundary.  
		*/
		if (! isSymDiffAreaInTolerance(actualBuffer, expectedBuffer))
			return false;
		
		if (! isBoundaryHausdorffDistanceInTolerance(actualBuffer, expectedBuffer, distance))
			return false;
		
		return true;
	}
	
	public boolean isSymDiffAreaInTolerance(Geometry actualBuffer, Geometry expectedBuffer)
	{
		double area = expectedBuffer.getArea();
		Geometry diff = actualBuffer.symDifference(expectedBuffer);
//		System.out.println(diff);
		double areaDiff = diff.getArea();
		
		// can't get closer than difference area = 0 !  This also handles case when symDiff is empty
		if (areaDiff <= 0.0)
			return true;
		
		double frac = Double.POSITIVE_INFINITY;
		if (area > 0.0)
			frac = areaDiff / area;

		return frac < MAX_RELATIVE_AREA_DIFFERENCE;
	}
	
	public boolean isBoundaryHausdorffDistanceInTolerance(Geometry actualBuffer, Geometry expectedBuffer, double distance)
	{
		Geometry actualBdy = actualBuffer.getBoundary();
		Geometry expectedBdy = expectedBuffer.getBoundary();
		
    DiscreteHausdorffDistance haus = new DiscreteHausdorffDistance(actualBdy, expectedBdy);
    haus.setDensifyFraction(0.25);
    double maxDistanceFound = haus.orientedDistance();
    double expectedDistanceTol = Math.abs(distance) / MAX_HAUSDORFF_DISTANCE_FACTOR;
    if (expectedDistanceTol < MIN_DISTANCE_TOLERANCE)
    	expectedDistanceTol = MIN_DISTANCE_TOLERANCE;
    if (maxDistanceFound > expectedDistanceTol)
    	return false;
    return true;
	}
	
}
