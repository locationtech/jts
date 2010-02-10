/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.io.gml2;

import java.io.*;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import junit.framework.TestCase;

/**
 * 
 * Abstract Test Case. Intended to provide some IO operations for the middle of a test.
 * 
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class WritingTestCase extends TestCase {
	
	/**
	 * @param arg
	 */
	public WritingTestCase(String arg){
		super(arg);
	}
	
	protected StringWriter sw = null;
	protected Writer getWriter(){
		sw = new StringWriter();
		sw.write("<?xml version='1.0' encoding='UTF-8'?>\n");
		return sw;
	}
	protected Reader getReader() throws IOException{
		sw.flush();
		sw.close();
		String s = sw.toString();
		
//		System.out.println(s);
		
		return new StringReader(s);
	}
	
	protected static PrecisionModel precisionModel = new PrecisionModel(1000);
	protected static GeometryFactory geometryFactory = new GeometryFactory(precisionModel);

}
