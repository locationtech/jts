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

package org.locationtech.jtstest.testrunner;

import org.locationtech.jts.geom.*;

/**
 * An interface for classes which can determine whether
 * two {@link Result}s match, within a given <tt>tolerance</tt>.
 * The matching may also take into account the original input parameters
 * to the geometry method.
 *
 * @author mbdavis
 *
 */
public interface ResultMatcher
{
	/**
	 * Tests whether the actual and expected results match well
	 * enough for the test to be considered as passed.
	 *
	 * @param geom the target geometry
	 * @param opName the operation performed
	 * @param args the input arguments to the operation
	 * @param actualResult the actual computed result
	 * @param expectedResult the expected result of the test
	 * @param tolerance the tolerance for the test
	 * @return true if the actual and expected results match
	 */
	boolean isMatch(Geometry geom, String opName, Object[] args,
			Result actualResult, Result expectedResult,
			double tolerance);
}
