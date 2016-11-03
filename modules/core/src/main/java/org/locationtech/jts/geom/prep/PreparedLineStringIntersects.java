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
package org.locationtech.jts.geom.prep;

import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.PointLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.ComponentCoordinateExtracter;
import org.locationtech.jts.noding.SegmentStringUtil;



/**
 * Computes the <tt>intersects</tt> spatial relationship predicate
 * for a target {@link PreparedLineString} relative to other {@link Geometry} classes.
 * Uses short-circuit tests and indexing to improve performance. 
 * 
 * @author Martin Davis
 *
 */
class PreparedLineStringIntersects 
{
	/**
	 * Computes the intersects predicate between a {@link PreparedLineString}
	 * and a {@link Geometry}.
	 * 
	 * @param prep the prepared linestring
	 * @param geom a test geometry
	 * @return true if the linestring intersects the geometry
	 */
	public static boolean intersects(PreparedLineString prep, Geometry geom)
	{
		PreparedLineStringIntersects op = new PreparedLineStringIntersects(prep);
    return op.intersects(geom);
	}

	protected PreparedLineString prepLine;

  /**
   * Creates an instance of this operation.
   * 
   * @param prepPoly the target PreparedLineString
   */
	public PreparedLineStringIntersects(PreparedLineString prepLine)
	{
		this.prepLine = prepLine;
	}
	
	/**
	 * Tests whether this geometry intersects a given geometry.
	 * 
	 * @param geom the test geometry
	 * @return true if the test geometry intersects
	 */
	public boolean intersects(Geometry geom)
	{
		/**
		 * If any segments intersect, obviously intersects = true
		 */
    List lineSegStr = SegmentStringUtil.extractSegmentStrings(geom);
    // only request intersection finder if there are segments (ie NOT for point inputs)
    if (lineSegStr.size() > 0) {
  		boolean segsIntersect = prepLine.getIntersectionFinder().intersects(lineSegStr);
  		// MD - performance testing
  //		boolean segsIntersect = false;
  		if (segsIntersect) 
        return true;
    }
		/**
		 * For L/L case we are done
		 */
		if (geom.getDimension() == 1) return false;
		
		/**
		 * For L/A case, need to check for proper inclusion of the target in the test
		 */
		if (geom.getDimension() == 2
				&& prepLine.isAnyTargetComponentInTest(geom)) return true;
		
		/** 
		 * For L/P case, need to check if any points lie on line(s)
		 */
		if (geom.getDimension() == 0)
			return isAnyTestPointInTarget(geom);
		
		return false;
	}
	  
  /**
   * Tests whether any representative point of the test Geometry intersects
   * the target geometry.
   * Only handles test geometries which are Puntal (dimension 0)
   * 
   * @param geom a Puntal geometry to test
   * @return true if any point of the argument intersects the prepared geometry
   */
	protected boolean isAnyTestPointInTarget(Geometry testGeom)
	{
		/**
		 * This could be optimized by using the segment index on the lineal target.
		 * However, it seems like the L/P case would be pretty rare in practice.
		 */
		PointLocator locator = new PointLocator();
    List coords = ComponentCoordinateExtracter.getCoordinates(testGeom);
    for (Iterator i = coords.iterator(); i.hasNext(); ) {
      Coordinate p = (Coordinate) i.next();
      if (locator.intersects(p, prepLine.getGeometry()))
        return true;
    }
		return false;
	}

}
