package com.vividsolutions.jtstest.util;

/**
 * Cleans text strings which are supposed
 * to contain valid text for Geometries 
 * (either WKB, WKB, or GML) 
 * 
 * @author mbdavis
 *
 */
public class GeometryTextCleaner 
{
	public static final String WKT_SYMBOLS = "(),.-";
	
	public static String cleanWKT(String input)
	{
		return clean(input, WKT_SYMBOLS);
	}
	
	private static String clean(String input, String allowedSymbols)
	{
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (isAllowed(c, allowedSymbols))
				buf.append(c);
		}
		return buf.toString();
	}
	
	private static boolean isAllowed(char c, String allowedSymbols)
	{
		if (Character.isWhitespace(c)) return true;
		if (Character.isLetterOrDigit(c)) return true;
		if (allowedSymbols.indexOf(c) >= 0) return true;
		return false;		
	}
	
}
