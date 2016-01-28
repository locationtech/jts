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

package org.locationtech.jts.algorithm;

import java.util.Random;

import org.locationtech.jts.geom.*;



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
		System.out.println(grid);
		return grid;
		
	}
	
	private double getRand()
	{
		if (rand == null) {
			System.out.println("Seed = " + seed);
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
