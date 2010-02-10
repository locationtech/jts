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
package com.vividsolutions.jts.awt;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A factory for generating square shapes to represent points.
 * 
 * @author Martin Davis
 *
 */
public class SqarePointShapeFactory 
implements PointShapeFactory
{
	/**
	 * The default length of the square's side.
	 */
	public static double DEFAULT_SIZE = 3.0;
	
	private double squareSize = DEFAULT_SIZE;
	
	/**
	 * Creates a new factory for squares with default size.
	 *
	 */
	public SqarePointShapeFactory()
	{
	}
	
	/**
	 * Creates a factory for squares of given size.
	 * 
	 * @param squareSize the length of the side of the square
	 */
	public SqarePointShapeFactory(double squareSize)
	{
		this.squareSize = squareSize;
	}
	
	/**
	 * Creates a shape representing a point.
	 * 
	 * @param point the location of the point
	 * @return a shape
	 */
	public Shape createPoint(Point2D point)
	{
		Rectangle2D.Double pointMarker =
			new Rectangle2D.Double(
				0.0,
				0.0,
				squareSize,
				squareSize);
		pointMarker.x = (double) (point.getX() - (squareSize / 2));
		pointMarker.y = (double) (point.getY() - (squareSize / 2));

		return pointMarker;

	}
}
