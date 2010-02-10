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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.vividsolutions.jts.generator.LineStringGenerator;
import com.vividsolutions.jts.generator.MultiGenerator;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * 
 * Does round trip testing by creating the oracle object, then decoding it. 
 * 
 * These tests do not include insert / delete / select operations.
 * 
 * NOTE: This test does require a precision to be used during the comparison, 
 * as points are rounded somewhat when creating the oracle struct. 
 * (One less decimal than a java double).
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class StaticMultiLineStringTest extends WritingTestCase {

	/**
	 * @param arg
	 */
	public StaticMultiLineStringTest(String arg) {
		super(arg);
	}

	/**
	 * Round Trip test for a single line string
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSingleMultiLineStringRoundTrip() throws SAXException, IOException, ParserConfigurationException{
		LineStringGenerator pgc = new LineStringGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setNumberPoints(10);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		MultiLineString pt = (MultiLineString) pg.create();

		GMLWriter out = new GMLWriter();
		out.setPrefix(null);
		out.write(pt,getWriter());
		
		GMLReader in = new GMLReader();
		MultiLineString pt2 = (MultiLineString) in.read(getReader(),geometryFactory);
		
//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input MultiLineString is not the same as the output MultiLineString",pt.equals(pt2));
	}

	/**
	 * Round Trip test for a single line string with lotsa points
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public void testSingleMultiLineStringManyPointRoundTrip() throws IOException, SAXException, ParserConfigurationException{

		LineStringGenerator pgc = new LineStringGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setNumberPoints(1000);
		pgc.setGenerationAlgorithm(LineStringGenerator.HORZ);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		MultiLineString pt = (MultiLineString) pg.create();
//		System.out.println((pt==null?"NULL":pt.toString()));


		GMLWriter out = new GMLWriter();
		out.setPrefix(null);
		out.write(pt,getWriter());
		
		GMLReader in = new GMLReader();
		MultiLineString pt2 = (MultiLineString) in.read(getReader(),geometryFactory);

//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input MultiLineString is not the same as the output MultiLineString",pt.equals(pt2));
	}
}
