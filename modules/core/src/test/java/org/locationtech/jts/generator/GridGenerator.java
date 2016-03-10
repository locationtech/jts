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
package org.locationtech.jts.generator;

import java.util.NoSuchElementException;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;


/**
 * This class should be used to generate a grid of bounding boxes, 
 * most useful when creating multiple geometries.
 *
 * Successive calls to create() will walk the user though the grid. 
 * Use canCreate() and reset() to control the walk through the grid.
 * 
 * @see #canCreate()
 * @see #reset()
 * 
 * @author David Zwiers, Vivid Solutions. 
 */
public class GridGenerator extends GeometryGenerator {

	protected int numberColumns = 1;
	protected int numberRows = 1;
	protected int index = 0;
	
	/**
	 * Sets some default values.
	 */
	public GridGenerator(){
		dimensions = 2;
	}
	
	/**
	 * 
	 * @see org.locationtech.jts.generator.GeometryGenerator#create()
	 * 
	 * @throws NoSuchElementException when all the grids have been created (@see #create())
	 * @throws NullPointerException when either the Geometry Factory, or the Bounding Box are undefined.
	 */
	public Geometry create() {
		return geometryFactory.toGeometry(createEnv());
	}
	/**
	 * 
	 * @return Envelope 
	 * 
	 * @see org.locationtech.jts.generator.GeometryGenerator#create()
	 * 
	 * @throws NoSuchElementException when all the grids have been created (@see #create())
	 * @throws NullPointerException when either the Geometry Factory, or the Bounding Box are undefined.
	 */
	public Envelope createEnv() {
		if(!canCreate()){
			throw new NoSuchElementException("There are not any grids left to create.");
		}
		if(geometryFactory == null){
			throw new NullPointerException("GeometryFactory is not declared");
		}
		if(boundingBox == null || boundingBox.isNull()){
			throw new NullPointerException("Bounding Box is not declared");
		}

		double x = boundingBox.getMinX(); // base x
		double dx = boundingBox.getMaxX()-x;
		
		double y = boundingBox.getMinY(); // base y
		double dy = boundingBox.getMaxY()-y;
		
		int row = numberRows==1?0:index / numberRows;
		int col = numberColumns==1?0:index % numberColumns;
		
		double sx,sy; // size of a step
		sx = dx/numberColumns;
		sy = dy/numberRows;
		
		double minx, miny;
		minx = x+col*sx;
		miny = y+row*sy;
		
		Envelope box = new Envelope(geometryFactory.getPrecisionModel().makePrecise(minx),
									geometryFactory.getPrecisionModel().makePrecise(minx+sx),
									geometryFactory.getPrecisionModel().makePrecise(miny),
									geometryFactory.getPrecisionModel().makePrecise(miny+sy));
		
		index++;
		return box;
	}

	/**
	 * @return true when more grids exist
	 */
	public boolean canCreate(){
		return (numberColumns*numberRows)>index;
	}
	
	/**
	 * Resets the grid counter
	 */
	public void reset(){
		index = 0;
	}

	/**
	 * @see org.locationtech.jts.generator.GeometryGenerator#setDimensions(int)
	 */
	public void setDimensions(int dimensions) {
		if(dimensions!=2)
			throw new IllegalStateException("MAY NOT CHANGE GridGenerator's Dimensions");
	}

	/**
	 * @return Returns the numberColumns.
	 */
	public int getNumberColumns() {
		return numberColumns;
	}

	/**
	 * @param numberColumns The numberColumns to set.
	 */
	public void setNumberColumns(int numberColumns) {
		if(numberColumns<=0)
			throw new IndexOutOfBoundsException("Index sizes must be positive, non zero");
		this.numberColumns = numberColumns;
	}

	/**
	 * @return Returns the numberRows.
	 */
	public int getNumberRows() {
		return numberRows;
	}

	/**
	 * @param numberRows The numberRows to set.
	 */
	public void setNumberRows(int numberRows) {
		if(numberRows<=0)
			throw new IndexOutOfBoundsException("Index sizes must be positive, non zero");
		this.numberRows = numberRows;
	}
	
}
