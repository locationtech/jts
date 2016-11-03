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

import org.locationtech.jts.geom.Geometry;

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
