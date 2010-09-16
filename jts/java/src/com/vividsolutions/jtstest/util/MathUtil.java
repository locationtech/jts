package com.vividsolutions.jtstest.util;

public class MathUtil {

	private static final double LOG_10 = Math.log(10);
	
	public static double log10(double x)
	{
		return Math.log(x)/ LOG_10;
	}
}
