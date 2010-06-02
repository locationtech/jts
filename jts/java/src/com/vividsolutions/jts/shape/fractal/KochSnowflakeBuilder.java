package com.vividsolutions.jts.shape.fractal;

import com.vividsolutions.jts.algorithm.VectorMath;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.shape.*;

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
	private static double HEIGHT_FACTOR = Math.sin(Math.PI / 3.0);
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
			Coordinate midPt = VectorMath.average(p0, p1);
			
			Coordinate heightPt = VectorMath.pointAlong(p0, p1, THIRD_HEIGHT);
			Coordinate heightVec = VectorMath.difference(heightPt, p0); 
			Coordinate offsetVec = VectorMath.rotateByQuarterCircle(heightVec, 1);
			Coordinate offsetPt = VectorMath.sum(midPt, offsetVec);
			
			int n2 = level - 1;
			Coordinate thirdPt = VectorMath.pointAlong(p0, p1, ONE_THIRD);
			Coordinate twoThirdPt = VectorMath.pointAlong(p0, p1, TWO_THIRDS);
			
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
	
	private static Coordinate midPoint(Coordinate p0, Coordinate p1)
	{
    return new Coordinate( 
    		(p0.x + p1.x) / 2,
        (p0.y + p1.y) / 2);
	}
	
	private static Coordinate pointAlong(Coordinate p0, Coordinate p1, double segmentLengthFraction)
  {
    Coordinate coord = new Coordinate();
    coord.x = p0.x + segmentLengthFraction * (p1.x - p0.x);
    coord.y = p0.y + segmentLengthFraction * (p1.y - p0.y);
    return coord;
  }
  
	private static Coordinate vectorNormalize(Coordinate p0, Coordinate p1)
  {
  	return new Coordinate(p1.x - p0.x, p1.y - p0.y);
  }

	private static Coordinate vectorRotatePos90(Coordinate p)
  {
  	return new Coordinate(-p.y, p.x);
  }

	private static Coordinate add(Coordinate p0, Coordinate p1)
  {
  	return new Coordinate(p0.x + p1.x, p0.y + p1.y);
  }
}
