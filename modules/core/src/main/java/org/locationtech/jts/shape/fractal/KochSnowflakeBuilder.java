/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.shape.fractal;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.math.Vector2D;
import org.locationtech.jts.shape.GeometricShapeBuilder;

public class KochSnowflakeBuilder 
extends GeometricShapeBuilder
{
	private CoordinateList coordList = new CoordinateList();
	
	public KochSnowflakeBuilder(GeometryFactory geomFactory)
	{
		super(geomFactory);
	}
	
	public static int recursionLevelForSize(int numPts)
	{
		double pow4 = numPts / 3;
		double exp = Math.log(pow4)/Math.log(4);
		return (int) exp;
	}
	
	public Geometry getGeometry()
	{
		int level = recursionLevelForSize(numPts);
		LineSegment baseLine = getSquareBaseLine();
		Coordinate[] pts = getBoundary(level, baseLine.getCoordinate(0), baseLine.getLength());
		return geomFactory.createPolygon(
				geomFactory.createLinearRing(pts), null);
	}
	
	/**
	 * The height of an equilateral triangle of side one
	 */
	private static final double HEIGHT_FACTOR = Math.sin(Math.PI / 3.0);
	private static final double ONE_THIRD = 1.0/3.0;
	private static final double THIRD_HEIGHT = HEIGHT_FACTOR/3.0;
	private static final double TWO_THIRDS = 2.0/3.0;
	
	private Coordinate[] getBoundary(int level, Coordinate origin, double width) 
	{
		double y = origin.y;
		// for all levels beyond 0 need to vertically shift shape by height of one "arm" to centre it
		if (level > 0) {
			y += THIRD_HEIGHT * width;
		}
		
		Coordinate p0 = new Coordinate(origin.x, y);
		Coordinate p1 = new Coordinate(origin.x + width/2, y + width * HEIGHT_FACTOR);
		Coordinate p2 = new Coordinate(origin.x + width, y);
		addSide(level, p0, p1);
		addSide(level, p1, p2);
		addSide(level, p2, p0);
		coordList.closeRing();
		return coordList.toCoordinateArray();
	}

	public void addSide(int level, Coordinate p0, Coordinate p1) {
		if (level == 0)
			addSegment(p0, p1);
		else {
			Vector2D base = Vector2D.create(p0, p1);
			Coordinate midPt = base.multiply(0.5).translate(p0);
			
			Vector2D heightVec = base.multiply(THIRD_HEIGHT);
			Vector2D offsetVec = heightVec.rotateByQuarterCircle(1);
			Coordinate offsetPt = offsetVec.translate(midPt);
			
			int n2 = level - 1;
			Coordinate thirdPt = base.multiply(ONE_THIRD).translate(p0);
			Coordinate twoThirdPt = base.multiply(TWO_THIRDS).translate(p0);
			
			// construct sides recursively
			addSide(n2, p0, thirdPt);
			addSide(n2, thirdPt, offsetPt);
			addSide(n2, offsetPt, twoThirdPt);
			addSide(n2, twoThirdPt, p1);
		}
	}
		
	private void addSegment(Coordinate p0, Coordinate p1)
	{
		coordList.add(p1);
	}
	
}
