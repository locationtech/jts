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

import com.vividsolutions.jts.generator.PolygonGenerator;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

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
 * NOTE: The points may be re-ordered during these tests. 
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class StaticPolygonTest extends WritingTestCase {

	/**
	 * @param arg
	 */
	public StaticPolygonTest(String arg) {
		super(arg);
	}

	/**
	 * Round Trip test for a single polygon
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public void testSinglePolygonNoHoleRoundTrip() throws IOException, SAXException, ParserConfigurationException{
		PolygonGenerator pg = new PolygonGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberPoints(10);
		
		Polygon pt = (Polygon) pg.create();
		
		GMLWriter out = new GMLWriter();
		out.setPrefix("");
		out.write(pt,getWriter());
		
		GMLReader in = new GMLReader();
		Polygon pt2 = (Polygon) in.read(getReader(),geometryFactory);
		
		assertTrue("The input polygon is not the same as the output polygon",pt.equals(pt2));
	}

	/**
	 * Round Trip test for a single polygon with lotsa points
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public void testSinglePolygonManyPointsNoHoleRoundTrip() throws IOException, SAXException, ParserConfigurationException{
		PolygonGenerator pg = new PolygonGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setGenerationAlgorithm(PolygonGenerator.BOX);
		pg.setNumberPoints(1000);
		
		Polygon pt = (Polygon) pg.create();

		GMLWriter out = new GMLWriter();
		out.setPrefix("");
		out.write(pt,getWriter());
		
		GMLReader in = new GMLReader();
		Polygon pt2 = (Polygon) in.read(getReader(),geometryFactory);

		assertTrue("The input polygon is not the same as the output polygon",pt.equals(pt2));
	}

	/**
	 * Round Trip test for a single polygon
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSinglePolygonHolesRoundTrip() throws SAXException, IOException, ParserConfigurationException{
		PolygonGenerator pg = new PolygonGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberPoints(10);
		pg.setNumberHoles(4);
		
		Polygon pt = (Polygon) pg.create();

		GMLWriter out = new GMLWriter();
		out.setPrefix("");
		out.write(pt,getWriter());
		
		GMLReader in = new GMLReader();
		Polygon pt2 = (Polygon) in.read(getReader(),geometryFactory);
		
//		System.out.println(pt);
//		System.out.println(pt2);
		assertTrue("The input polygon is not the same as the output polygon",pt.equals(pt2));
	}

	/**
	 * Round Trip test for a single polygon with lotsa points
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSinglePolygonManyPointsHolesRoundTrip() throws SAXException, IOException, ParserConfigurationException{
		PolygonGenerator pg = new PolygonGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setGenerationAlgorithm(PolygonGenerator.BOX);
		pg.setNumberPoints(1000);
		pg.setNumberHoles(4);
		
		Polygon pt = (Polygon) pg.create();

		GMLWriter out = new GMLWriter();
		out.setPrefix("");
		out.write(pt,getWriter());
		
		GMLReader in = new GMLReader();
		Polygon pt2 = (Polygon) in.read(getReader(),geometryFactory);

		assertTrue("The input polygon is not the same as the output polygon",pt.equals(pt2));
	}

	/**
	 * Round Trip test for a single polygon with lotsa points
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSinglePolygonManyPointsManyHolesRoundTrip() throws SAXException, IOException, ParserConfigurationException{
		PolygonGenerator pg = new PolygonGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setGenerationAlgorithm(PolygonGenerator.BOX);
		pg.setNumberPoints(100);
		pg.setNumberHoles(100);
		
		Polygon pt = (Polygon) pg.create();

		GMLWriter out = new GMLWriter();
		out.setPrefix("");
		out.write(pt,getWriter());
		
		GMLReader in = new GMLReader();
		Polygon pt2 = (Polygon) in.read(getReader(),geometryFactory);

		assertTrue("The input polygon is not the same as the output polygon",pt.equals(pt2));
	}
}
