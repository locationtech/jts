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

import com.vividsolutions.jts.generator.MultiGenerator;
import com.vividsolutions.jts.generator.PolygonGenerator;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * Round trip testing for GML reading and writing. 
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class StaticMultiPolygonTest extends WritingTestCase {

	/**
	 * @param arg
	 */
	public StaticMultiPolygonTest(String arg) {
		super(arg);
	}

	/**
	 * Round Trip test for a single MultiPolygon
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSingleMultiPolygonNoHoleRoundTrip() throws SAXException, IOException, ParserConfigurationException{
		PolygonGenerator pgc = new PolygonGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setNumberPoints(10);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		MultiPolygon pt = (MultiPolygon) pg.create();

		checkRoundTrip(pt);
	}

	/**
	 * Round Trip test for a single MultiPolygon with lotsa points
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSingleMultiPolygonManyPointsNoHoleRoundTrip() throws SAXException, IOException, ParserConfigurationException{

		PolygonGenerator pgc = new PolygonGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setGenerationAlgorithm(PolygonGenerator.BOX);
		pgc.setNumberPoints(1000);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		MultiPolygon pt = (MultiPolygon) pg.create();

		checkRoundTrip(pt);
	}

	/**
	 * Round Trip test for a single MultiPolygon
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSingleMultiPolygonHolesRoundTrip() throws SAXException, IOException, ParserConfigurationException{

		PolygonGenerator pgc = new PolygonGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setGenerationAlgorithm(PolygonGenerator.BOX);
		pgc.setNumberPoints(10);
		pgc.setNumberHoles(4);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		MultiPolygon pt = (MultiPolygon) pg.create();

		checkRoundTrip(pt);
	}

	/**
	 * Round Trip test for a single MultiPolygon with lotsa points
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSingleMultiPolygonManyPointsHolesRoundTrip() throws SAXException, IOException, ParserConfigurationException{

		PolygonGenerator pgc = new PolygonGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setGenerationAlgorithm(PolygonGenerator.BOX);
		pgc.setNumberPoints(1000);
		pgc.setNumberHoles(4);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		MultiPolygon pt = (MultiPolygon) pg.create();
//		System.out.println((pt==null?"NULL":pt.toString()));

		checkRoundTrip(pt);
	}

	/**
	 * Round Trip test for a single MultiPolygon with lotsa points
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSingleMultiPolygonManyPointsManyHolesRoundTrip() throws SAXException, IOException, ParserConfigurationException{

		PolygonGenerator pgc = new PolygonGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setGenerationAlgorithm(PolygonGenerator.BOX);
		pgc.setNumberPoints(100);
		pgc.setNumberHoles(100);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		MultiPolygon pt = (MultiPolygon) pg.create();
//		System.out.println((pt==null?"NULL":pt.toString()));

		checkRoundTrip(pt);
	}
}
