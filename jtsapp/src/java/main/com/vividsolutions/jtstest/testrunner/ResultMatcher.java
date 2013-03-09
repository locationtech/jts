package com.vividsolutions.jtstest.testrunner;

import com.vividsolutions.jts.geom.*;

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
