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

import javax.xml.parsers.ParserConfigurationException;

import org.locationtech.jts.generator.PolygonGenerator;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.xml.sax.SAXException;


/**
 * Round trip testing for GML reading and writing. 
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
		checkRoundTrip(pt);
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
		checkRoundTrip(pt);
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
		checkRoundTrip(pt);
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
		checkRoundTrip(pt);
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
		checkRoundTrip(pt);
	}
}
