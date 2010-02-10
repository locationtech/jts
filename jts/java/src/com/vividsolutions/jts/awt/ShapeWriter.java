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
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.*;


/**
 * Writes {@link Geometry}s into Java2D {@link Shape} objects
 */
public class ShapeWriter 
{
	/**
	 * The point transformation used by default.
	 */
	public static final PointTransformation DEFAULT_POINT_TRANSFORMATION = new IdentityPointTransformation();
	
	/**
	 * The point shape factory used by default.
	 */
	public static final PointShapeFactory DEFAULT_POINT_FACTORY = new SqarePointShapeFactory(3.0);
	
	private PointTransformation pointTransformer = DEFAULT_POINT_TRANSFORMATION;
	private PointShapeFactory pointFactory = DEFAULT_POINT_FACTORY;

	/**
	 * Creates a new ShapeWriter with a specified point transformation
	 * and point shape factory.
	 * 
	 * @param pointTransformer a transformation from model to view space to use 
	 * @param pointFactory the PointShapeFactory to use
	 */
	public ShapeWriter(PointTransformation pointTransformer, PointShapeFactory pointFactory) 
	{
		if (pointTransformer != null)
			this.pointTransformer = pointTransformer;
		if (pointFactory != null)
			this.pointFactory = pointFactory;
	}

	/**
	 * Creates a new ShapeWriter with a specified point transformation
	 * and the default point shape factory.
	 * 
	 * @param pointTransformer a transformation from model to view space to use 
	 */
	public ShapeWriter(PointTransformation pointTransformer) 
	{
		this(pointTransformer, null);
	}

	/**
	 * Creates a new ShapeWriter with the default (identity) point transformation.
	 *
	 */
	public ShapeWriter() {
	}

	/**
	 * Creates a {@link Shape} representing a {@link Geometry}, 
	 * according to the specified PointTransformation
	 * and PointShapeFactory (if relevant).
	 * <p>
	 * Note that Shapes do not
	 * preserve information about which elements in heterogeneous collections
	 * are 1D and which are 2D.
	 * For example, a GeometryCollection containing a ring and a
	 * disk will render as two disks if Graphics.fill is used, 
	 * or as two rings if Graphics.draw is used.
	 * To avoid this issue use separate shapes for the components.
	 * 
	 * @param geometry the geometry to convert
	 * @return a Shape representing the geometry
	 */
	public Shape toShape(Geometry geometry)
	{
		if (geometry.isEmpty()) return new GeneralPath();
		if (geometry instanceof Polygon) return toShape((Polygon) geometry);
		if (geometry instanceof LineString) 			return toShape((LineString) geometry);
		if (geometry instanceof MultiLineString) 	return toShape((MultiLineString) geometry);
		if (geometry instanceof Point) 			return toShape((Point) geometry);
		if (geometry instanceof GeometryCollection) return toShape((GeometryCollection) geometry);

		throw new IllegalArgumentException(
			"Unrecognized Geometry class: " + geometry.getClass());
	}

	private Shape toShape(Polygon p) 
	{
		ArrayList holeVertexCollection = new ArrayList();

		for (int j = 0; j < p.getNumInteriorRing(); j++) {
			holeVertexCollection.add(
				toViewCoordinates(p.getInteriorRingN(j).getCoordinates()));
		}

		return new PolygonShape(
			toViewCoordinates(p.getExteriorRing().getCoordinates()),
			holeVertexCollection);
	}

	private Coordinate[] toViewCoordinates(Coordinate[] modelCoordinates)
	{
		Coordinate[] viewCoordinates = new Coordinate[modelCoordinates.length];

		for (int i = 0; i < modelCoordinates.length; i++) {
			Point2D point2D = toPoint(modelCoordinates[i]);
			viewCoordinates[i] = new Coordinate(point2D.getX(), point2D.getY());
		}

		return viewCoordinates;
	}

	private Shape toShape(GeometryCollection gc)
	{
		GeometryCollectionShape shape = new GeometryCollectionShape();

		for (int i = 0; i < gc.getNumGeometries(); i++) {
			Geometry g = (Geometry) gc.getGeometryN(i);
			shape.add(toShape(g));
		}

		return shape;
	}

	private GeneralPath toShape(MultiLineString mls)
	{
		GeneralPath path = new GeneralPath();

		for (int i = 0; i < mls.getNumGeometries(); i++) {
			LineString lineString = (LineString) mls.getGeometryN(i);
			path.append(toShape(lineString), false);
		}
		return path;
	}

	private GeneralPath toShape(LineString lineString)
	{
		GeneralPath shape = new GeneralPath();
		Point2D viewPoint = toPoint(lineString.getCoordinateN(0));
		shape.moveTo((float) viewPoint.getX(), (float) viewPoint.getY());

		for (int i = 1; i < lineString.getNumPoints(); i++) {
			viewPoint = toPoint(lineString.getCoordinateN(i));
			shape.lineTo((float) viewPoint.getX(), (float) viewPoint.getY());
		}
		return shape;
	}

	private Shape toShape(Point point)
  {
		Point2D viewPoint = toPoint(point.getCoordinate());
		return pointFactory.createPoint(viewPoint);
	}

  private Point2D toPoint(Coordinate model)
    {
    Point2D view = new Point2D.Double();
    pointTransformer.transform(model, view);
    /**
     * Do the rounding now instead of relying on Java 2D rounding.
     * Java2D seems to do rounding differently for drawing and filling, resulting in the draw
     * being a pixel off from the fill sometimes.
     */
    view.setLocation(Math.round(view.getX()), Math.round(view.getY()));
    return view;
  }
}
