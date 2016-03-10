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
package org.locationtech.jts.io.gml2;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.xml.sax.SAXException;

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

		//System.out.println(sw.toString());
		
		GMLReader in = new GMLReader();
		Geometry g2 = in.read(getReader(), geometryFactory);

		// System.out.println((pt==null?"NULL":pt.toString()));
		// System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input Geometry is not the same as the output Geometry", g
				.equalsExact(g2));
	}
}
