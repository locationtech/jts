
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
package com.vividsolutions.jtstest.testbuilder.model;

import java.awt.geom.Point2D;


/**
 * @version 1.7
 */
public class DrawingGrid 
{
	public static int DEFAULT_GRID_SIZE = 10;

	public static final int GRID_MAJOR_FACTOR = 1;
	
  private double gridSize = DEFAULT_GRID_SIZE;
  private int numGridUnits;

  public DrawingGrid(int gridSize)
  {
    setGridSize(gridSize);
  }

  public DrawingGrid()
  {
    this(DEFAULT_GRID_SIZE);
  }

  public Point2D snapToGrid(Point2D modelPoint)
  {
  	return snapToGrid(modelPoint, 1);
  }
  
  public Point2D snapToMajorGrid(Point2D modelPoint)
  {
  	return snapToGrid(modelPoint, GRID_MAJOR_FACTOR);
  }
  
  public Point2D snapToGrid(Point2D modelPoint, int factor)
  {
    double rx, ry;
    int numUnits = factor * numGridUnits;
    
    if (isFractional()) {
      rx = Math.floor(modelPoint.getX() * numUnits + .5) / numUnits;
      ry = Math.floor(modelPoint.getY() * numUnits + .5) / numUnits;
    }
    else {
      rx = Math.floor(modelPoint.getX()/numUnits + .5) * numUnits;
      ry = Math.floor(modelPoint.getY()/numUnits + .5) * numUnits;
    }
    return  new Point2D.Double(rx, ry);
  }

  public void setGridSize(double gridSize)
  {
    this.gridSize = gridSize;
    numGridUnits = (int) Math.floor(gridSize);
    if (isFractional()) {
      numGridUnits = (int) Math.floor(1.0 / gridSize);
    }
  }

  public boolean isFractional()
  {
    return gridSize < 1.0;
  }

  public double getGridSize () {
    return  gridSize;
  }

  public boolean isResolvable(Point2D p1,  Point2D p2)
  {
  	return p2.getX() - p1.getX() < getGridSize();
  }
}
