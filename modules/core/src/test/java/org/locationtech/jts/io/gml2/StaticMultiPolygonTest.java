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

import org.locationtech.jts.generator.MultiGenerator;
import org.locationtech.jts.generator.PolygonGenerator;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiPolygon;
import org.xml.sax.SAXException;


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
