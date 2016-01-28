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
package org.locationtech.jts.io.gml2;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;

import org.locationtech.jts.generator.*;
import org.locationtech.jts.geom.*;
import org.xml.sax.SAXException;


/**
 * Round trip testing for GML reading and writing. 
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class StaticLineStringTest extends WritingTestCase {

	/**
	 * @param arg
	 */
	public StaticLineStringTest(String arg) {
		super(arg);
	}

	/**
	 * Round Trip test for a single line string
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSingleLineStringRoundTrip() throws SAXException, IOException, ParserConfigurationException{
		LineStringGenerator pg = new LineStringGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberPoints(10);
		
		LineString pt = (LineString) pg.create();
		
		checkRoundTrip(pt);
	}

	/**
	 * Round Trip test for a single line string with lotsa points
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSingleLineStringManyPointRoundTrip() throws SAXException, IOException, ParserConfigurationException{
		LineStringGenerator pg = new LineStringGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setGenerationAlgorithm(LineStringGenerator.HORZ);
		pg.setNumberPoints(1000);
		
		LineString pt = (LineString) pg.create();

		checkRoundTrip(pt);
	}
}

