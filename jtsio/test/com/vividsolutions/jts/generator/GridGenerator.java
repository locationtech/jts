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

import java.util.NoSuchElementException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

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
	 * @see com.vividsolutions.jts.generator.GeometryGenerator#create()
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
	 * @see com.vividsolutions.jts.generator.GeometryGenerator#create()
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
	 * @see com.vividsolutions.jts.generator.GeometryGenerator#setDimensions(int)
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
