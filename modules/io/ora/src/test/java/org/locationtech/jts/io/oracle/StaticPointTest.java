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

import org.locationtech.jts.generator.GridGenerator;
import org.locationtech.jts.generator.PointGenerator;
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
 * @author David Zwiers, Vivid Solutions. 
 */
public class StaticPointTest extends ConnectedTestCase {

	/**
	 * @param arg
	 */
	public StaticPointTest(String arg) {
		super(arg);
	}

	/**
	 * Round Trip test for a single point
	 * @throws SQLException 
	 */
	public void testSinglePointRoundTrip() throws SQLException{
		PointGenerator pg = new PointGenerator();
		pg.setGeometryFactory(geometryFactory);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		
		Point pt = (Point) pg.create();
		
		OraWriter ow = new OraWriter();
		STRUCT st = ow.write(pt, getConnection());
		
		OraReader or = new OraReader();
		Point pt2 = (Point) or.read(st);

//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input Point is not the same as the output Point",pt.equals(pt2));
	}

	/**
	 * Round Trip test for a 100 non overlapping points
	 * @throws SQLException 
	 */
	public void testGridPointsRoundTrip() throws SQLException{
		GridGenerator grid = new GridGenerator();
		grid.setGeometryFactory(geometryFactory);
		grid.setBoundingBox(new Envelope(0,10,0,10));
		grid.setNumberColumns(10);
		grid.setNumberRows(10);
		
		Point[] pt = new Point[100];
		STRUCT[] st = new STRUCT[100];

		PointGenerator pg = new PointGenerator();
		pg.setGeometryFactory(geometryFactory);
		OraWriter ow = new OraWriter();
		
		int i=0;
		while(grid.canCreate() && i<100){
			pg.setBoundingBox(grid.createEnv());
			pt[i] = (Point) pg.create();
			st[i] = ow.write(pt[i], getConnection());
			i++;
		}
		
		OraReader or = new OraReader();
		i=0;
		while(i<100 && pt[i] != null){
			Point pt2 = (Point) or.read(st[i]);
//			System.out.println((pt[i]==null?"NULL":pt[i].toString()));
//			System.out.println((pt2==null?"NULL":pt2.toString()));
			assertTrue("The input Point is not the same as the output Point",pt[i].equals(pt2));
			i++;
		}
	}

	/**
	 * Round Trip test for a 8 overlapping points (4 distinct points)
	 * @throws SQLException 
	 */
	public void testOverlappingPointsRoundTrip() throws SQLException{
		GridGenerator grid = new GridGenerator();
		grid.setGeometryFactory(geometryFactory);
		grid.setBoundingBox(new Envelope(0,10,0,10));
		grid.setNumberColumns(2);
		grid.setNumberRows(2);
		
		Point[] pt = new Point[4];
		STRUCT[] st = new STRUCT[8];

		PointGenerator pg = new PointGenerator();
		pg.setGeometryFactory(geometryFactory);
		OraWriter ow = new OraWriter();
		
		int i=0;
		while(grid.canCreate() && i<8){
			pg.setBoundingBox(grid.createEnv());
			pt[i] = (Point) pg.create();
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
			Point pt2 = (Point) or.read(st[i]);
//			System.out.println((pt[i]==null?"NULL":pt[i].toString()));
//			System.out.println((pt2==null?"NULL":pt2.toString()));
			assertTrue("The input Point is not the same as the output Point",pt[i%4].equals(pt2));
			i++;
		}
	}
}
