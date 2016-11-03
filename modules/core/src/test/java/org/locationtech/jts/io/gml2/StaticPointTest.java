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

import org.locationtech.jts.generator.PointGenerator;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.xml.sax.SAXException;


/**
 * Round trip testing for GML reading and writing. 
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class StaticPointTest extends WritingTestCase {

	/**
	 * @param arg
	 */
	public StaticPointTest(String arg) {
		super(arg);
	}

	/**
	 * Round Trip test for a single point
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSinglePointRoundTrip() throws SAXException, IOException, ParserConfigurationException{
		PointGenerator pg = new PointGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		
		Point pt = (Point) pg.create();
		checkRoundTrip(pt);
	}
}
