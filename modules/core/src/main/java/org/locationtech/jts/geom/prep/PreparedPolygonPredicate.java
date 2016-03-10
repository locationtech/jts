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

import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.util.ComponentCoordinateExtracter;


/**
 * A base class for predicate operations on {@link PreparedPolygon}s.
 * 
 * @author mbdavis
 *
 */
abstract class PreparedPolygonPredicate 
{
	protected PreparedPolygon prepPoly;
  private PointOnGeometryLocator targetPointLocator;

  /**
   * Creates an instance of this operation.
   * 
   * @param prepPoly the PreparedPolygon to evaluate
   */
	public PreparedPolygonPredicate(PreparedPolygon prepPoly)
	{
		this.prepPoly = prepPoly;
    targetPointLocator = prepPoly.getPointLocator();
	}
	
  /**
   * Tests whether all components of the test Geometry 
	 * are contained in the target geometry.
   * Handles both linear and point components.
   * 
   * @param geom a geometry to test
   * @return true if all componenta of the argument are contained in the target geometry
   */
	protected boolean isAllTestComponentsInTarget(Geometry testGeom)
	{
    List coords = ComponentCoordinateExtracter.getCoordinates(testGeom);
    for (Iterator i = coords.iterator(); i.hasNext(); ) {
      Coordinate p = (Coordinate) i.next();
      int loc = targetPointLocator.locate(p);
      if (loc == Location.EXTERIOR)
        return false;
    }
		return true;
	}
	
  /**
   * Tests whether all components of the test Geometry 
	 * are contained in the interior of the target geometry.
   * Handles both linear and point components.
   * 
   * @param geom a geometry to test
   * @return true if all componenta of the argument are contained in the target geometry interior
   */
	protected boolean isAllTestComponentsInTargetInterior(Geometry testGeom)
	{
    List coords = ComponentCoordinateExtracter.getCoordinates(testGeom);
    for (Iterator i = coords.iterator(); i.hasNext(); ) {
      Coordinate p = (Coordinate) i.next();
      int loc = targetPointLocator.locate(p);
      if (loc != Location.INTERIOR)
        return false;
    }
		return true;
	}
	
  /**
   * Tests whether any component of the test Geometry intersects
   * the area of the target geometry.
   * Handles test geometries with both linear and point components.
   * 
   * @param geom a geometry to test
   * @return true if any component of the argument intersects the prepared area geometry
   */
	protected boolean isAnyTestComponentInTarget(Geometry testGeom)
	{
    List coords = ComponentCoordinateExtracter.getCoordinates(testGeom);
    for (Iterator i = coords.iterator(); i.hasNext(); ) {
      Coordinate p = (Coordinate) i.next();
      int loc = targetPointLocator.locate(p);
      if (loc != Location.EXTERIOR)
        return true;
    }
		return false;
	}

  /**
   * Tests whether any component of the test Geometry intersects
   * the interior of the target geometry.
   * Handles test geometries with both linear and point components.
   * 
   * @param geom a geometry to test
   * @return true if any component of the argument intersects the prepared area geometry interior
   */
	protected boolean isAnyTestComponentInTargetInterior(Geometry testGeom)
	{
    List coords = ComponentCoordinateExtracter.getCoordinates(testGeom);
    for (Iterator i = coords.iterator(); i.hasNext(); ) {
      Coordinate p = (Coordinate) i.next();
      int loc = targetPointLocator.locate(p);
      if (loc == Location.INTERIOR)
        return true;
    }
		return false;
	}


	/**
	 * Tests whether any component of the target geometry 
	 * intersects the test geometry (which must be an areal geometry) 
	 * 
	 * @param geom the test geometry
	 * @param repPts the representative points of the target geometry
	 * @return true if any component intersects the areal test geometry
	 */
	protected boolean isAnyTargetComponentInAreaTest(Geometry testGeom, List targetRepPts)
	{
		PointOnGeometryLocator piaLoc = new SimplePointInAreaLocator(testGeom);
    for (Iterator i = targetRepPts.iterator(); i.hasNext(); ) {
      Coordinate p = (Coordinate) i.next();
      int loc = piaLoc.locate(p);
      if (loc != Location.EXTERIOR)
        return true;
    }
		return false;
	}

}
