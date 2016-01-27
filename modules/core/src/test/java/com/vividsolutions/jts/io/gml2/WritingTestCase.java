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
package com.vividsolutions.jts.io.gml2;

import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.vividsolutions.jts.geom.*;
import junit.framework.TestCase;

/**
 * Test Case framework for GML unit tests.
 * 
 * @author David Zwiers, Vivid Solutions.
 * @author Martin Davis 
 */
public abstract class WritingTestCase extends TestCase 
{
	
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

	protected void checkRoundTrip(Geometry g) 
	throws SAXException, IOException, ParserConfigurationException
	{
		GMLWriter out = new GMLWriter();
		out.setPrefix(null);
		out.setNamespace(true);
		out.setSrsName("foo");
		// this markup is not currently work with GMLReader
//		out.setCustomElements(new String[] { "<test>1</test>" } );
		out.write(g, getWriter());

		System.out.println(sw.toString());
		
		GMLReader in = new GMLReader();
		Geometry g2 = in.read(getReader(), geometryFactory);

		// System.out.println((pt==null?"NULL":pt.toString()));
		// System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input Geometry is not the same as the output Geometry", g
				.equalsExact(g2));
	}
}
