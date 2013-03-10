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
 * NOTE: The points may be re-ordered during these tests. 
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class StaticMultiPolygonTest extends ConnectedTestCase {

	/**
	 * @param arg
	 */
	public StaticMultiPolygonTest(String arg) {
		super(arg);
	}

	/**
	 * Round Trip test for a single MultiPolygon
	 * @throws SQLException 
	 */
	public void testSingleMultiPolygonNoHoleRoundTrip() throws SQLException{
		PolygonGenerator pgc = new PolygonGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setNumberPoints(10);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		MultiPolygon pt = (MultiPolygon) pg.create();
		
		OraWriter ow = new OraWriter(getConnection());
		STRUCT st = ow.write(pt);
		
		OraReader or = new OraReader();
		MultiPolygon pt2 = (MultiPolygon) or.read(st);
		
//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input MultiPolygon is not the same as the output MultiPolygon",pt.equals(pt2));
	}

	/**
	 * Round Trip test for a 100 non overlapping MultiPolygon
	 * @throws SQLException 
	 */
	public void testGridMultiPolygonsNoHoleRoundTrip() throws SQLException{
		GridGenerator grid = new GridGenerator();
		grid.setGeometryFactory(geometryFactory);
		grid.setBoundingBox(new Envelope(0,10,0,10));
		grid.setNumberColumns(10);
		grid.setNumberRows(10);
		
		MultiPolygon[] pt = new MultiPolygon[100];
		STRUCT[] st = new STRUCT[100];

		PolygonGenerator pgc = new PolygonGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setNumberPoints(10);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		OraWriter ow = new OraWriter(getConnection());
		
		int i=0;
		while(grid.canCreate() && i<100){
			pg.setBoundingBox(grid.createEnv());
			pt[i] = (MultiPolygon) pg.create();
			st[i] = ow.write(pt[i]);
			i++;
		}
		
		OraReader or = new OraReader();
		i=0;
		while(i<100 && pt[i] != null){
			MultiPolygon pt2 = (MultiPolygon) or.read(st[i]);
//			System.out.println((pt[i]==null?"NULL":pt[i].toString()));
//			System.out.println((pt2==null?"NULL":pt2.toString()));
			assertTrue("The input MultiPolygon is not the same as the output MultiPolygon",pt[i].equals(pt2));
			i++;
		}
	}

	/**
	 * Round Trip test for a 8 overlapping line MultiPolygons (4 distinct MultiPolygons)
	 * @throws SQLException 
	 */
	public void testOverlappingMultiPolygonsNoHoleRoundTrip() throws SQLException{
		GridGenerator grid = new GridGenerator();
		grid.setGeometryFactory(geometryFactory);
		grid.setBoundingBox(new Envelope(0,10,0,10));
		grid.setNumberColumns(2);
		grid.setNumberRows(2);
		
		MultiPolygon[] pt = new MultiPolygon[4];
		STRUCT[] st = new STRUCT[8];

		PolygonGenerator pgc = new PolygonGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setNumberPoints(10);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		OraWriter ow = new OraWriter(getConnection());
		
		int i=0;
		while(grid.canCreate() && i<8){
			pg.setBoundingBox(grid.createEnv());
			pt[i] = (MultiPolygon) pg.create();
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
			MultiPolygon pt2 = (MultiPolygon) or.read(st[i]);
//			System.out.println((pt==null?"NULL":pt[i%4].toString()));
//			System.out.println((pt2==null?"NULL":pt2.toString()));
			assertTrue("The input MultiPolygon is not the same as the output MultiPolygon",pt[i%4].equals(pt2));
			i++;
		}
	}

	/**
	 * Round Trip test for a single MultiPolygon with lotsa points
	 * @throws SQLException 
	 */
	public void testSingleMultiPolygonManyPointsNoHoleRoundTrip() throws SQLException{

		PolygonGenerator pgc = new PolygonGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setGenerationAlgorithm(PolygonGenerator.BOX);
		pgc.setNumberPoints(1000);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		MultiPolygon pt = (MultiPolygon) pg.create();
//		System.out.println((pt==null?"NULL":pt.toString()));
		
		OraWriter ow = new OraWriter(getConnection());
		STRUCT st = ow.write(pt);
		
		OraReader or = new OraReader();
		MultiPolygon pt2 = (MultiPolygon) or.read(st);

//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input MultiPolygon is not the same as the output MultiPolygon",pt.equals(pt2));
	}

	/**
	 * Round Trip test for a single MultiPolygon
	 * @throws SQLException 
	 */
	public void testSingleMultiPolygonHolesRoundTrip() throws SQLException{

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
		
		OraWriter ow = new OraWriter(getConnection());
		STRUCT st = ow.write(pt);
		
		OraReader or = new OraReader();
		MultiPolygon pt2 = (MultiPolygon) or.read(st);
		
//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input MultiPolygon is not the same as the output MultiPolygon",pt.equals(pt2));
	}

	/**
	 * Round Trip test for a 100 non overlapping MultiPolygon
	 * @throws SQLException 
	 */
	public void testGridMultiPolygonsHolesRoundTrip() throws SQLException{
		GridGenerator grid = new GridGenerator();
		grid.setGeometryFactory(geometryFactory);
		grid.setBoundingBox(new Envelope(0,10,0,10));
		grid.setNumberColumns(10);
		grid.setNumberRows(10);
		
		MultiPolygon[] pt = new MultiPolygon[100];
		STRUCT[] st = new STRUCT[100];


		PolygonGenerator pgc = new PolygonGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setGenerationAlgorithm(PolygonGenerator.BOX);
		pgc.setNumberPoints(10);
		pgc.setNumberHoles(4);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		OraWriter ow = new OraWriter(getConnection());
		
		int i=0;
		while(grid.canCreate() && i<100){
			pg.setBoundingBox(grid.createEnv());
			pt[i] = (MultiPolygon) pg.create();
			st[i] = ow.write(pt[i]);
			i++;
		}
		
		OraReader or = new OraReader();
		i=0;
		while(i<100 && pt[i] != null){
			MultiPolygon pt2 = (MultiPolygon) or.read(st[i]);
//			System.out.println((pt[i]==null?"NULL":pt[i].toString()));
//			System.out.println((pt2==null?"NULL":pt2.toString()));
			assertTrue("The input MultiPolygon is not the same as the output MultiPolygon",pt[i].equals(pt2));
			i++;
		}
	}

	/**
	 * Round Trip test for a 8 overlapping line MultiPolygons (4 distinct MultiPolygons)
	 * @throws SQLException 
	 */
	public void testOverlappingMultiPolygonsHolesRoundTrip() throws SQLException{
		GridGenerator grid = new GridGenerator();
		grid.setGeometryFactory(geometryFactory);
		grid.setBoundingBox(new Envelope(0,10,0,10));
		grid.setNumberColumns(2);
		grid.setNumberRows(2);
		
		MultiPolygon[] pt = new MultiPolygon[4];
		STRUCT[] st = new STRUCT[8];


		PolygonGenerator pgc = new PolygonGenerator();
		pgc.setGeometryFactory(geometryFactory);
		pgc.setGenerationAlgorithm(PolygonGenerator.BOX);
		pgc.setNumberPoints(10);
		pgc.setNumberHoles(4);
		MultiGenerator pg = new MultiGenerator(pgc);
		pg.setBoundingBox(new Envelope(0,10,0,10));
		pg.setNumberGeometries(3);
		pg.setGeometryFactory(geometryFactory);
		
		OraWriter ow = new OraWriter(getConnection());
		
		int i=0;
		while(grid.canCreate() && i<8){
			pg.setBoundingBox(grid.createEnv());
			pt[i] = (MultiPolygon) pg.create();
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
			MultiPolygon pt2 = (MultiPolygon) or.read(st[i]);
//			System.out.println((pt==null?"NULL":pt[i%4].toString()));
//			System.out.println((pt2==null?"NULL":pt2.toString()));
			assertTrue("The input MultiPolygon is not the same as the output MultiPolygon",pt[i%4].equals(pt2));
			i++;
		}
	}

	/**
	 * Round Trip test for a single MultiPolygon with lotsa points
	 * @throws SQLException 
	 */
	public void testSingleMultiPolygonManyPointsHolesRoundTrip() throws SQLException{

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
		
		OraWriter ow = new OraWriter(getConnection());
		STRUCT st = ow.write(pt);
		
		OraReader or = new OraReader();
		MultiPolygon pt2 = (MultiPolygon) or.read(st);

//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input MultiPolygon is not the same as the output MultiPolygon",pt.equals(pt2));
	}

	/**
	 * Round Trip test for a single MultiPolygon with lotsa points
	 * @throws SQLException 
	 */
	public void testSingleMultiPolygonManyPointsManyHolesRoundTrip() throws SQLException{

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
		
		OraWriter ow = new OraWriter(getConnection());
		STRUCT st = ow.write(pt);
		
		OraReader or = new OraReader();
		MultiPolygon pt2 = (MultiPolygon) or.read(st);

//		System.out.println((pt==null?"NULL":pt.toString()));
//		System.out.println((pt2==null?"NULL":pt2.toString()));
		assertTrue("The input MultiPolygon is not the same as the output MultiPolygon",pt.equals(pt2));
	}
}
