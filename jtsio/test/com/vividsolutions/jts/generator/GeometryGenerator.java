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
package com.vividsolutions.jts.generator;

import com.vividsolutions.jts.geom.*;

/**
 * This class illustrates the basic functionality and configuration options for generating spatial data. 
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public abstract class GeometryGenerator {
	protected int dimensions = 2;
	protected GeometryFactory geometryFactory; // includes srid
	protected Envelope boundingBox;
	
	/**
	 * @return A Geometry which uses some or all of the Bounding Box specified.
	 */
	public abstract Geometry create();

	
	/**
	 * @see GridGenerator
	 * @return A new GridGenerator
	 */
	public static GridGenerator createGridGenerator(){
		return new GridGenerator();
	}
	
	/**
	 * @see PointGenerator
	 * @return A new PointGenerator
	 */
	public static PointGenerator createPointGenerator(){
		return new PointGenerator();
	}

	/**
	 * @see LineStringGenerator
	 * @return A new LineStringGenerator
	 */
	public static LineStringGenerator createLineStringGenerator(){
		LineStringGenerator lsg = new LineStringGenerator();
		lsg.setGenerationAlgorithm(LineStringGenerator.ARC);
		lsg.setNumberPoints(10);
		return lsg;
	}

	/**
	 * @see PolygonGenerator
	 * @return A new PolygonGenerator
	 */
	public static PolygonGenerator createPolygonGenerator(){
		PolygonGenerator pg = new PolygonGenerator();
		pg.setGenerationAlgorithm(PolygonGenerator.ARC);
		pg.setNumberPoints(10);
		pg.setNumberHoles(8);
		return pg;
	}

	/**
	 * @see PointGenerator
	 * @see MultiGenerator
	 * @return A new MultiGenerator
	 */
	public static MultiGenerator createMultiPointGenerator(){
		MultiGenerator mg = new MultiGenerator(createPointGenerator());
		mg.setNumberGeometries(4);
		return mg;
	}

	/**
	 * @see LineStringGenerator
	 * @see MultiGenerator
	 * @return A new PointGenerator
	 */
	public static MultiGenerator createMultiLineStringGenerator(){
		MultiGenerator mg = new MultiGenerator(createLineStringGenerator());
		mg.setNumberGeometries(4);
		return mg;
	}

	/**
	 * @see PolygonGenerator
	 * @see MultiGenerator
	 * @return A new PointGenerator
	 */
	public static MultiGenerator createMultiPolygonGenerator(){
		MultiGenerator mg = new MultiGenerator(createPolygonGenerator());
		mg.setNumberGeometries(4);
		return mg;
	}

	/**
	 * @return Returns the boundingBox.
	 */
	public Envelope getBoundingBox() {
		return boundingBox;
	}

	/**
	 * @param boundingBox The boundingBox to set.
	 */
	public void setBoundingBox(Envelope boundingBox) {
		this.boundingBox = boundingBox;
	}

	/**
	 * @return Returns the dimensions.
	 */
	public int getDimensions() {
		return dimensions;
	}

	/**
	 * @param dimensions The dimensions to set.
	 */
	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
		throw new RuntimeException("Dimensions other than 2 are not yet supported");
	}

	/**
	 * @return Returns the geometryFactory.
	 */
	public GeometryFactory getGeometryFactory() {
		return geometryFactory;
	}

	/**
	 * @param geometryFactory The geometryFactory to set.
	 */
	public void setGeometryFactory(GeometryFactory geometryFactory) {
		this.geometryFactory = geometryFactory;
	}
	
	
}
