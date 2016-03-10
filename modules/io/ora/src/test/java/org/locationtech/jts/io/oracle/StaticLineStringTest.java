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
package org.locationtech.jts.io.oracle;

import java.sql.SQLException;

import org.locationtech.jts.generator.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.oracle.OraReader;
import org.locationtech.jts.io.oracle.OraWriter;

import oracle.sql.STRUCT;


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
public class StaticLineStringTest extends ConnectedTestCase {

	/**
	 * @param arg
	 */
	public StaticLineStringTest(String arg) {
		super(arg);
	}

	/**
	 * Round Trip test for a single line string
	 * @throws SQLException 
	 */
	public void testSingleLineStringRoundTrip() throws SQLException{
		LineStringGenerator pg = new LineStringGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberPoints(10);
		
		LineString pt = (LineString) pg.create();
		
		OraWriter ow = new OraWriter();
		STRUCT st = ow.write(pt, getConnection());
		
		OraReader or = new OraReader();
		LineString pt2 = (LineString) or.read(st);
		
//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input LineString is not the same as the output LineString",pt.equals(pt2));
	}

	/**
	 * Round Trip test for a 100 non overlapping line strings
	 * @throws SQLException 
	 */
	public void testGridLineStringsRoundTrip() throws SQLException{
		GridGenerator grid = new GridGenerator();
		grid.setGeometryFactory(geometryFactory);
		grid.setBoundingBox(new Envelope(0,10,0,10));
		grid.setNumberColumns(10);
		grid.setNumberRows(10);
		
		LineString[] pt = new LineString[100];
		STRUCT[] st = new STRUCT[100];

		LineStringGenerator pg = new LineStringGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setNumberPoints(10);
		OraWriter ow = new OraWriter();
		
		int i=0;
		while(grid.canCreate() && i<100){
			pg.setBoundingBox(grid.createEnv());
			pt[i] = (LineString) pg.create();
			st[i] = ow.write(pt[i], getConnection());
			i++;
		}
		
		OraReader or = new OraReader();
		i=0;
		while(i<100 && pt[i] != null){
			LineString pt2 = (LineString) or.read(st[i]);
//			System.out.println((pt[i]==null?"NULL":pt[i].toString()));
//			System.out.println((pt2==null?"NULL":pt2.toString()));
			assertTrue("The input LineString is not the same as the output LineString",pt[i].equals(pt2));
			i++;
		}
	}

	/**
	 * Round Trip test for a 8 overlapping line strings (4 distinct line strings)
	 * @throws SQLException 
	 */
	public void testOverlappingLineStringsRoundTrip() throws SQLException{
		GridGenerator grid = new GridGenerator();
		grid.setGeometryFactory(geometryFactory);
		grid.setBoundingBox(new Envelope(0,10,0,10));
		grid.setNumberColumns(2);
		grid.setNumberRows(2);
		
		LineString[] pt = new LineString[4];
		STRUCT[] st = new STRUCT[8];

		LineStringGenerator pg = new LineStringGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setNumberPoints(10);
		OraWriter ow = new OraWriter();
		
		int i=0;
		while(grid.canCreate() && i<8){
			pg.setBoundingBox(grid.createEnv());
			pt[i] = (LineString) pg.create();
			st[i] = ow.write(pt[i], getConnection());
			i++;
		}
		for(int j=0;j<4;j++){
			if(pt[j]!=null)
				st[i++] = ow.write(pt[j], getConnection());
		}
		
		OraReader or = new OraReader();
		i=0;
		while(i<8 && pt[i%4] != null){
			LineString pt2 = (LineString) or.read(st[i]);
//			System.out.println((pt==null?"NULL":pt[i%4].toString()));
//			System.out.println((pt2==null?"NULL":pt2.toString()));
			assertTrue("The input LineString is not the same as the output LineString",pt[i%4].equals(pt2));
			i++;
		}
	}

	/**
	 * Round Trip test for a single line string with lotsa points
	 * @throws SQLException 
	 */
	public void testSingleLineStringManyPointRoundTrip() throws SQLException{
		LineStringGenerator pg = new LineStringGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setGenerationAlgorithm(LineStringGenerator.HORZ);
		pg.setNumberPoints(1000);
		
		LineString pt = (LineString) pg.create();
//		System.out.println((pt==null?"NULL":pt.toString()));
		
		OraWriter ow = new OraWriter();
		STRUCT st = ow.write(pt, getConnection());
		
		OraReader or = new OraReader();
		LineString pt2 = (LineString) or.read(st);

//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input LineString is not the same as the output LineString",pt.equals(pt2));
	}
}
