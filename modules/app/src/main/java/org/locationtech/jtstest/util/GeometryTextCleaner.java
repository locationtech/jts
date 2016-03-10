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

package org.locationtech.jtstest.util;

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
