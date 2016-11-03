/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.shape;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;

public abstract class GeometricShapeBuilder 
{
	protected Envelope extent = new Envelope(0, 1, 0, 1);
	protected int numPts = 0;
	protected GeometryFactory geomFactory;
	
	public GeometricShapeBuilder(GeometryFactory geomFactory)
	{
		this.geomFactory = geomFactory;
	}
	
	public void setExtent(Envelope extent)
	{
		this.extent = extent;
	}
	
	public Envelope getExtent()
	{
		return extent;
	}
	
	public Coordinate getCentre()
	{
		return extent.centre();
	}
	
	public double getDiameter()
	{
		return Math.min(extent.getHeight(), extent.getWidth());
	}
	
	public double getRadius()
	{
		return getDiameter() / 2;
	}
	
	public LineSegment getSquareBaseLine()
	{
		double radius = getRadius();
		
		Coordinate centre = getCentre();
		Coordinate p0 = new Coordinate(centre.x - radius, centre.y - radius);
		Coordinate p1 = new Coordinate(centre.x + radius, centre.y - radius);
		return new LineSegment(p0, p1);
	}
	
	public Envelope getSquareExtent()
	{
		double radius = getRadius();
		
		Coordinate centre = getCentre();
		return new Envelope(centre.x - radius, centre.x + radius,
				centre.y - radius, centre.y + radius);
	}
	

  /**
   * Sets the total number of points in the created {@link Geometry}.
   * The created geometry will have no more than this number of points,
   * unless more are needed to create a valid geometry.
   */
  public void setNumPoints(int numPts) { this.numPts = numPts; }

  public abstract Geometry getGeometry();

  protected Coordinate createCoord(double x, double y)
  {
  	Coordinate pt = new Coordinate(x, y);
  	geomFactory.getPrecisionModel().makePrecise(pt);
    return pt;
  }
  

}
