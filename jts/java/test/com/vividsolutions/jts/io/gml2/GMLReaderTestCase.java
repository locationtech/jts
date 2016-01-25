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

import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.*;

import junit.framework.TestCase;

public class GMLReaderTestCase extends TestCase 
{

	private static final String TEST_DIR = "bin/data/";
	
	public GMLReaderTestCase(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	protected static PrecisionModel precisionModel = new PrecisionModel(1000);
	protected static GeometryFactory geometryFactory = new GeometryFactory(precisionModel);
	
	public void testPointRead() throws SAXException, IOException, ParserConfigurationException{
		FileReader fr = new FileReader(TEST_DIR + "points.xml");
		
		GMLReader gr = new GMLReader();
		Geometry g = gr.read(fr,geometryFactory);
		
		GeometryCollection gc = (GeometryCollection)g;
		assertTrue(gc.getNumGeometries() == 25);
		
		for(int i=0;i<25;i++){
			Point p = (Point) gc.getGeometryN(i);
			assertNotNull(p);
		}
	}

	public void testLineStringRead() throws SAXException, IOException, ParserConfigurationException{
		FileReader fr = new FileReader(TEST_DIR + "linestrings.xml");
		
		GMLReader gr = new GMLReader();
		Geometry g = gr.read(fr,geometryFactory);
		
		GeometryCollection gc = (GeometryCollection)g;
		assertTrue(gc.getNumGeometries() == 25);
		
		for(int i=0;i<25;i++){
			LineString ls = (LineString) gc.getGeometryN(i);
			assertNotNull(ls);
		}
	}

	public void testPolygonRead() throws SAXException, IOException, ParserConfigurationException{
		FileReader fr = new FileReader(TEST_DIR + "polygons.xml");
		
		GMLReader gr = new GMLReader();
		Geometry g = gr.read(fr,geometryFactory);
		
		GeometryCollection gc = (GeometryCollection)g;
		assertTrue(gc.getNumGeometries() == 25);
		
		for(int i=0;i<25;i++){
			Polygon p = (Polygon) gc.getGeometryN(i);
			assertNotNull(p);
		}
	}
	
	public void testMultiPointRead() throws SAXException, IOException, ParserConfigurationException{
		FileReader fr = new FileReader(TEST_DIR + "multipoints.xml");
		
		GMLReader gr = new GMLReader();
		Geometry g = gr.read(fr,geometryFactory);
		
		GeometryCollection gc = (GeometryCollection)g;
		assertTrue(gc.getNumGeometries() == 25);
		
		for(int i=0;i<25;i++){
			MultiPoint p = (MultiPoint) gc.getGeometryN(i);
			assertNotNull(p);
		}
	}

	public void testMultiLineStringRead() throws SAXException, IOException, ParserConfigurationException{
		FileReader fr = new FileReader(TEST_DIR + "multilinestrings.xml");
		
		GMLReader gr = new GMLReader();
		Geometry g = gr.read(fr,geometryFactory);
		
		GeometryCollection gc = (GeometryCollection)g;
		assertTrue(gc.getNumGeometries() == 25);
		
		for(int i=0;i<25;i++){
			MultiLineString ls = (MultiLineString) gc.getGeometryN(i);
			assertNotNull(ls);
		}
	}

	public void testMultiPolygonRead() throws SAXException, IOException, ParserConfigurationException{
		FileReader fr = new FileReader(TEST_DIR + "multipolygons.xml");
		
		GMLReader gr = new GMLReader();
		Geometry g = gr.read(fr,geometryFactory);
		
		GeometryCollection gc = (GeometryCollection)g;
		assertTrue(gc.getNumGeometries() == 25);
		
		for(int i=0;i<25;i++){
			MultiPolygon p = (MultiPolygon) gc.getGeometryN(i);
			assertNotNull(p);
		}
	}

}
