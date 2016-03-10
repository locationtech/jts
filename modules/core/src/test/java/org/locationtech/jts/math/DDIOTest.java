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

package org.locationtech.jts.math;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests I/O for {@link DD}s.
 * 
 * @author mbdavis
 * 
 */
public class DDIOTest extends TestCase {
	public static void main(String args[]) {
		TestRunner.run(DDIOTest.class);
	}

	public DDIOTest(String name) {
		super(name);
	}


	public void testStandardNotation() 
	{	
		// standard cases
		checkStandardNotation(1.0, "1.0");
		checkStandardNotation(0.0, "0.0");
		
		// cases where hi is a power of 10 and lo is negative
		checkStandardNotation(DD.valueOf(1e12).subtract(DD.valueOf(1)),	"999999999999.0");
		checkStandardNotation(DD.valueOf(1e14).subtract(DD.valueOf(1)),	"99999999999999.0");
  	checkStandardNotation(DD.valueOf(1e16).subtract(DD.valueOf(1)),	"9999999999999999.0");
		
		DD num8Dec = DD.valueOf(-379363639).divide(
				DD.valueOf(100000000));
		checkStandardNotation(num8Dec, "-3.79363639");
		
		checkStandardNotation(new DD(-3.79363639, 8.039137357367426E-17),
				"-3.7936363900000000000000000");
		
		checkStandardNotation(DD.valueOf(34).divide(
				DD.valueOf(1000)), "0.034");
		checkStandardNotation(1.05e3, "1050.0");
		checkStandardNotation(0.34, "0.34000000000000002442490654175344");
		checkStandardNotation(DD.valueOf(34).divide(
				DD.valueOf(100)), "0.34");
		checkStandardNotation(14, "14.0");
	}

	private void checkStandardNotation(double x, String expectedStr) {
		checkStandardNotation(DD.valueOf(x), expectedStr);
	}

	private void checkStandardNotation(DD x, String expectedStr) {
		String xStr = x.toStandardNotation();
		//System.out.println("Standard Notation: " + xStr);
		assertEquals(expectedStr, xStr);
	}

	public void testSciNotation() {
		checkSciNotation(0.0, "0.0E0");
		checkSciNotation(1.05e10, "1.05E10");
		checkSciNotation(0.34, "3.4000000000000002442490654175344E-1");
		checkSciNotation(
				DD.valueOf(34).divide(DD.valueOf(100)), "3.4E-1");
		checkSciNotation(14, "1.4E1");
	}

	private void checkSciNotation(double x, String expectedStr) {
		checkSciNotation(DD.valueOf(x), expectedStr);
	}

	private void checkSciNotation(DD x, String expectedStr) {
		String xStr = x.toSciNotation();
		//System.out.println("Sci Notation: " + xStr);
		assertEquals(xStr, expectedStr);
	}

	public void testParse() {
		checkParse("1.05e10", 1.05E10, 1e-32);
		checkParse("-1.05e10", -1.05E10, 1e-32);
		checkParse("1.05e-10", DD.valueOf(105.).divide(
				DD.valueOf(100.)).divide(DD.valueOf(1.0E10)), 1e-32);
		checkParse("-1.05e-10", DD.valueOf(105.).divide(
				DD.valueOf(100.)).divide(DD.valueOf(1.0E10))
				.negate(), 1e-32);

		/**
		 * The Java double-precision constant 1.4 gives rise to a value which
		 * differs from the exact binary representation down around the 17th decimal
		 * place. Thus it will not compare exactly to the DoubleDouble
		 * representation of the same number. To avoid this, compute the expected
		 * value using full DD precision.
		 */
		checkParse("1.4",
				DD.valueOf(14).divide(DD.valueOf(10)), 1e-30);

		// 39.5D can be converted to an exact FP representation
		checkParse("39.5", 39.5, 1e-30);
		checkParse("-39.5", -39.5, 1e-30);
	}

	private void checkParse(String str, double expectedVal, double errBound) {
		checkParse(str, new DD(expectedVal), errBound);
	}

	private void checkParse(String str, DD expectedVal,
			double relErrBound) {
		DD xdd = DD.parse(str);
		double err = xdd.subtract(expectedVal).doubleValue();
		double relErr = err / xdd.doubleValue();

		//System.out.println("Parsed= " + xdd + " rel err= " + relErr);

		assertTrue(err <= relErrBound);
	}

	public void testParseError() {
		checkParseError("-1.05E2w");
		checkParseError("%-1.05E2w");
		checkParseError("-1.0512345678t");
	}

	private void checkParseError(String str) {
		boolean foundParseError = false;
		try {
			DD.parse(str);
		} catch (NumberFormatException ex) {
			foundParseError = true;
		}
		assertTrue(foundParseError);
	}

	public void testRepeatedSqrt()
	{
		writeRepeatedSqrt(DD.valueOf(1.0));
		writeRepeatedSqrt(DD.valueOf(.999999999999));
		writeRepeatedSqrt(DD.PI.divide(DD.valueOf(10)));
	}
	
	/**
	 * This routine simply tests for robustness of the toString function.
	 * 
	 * @param xdd
	 */
	void writeRepeatedSqrt(DD xdd) 
	{
		int count = 0;
		while (xdd.doubleValue() > 1e-300) {
			count++;

			double x = xdd.doubleValue();
			DD xSqrt = xdd.sqrt();
			String s = xSqrt.toString();
//			System.out.println(count + ": " + s);

			DD xSqrt2 = DD.parse(s);
			DD xx = xSqrt2.multiply(xSqrt2);
			double err = Math.abs(xx.doubleValue() - x);
			//assertTrue(err < 1e-10);
	
			xdd = xSqrt;

			// square roots converge on 1 - stop when very close
			DD distFrom1DD = xSqrt.subtract(DD.valueOf(1.0));
			double distFrom1 = distFrom1DD.doubleValue();
			if (Math.abs(distFrom1) < 1.0e-40)
				break;
		}
	}
	
	public void testRepeatedSqr()
	{
		writeRepeatedSqr(DD.valueOf(.9));
		writeRepeatedSqr(DD.PI.divide(DD.valueOf(10)));
	}
	
	/**
	 * This routine simply tests for robustness of the toString function.
	 * 
	 * @param xdd
	 */
	void writeRepeatedSqr(DD xdd) 
	{
		if (xdd.ge(DD.valueOf(1)))
			throw new IllegalArgumentException("Argument must be < 1");
		
		int count = 0;
		while (xdd.doubleValue() > 1e-300) {
			count++;
			if (count == 100)
				count = count;
			double x = xdd.doubleValue();
			DD xSqr = xdd.sqr();
			String s = xSqr.toString();
			//System.out.println(count + ": " + s);

			DD xSqr2 = DD.parse(s);
	
			xdd = xSqr;
		}
	}
	
	public void testIOSquaresStress() {
		for (int i = 1; i < 10000; i++) {
			writeAndReadSqrt(i);
		}
	}

	/**
	 * Tests that printing values with many decimal places works. 
	 * This tests the correctness and robustness of both output and input.
	 * 
	 * @param x
	 */
	void writeAndReadSqrt(double x) {
		DD xdd = DD.valueOf(x);
		DD xSqrt = xdd.sqrt();
		String s = xSqrt.toString();
//		System.out.println(s);

		DD xSqrt2 = DD.parse(s);
		DD xx = xSqrt2.multiply(xSqrt2);
		String xxStr = xx.toString();
//		System.out.println("==>  " + xxStr);

		DD xx2 = DD.parse(xxStr);
		double err = Math.abs(xx2.doubleValue() - x);
		assertTrue(err < 1e-10);
	}

}