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
package com.vividsolutions.jts.generator;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.*;

/**
 * 
 * Cascades the effort of creating a set of topologically valid geometries.
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class MultiGenerator extends GeometryGenerator {

	private GeometryGenerator generator = null;
	private int numberGeometries = 2;
	private int generationAlgorithm = 0;
	
	/**
	 * Grid style blocks
	 */
	public static final int BOX = 0;
	/**
	 * vertical strips
	 */
	public static final int VERT = 1;
	/**
	 * Horizontal strips
	 */
	public static final int HORZ = 2;
	
	/**
	 * @param generator
	 */
	public MultiGenerator(GeometryGenerator generator) {
		this.generator = generator;
	}

	/**
	 * Creates a geometry collection representing the set of child geometries created.
	 * 
	 * @see #setNumberGeometries(int)
	 * @see com.vividsolutions.jts.generator.GeometryGenerator#create()
	 * 
	 * @see #BOX
	 * @see #VERT
	 * @see #HORZ
	 * 
	 * @throws NullPointerException when the generator is missing
	 * @throws IllegalStateException when the number of child geoms is too small
	 * @throws IllegalStateException when the selected alg. is invalid
	 */
	public Geometry create() {
		if(generator == null)
			throw new NullPointerException("Missing child generator");
		
		if(numberGeometries < 1)
			throw new IllegalStateException("Too few child geoms to create");
		
		ArrayList geoms = new ArrayList(numberGeometries);

		GridGenerator grid = GeometryGenerator.createGridGenerator();
		grid.setBoundingBox(boundingBox);
		grid.setGeometryFactory(geometryFactory);
		
		switch(generationAlgorithm){
		case BOX:

			int nrow = (int)Math.sqrt(numberGeometries);
			int ncol = numberGeometries/nrow;
			grid.setNumberRows(nrow);
			grid.setNumberColumns(ncol);
			
			break;
		case VERT:

			grid.setNumberRows(1);
			grid.setNumberColumns(numberGeometries);
			
			break;
		case HORZ:

			grid.setNumberRows(numberGeometries);
			grid.setNumberColumns(1);
			
			break;
		default:
			throw new IllegalStateException("Invalid Alg. Specified");
		}
		
		while(grid.canCreate()){
			generator.setBoundingBox(grid.createEnv());
			geoms.add(generator.create());
		}
		
		// yes ... there are better ways
		if(generator instanceof PointGenerator){
			return geometryFactory.createMultiPoint((Point[]) geoms.toArray(new Point[numberGeometries]));
		}else{
		if(generator instanceof LineStringGenerator){
			return geometryFactory.createMultiLineString((LineString[]) geoms.toArray(new LineString[numberGeometries]));
		}else{
		if(generator instanceof PolygonGenerator){
			return geometryFactory.createMultiPolygon((Polygon[]) geoms.toArray(new Polygon[numberGeometries]));
		}else{
			// same as multi
			return geometryFactory.createGeometryCollection((Geometry[]) geoms.toArray(new Geometry[numberGeometries]));
		}}}
	}

	/**
	 * @return Returns the numberGeometries.
	 */
	public int getNumberGeometries() {
		return numberGeometries;
	}

	/**
	 * @param numberGeometries The numberGeometries to set.
	 */
	public void setNumberGeometries(int numberGeometries) {
		this.numberGeometries = numberGeometries;
	}

	/**
	 * @return Returns the generator.
	 */
	public GeometryGenerator getGenerator() {
		return generator;
	}

	/**
	 * @see com.vividsolutions.jts.generator.GeometryGenerator#setBoundingBox(com.vividsolutions.jts.geom.Envelope)
	 */
	public void setBoundingBox(Envelope boundingBox) {
		super.setBoundingBox(boundingBox);
		if(generator!=null)
			generator.setBoundingBox(boundingBox);
	}

	/**
	 * @see com.vividsolutions.jts.generator.GeometryGenerator#setDimensions(int)
	 */
	public void setDimensions(int dimensions) {
		super.setDimensions(dimensions);
		if(generator!=null)
			generator.setDimensions(dimensions);
	}

	/**
	 * @see com.vividsolutions.jts.generator.GeometryGenerator#setGeometryFactory(com.vividsolutions.jts.geom.GeometryFactory)
	 */
	public void setGeometryFactory(GeometryFactory geometryFactory) {
		super.setGeometryFactory(geometryFactory);
		if(generator!=null)
			generator.setGeometryFactory(geometryFactory);
	}
}
