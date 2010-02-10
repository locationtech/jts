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
import com.vividsolutions.jts.generator.PointGenerator;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPoint;

/**
 * 
 * Does round trip testing by creating the oracle object, then decoding it. 
 * 
 * These tests do not include insert / delete / select operations.
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class StaticMultiPointTest extends WritingTestCase {

	/**
	 * @param arg
	 */
	public StaticMultiPointTest(String arg) {
		super(arg);
	}

	/**
	 * Round Trip test for a single MultiPoint
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void testSingleMultiPointRoundTrip() throws SAXException, IOException, ParserConfigurationException{
		PointGenerator pgc = new PointGenerator();
		pgc.setGeometryFactory(geometryFactory);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		MultiPoint pt = (MultiPoint) pg.create();

		GMLWriter out = new GMLWriter();
		out.setPrefix(null);
		out.write(pt,getWriter());
		
		GMLReader in = new GMLReader();
		MultiPoint pt2 = (MultiPoint) in.read(getReader(),geometryFactory);

//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input MultiPoint is not the same as the output MultiPoint",pt.equals(pt2));
	}
}
