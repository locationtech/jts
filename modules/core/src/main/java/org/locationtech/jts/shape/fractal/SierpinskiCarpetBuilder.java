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

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.shape.GeometricShapeBuilder;


public class SierpinskiCarpetBuilder 
extends GeometricShapeBuilder
{
	private CoordinateList coordList = new CoordinateList();
	
	public SierpinskiCarpetBuilder(GeometryFactory geomFactory)
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
		Coordinate origin = baseLine.getCoordinate(0);
		LinearRing[] holes = getHoles(level, origin.x, origin.y, getDiameter());
		LinearRing shell = (LinearRing) ((Polygon) geomFactory.toGeometry(getSquareExtent())).getExteriorRing();
		return geomFactory.createPolygon(
				shell, holes);
	}
	
	private LinearRing[] getHoles(int n, double originX, double originY, double width) 
	{
		List holeList = new ArrayList();
		
		addHoles(n, originX, originY, width, holeList );
		
		return GeometryFactory.toLinearRingArray(holeList);
	}

	private void addHoles(int n, double originX, double originY, double width, List holeList) 
	{
		if (n < 0) return;
		int n2 = n - 1;
		double widthThird = width / 3.0;
		double widthTwoThirds = width * 2.0 / 3.0;
		double widthNinth = width / 9.0;
		addHoles(n2, originX, 									originY, widthThird, holeList);
		addHoles(n2, originX + widthThird, 			originY, widthThird, holeList);
		addHoles(n2, originX + 2 * widthThird, 	originY, widthThird, holeList);
		
		addHoles(n2, originX, 									originY + widthThird, widthThird, holeList);
		addHoles(n2, originX + 2 * widthThird, 	originY + widthThird, widthThird, holeList);

		addHoles(n2, originX, 									originY + 2 * widthThird, widthThird, holeList);
		addHoles(n2, originX + widthThird, 			originY + 2 * widthThird, widthThird, holeList);
		addHoles(n2, originX + 2 * widthThird, 	originY + 2 * widthThird, widthThird, holeList);

		// add the centre hole
		holeList.add(createSquareHole(originX + widthThird, originY + widthThird, widthThird));
	}

	private LinearRing createSquareHole(double x, double y, double width)
	{
		Coordinate[] pts = new Coordinate[]{
        new Coordinate(x, y),
        new Coordinate(x + width, y),
        new Coordinate(x + width, y + width),
        new Coordinate(x, y + width),
        new Coordinate(x, y)
        }	;
		return geomFactory.createLinearRing(pts); 
	}
	

}
