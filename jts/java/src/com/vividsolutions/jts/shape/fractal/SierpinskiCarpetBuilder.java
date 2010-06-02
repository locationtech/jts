package com.vividsolutions.jts.shape.fractal;

import java.util.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.shape.*;

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
