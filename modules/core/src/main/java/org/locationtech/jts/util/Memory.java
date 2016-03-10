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
package org.locationtech.jts.util;

/**
 * Utility functions to report JVM memory usage.
 * 
 * @author mbdavis
 *
 */
public class Memory 
{
	public static long used()
	{
		Runtime runtime = Runtime.getRuntime ();
		return runtime.totalMemory() - runtime.freeMemory();
	}
	
	public static String usedString()
	{
		return format(used());
	}
	
	public static long free()
	{
		Runtime runtime = Runtime.getRuntime ();
		return runtime.freeMemory();
	}
	
	public static String freeString()
	{
		return format(free());
	}
	
	public static long total()
	{
		Runtime runtime = Runtime.getRuntime ();
		return runtime.totalMemory();
	}
	
	public static String totalString()
	{
		return format(total());
	}
	
	public static String usedTotalString()
	{
		return "Used: " + usedString() 
		+ "   Total: " + totalString();
	}
	
	public static String allString()
	{
		return "Used: " + usedString() 
		+ "   Free: " + freeString()
		+ "   Total: " + totalString();
	}
	
	public static final double KB = 1024;
	public static final double MB = 1048576;
	public static final double GB = 1073741824;

	public static String format(long mem)
	{
		if (mem < 2 * KB)
			return mem + " bytes";
		if (mem < 2 * MB)
			return round(mem / KB) + " KB";
		if (mem < 2 * GB)
			return round(mem / MB) + " MB";
		return round(mem / GB) + " GB";
	}
	
	public static double round(double d)
	{
		return Math.ceil(d * 100) / 100;
	}
}
