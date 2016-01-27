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
