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
package com.vividsolutions.jts.io.oracle;

import java.sql.SQLException;

import oracle.sql.STRUCT;

import com.vividsolutions.jts.generator.*;
import com.vividsolutions.jts.geom.*;

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
		
		OraWriter ow = new OraWriter(getConnection());
		STRUCT st = ow.write(pt);
		
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
		OraWriter ow = new OraWriter(getConnection());
		
		int i=0;
		while(grid.canCreate() && i<100){
			pg.setBoundingBox(grid.createEnv());
			pt[i] = (LineString) pg.create();
			st[i] = ow.write(pt[i]);
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
		OraWriter ow = new OraWriter(getConnection());
		
		int i=0;
		while(grid.canCreate() && i<8){
			pg.setBoundingBox(grid.createEnv());
			pt[i] = (LineString) pg.create();
			st[i] = ow.write(pt[i]);
			i++;
		}
		for(int j=0;j<4;j++){
			if(pt[j]!=null)
				st[i++] = ow.write(pt[j]);
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
		
		OraWriter ow = new OraWriter(getConnection());
		STRUCT st = ow.write(pt);
		
		OraReader or = new OraReader();
		LineString pt2 = (LineString) or.read(st);

//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input LineString is not the same as the output LineString",pt.equals(pt2));
	}
}
