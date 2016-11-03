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

package org.locationtech.jts.algorithm;

import java.util.Random;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;



public class PerturbedGridPolygonBuilder 
{
	private GeometryFactory geomFactory;
	private double gridWidth = 1000;
	private int numLines = 10;
	private double lineWidth = 20;
		
	private long seed = 0; 
	private Random rand;
	
	private Geometry grid;
	
	public PerturbedGridPolygonBuilder(GeometryFactory geomFactory)
	{
		this.geomFactory = geomFactory;
		seed = System.currentTimeMillis();
	}
	
	public void setSeed(long seed)
	{
		this.seed = seed;
	}
	
	public void setNumLines(int numLines)
	{
		this.numLines = numLines;
	}
	
	public void setLineWidth(double lineWidth)
	{
		this.lineWidth = lineWidth;
	}
	
	public Geometry getGeometry()
	{
		if (grid == null)
			grid = buildGrid();
		return grid;
	}
	
	private Geometry buildGrid()
	{
		LineString[] lines = new LineString[numLines * 2];
		int index = 0;
		
		for (int i = 0; i < numLines; i++) {
			Coordinate p0 = new Coordinate(getRandOrdinate(), 0);
			Coordinate p1 = new Coordinate(getRandOrdinate(), gridWidth);
			LineString line = geomFactory.createLineString(
					new Coordinate[] { p0, p1 });
			lines[index++] = line;
		}
		
		for (int i = 0; i < numLines; i++) {
			Coordinate p0 = new Coordinate(0, getRandOrdinate());
			Coordinate p1 = new Coordinate(gridWidth, getRandOrdinate());
			LineString line = geomFactory.createLineString(
					new Coordinate[] { p0, p1 });
			lines[index++] = line;
		}
		
		MultiLineString ml = geomFactory.createMultiLineString(lines);
		Geometry grid = ml.buffer(lineWidth);
		//System.out.println(grid);
		return grid;
		
	}
	
	private double getRand()
	{
		if (rand == null) {
			//System.out.println("Seed = " + seed);
			rand = new Random(seed);
		}
		return rand.nextDouble();
	}
	

	private double getRandOrdinate()
	{
		double randNum = getRand();
		double ord = geomFactory.getPrecisionModel().makePrecise(randNum * gridWidth);
		return ord;
	}

}
