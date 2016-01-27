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
 * A {@link ResultMatcher} which always passes.
 * This is useful if the expected result of an operation is not known.
 * 
 * @author mbdavis
 *
 */
public class NullResultMatcher
 implements ResultMatcher
{
	/**
	 * Always reports a match.
	 * 
	 * @return true always
	 */
	public boolean isMatch(Geometry geom, String opName, Object[] args, 
			Result actualResult, Result expectedResult,
			double tolerance)
	{
		return true;
	}

	
}
