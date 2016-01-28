
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
package org.locationtech.jtstest.testbuilder.model;

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
