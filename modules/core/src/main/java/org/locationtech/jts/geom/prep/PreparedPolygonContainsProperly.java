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

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.noding.SegmentStringUtil;



/**
 * Computes the <tt>containsProperly</tt> spatial relationship predicate
 * for {@link PreparedPolygon}s relative to all other {@link Geometry} classes.
 * Uses short-circuit tests and indexing to improve performance. 
 * <p>
 * A Geometry A <tt>containsProperly</tt> another Geometry B iff
 * all points of B are contained in the Interior of A.
 * Equivalently, B is contained in A AND B does not intersect 
 * the Boundary of A.
 * <p>
 * The advantage to using this predicate is that it can be computed
 * efficiently, with no need to compute topology at individual points.
 * In a situation with many geometries intersecting the boundary 
 * of the target geometry, this can make a performance difference.
 * 
 * @author Martin Davis
 */
class PreparedPolygonContainsProperly 
	extends PreparedPolygonPredicate
{
	/**
	 * Computes the </tt>containsProperly</tt> predicate between a {@link PreparedPolygon}
	 * and a {@link Geometry}.
	 * 
	 * @param prep the prepared polygon
	 * @param geom a test geometry
	 * @return true if the polygon properly contains the geometry
	 */
	public static boolean containsProperly(PreparedPolygon prep, Geometry geom)
	{
		PreparedPolygonContainsProperly polyInt = new PreparedPolygonContainsProperly(prep);
    return polyInt.containsProperly(geom);
	}

  /**
   * Creates an instance of this operation.
   * 
   * @param prepPoly the PreparedPolygon to evaluate
   */
	public PreparedPolygonContainsProperly(PreparedPolygon prepPoly)
	{
		super(prepPoly);
	}
	
	/**
	 * Tests whether this PreparedPolygon containsProperly a given geometry.
	 * 
	 * @param geom the test geometry
	 * @return true if the test geometry is contained properly
	 */
	public boolean containsProperly(Geometry geom)
	{
		/**
		 * Do point-in-poly tests first, since they are cheaper and may result
		 * in a quick negative result.
		 * 
		 * If a point of any test components does not lie in the target interior, result is false
		 */
		boolean isAllInPrepGeomAreaInterior = isAllTestComponentsInTargetInterior(geom);
		if (! isAllInPrepGeomAreaInterior) return false;
		
		/**
		 * If any segments intersect, result is false.
		 */
    List lineSegStr = SegmentStringUtil.extractSegmentStrings(geom);
		boolean segsIntersect = prepPoly.getIntersectionFinder().intersects(lineSegStr);
		if (segsIntersect) 
      return false;
		
		/**
		 * Given that no segments intersect, if any vertex of the target
		 * is contained in some test component.
		 * the test is NOT properly contained.
		 */
		if (geom instanceof Polygonal) {
			// TODO: generalize this to handle GeometryCollections
			boolean isTargetGeomInTestArea = isAnyTargetComponentInAreaTest(geom, prepPoly.getRepresentativePoints());
			if (isTargetGeomInTestArea) return false;
		}
		
		return true;
	}
	
}
