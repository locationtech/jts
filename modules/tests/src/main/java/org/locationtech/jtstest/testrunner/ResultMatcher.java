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
