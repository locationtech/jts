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
package com.vividsolutions.jtstest.testrunner;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A {@link ResultMatcher} which compares result for equality,
 * up to the given tolerance.
 * 
 * @author mbdavis
 *
 */
public class EqualityResultMatcher
 implements ResultMatcher
{
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
		return actualResult.equals(expectedResult, tolerance);
	}

	
}
