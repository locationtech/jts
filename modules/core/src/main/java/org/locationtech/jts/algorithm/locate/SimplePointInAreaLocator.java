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
package org.locationtech.jts.algorithm.locate;

import java.util.Iterator;

import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygonal;


/**
 * Computes the location of points
 * relative to a {@link Polygonal} {@link Geometry},
 * using a simple O(n) algorithm.
 * This algorithm is suitable for use in cases where
 * only one or a few points will be tested against a given area.
 * <p>
 * The algorithm used is only guaranteed to return correct results
 * for points which are <b>not</b> on the boundary of the Geometry.
 *
 * @version 1.7
 */
public class SimplePointInAreaLocator
	implements PointOnGeometryLocator
{

  /**
   * Determines the {@link Location} of a point in an areal {@link Geometry}.
   * Computes {@link Location.BOUNDARY} if the point lies exactly
   * on a geometry line segment. 
   * 
   * @param p the point to test
   * @param geom the areal geometry to test
   * @return the Location of the point in the geometry  
   */
  public static int locate(Coordinate p, Geometry geom)
  {
    if (geom.isEmpty()) return Location.EXTERIOR;
    return locateInGeometry(p, geom);
  }

  private static int locateInGeometry(Coordinate p, Geometry geom)
  {
    if (geom instanceof Polygon) {
      return locatePointInPolygon(p, (Polygon) geom);
    }
    else if (geom instanceof GeometryCollection) {
      Iterator geomi = new GeometryCollectionIterator((GeometryCollection) geom);
      while (geomi.hasNext()) {
        Geometry g2 = (Geometry) geomi.next();
        if (g2 != geom) {
          int loc = locateInGeometry(p, g2);
          if (loc != Location.EXTERIOR) return loc;
        }
      }
    }
    return Location.EXTERIOR;
  }

  /**
   * Determines the {@link Location} of a point in a {@link Polygon}.
   * Computes {@link Location.BOUNDARY} if the point lies exactly
   * on the polygon boundary. 
   * 
   * @param p the point to test
   * @param poly the geometry to test
   * @return the Location of the point in the polygon  
   */
  public static int locatePointInPolygon(Coordinate p, Polygon poly)
  {
    if (poly.isEmpty()) return Location.EXTERIOR;
    LinearRing shell = (LinearRing) poly.getExteriorRing();
    int shellLoc = locatePointInRing(p, shell);
    if (shellLoc != Location.INTERIOR) return shellLoc;
    
    // now test if the point lies in or on the holes
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      LinearRing hole = (LinearRing) poly.getInteriorRingN(i);
      int holeLoc = locatePointInRing(p, hole);
      if (holeLoc == Location.BOUNDARY) return Location.BOUNDARY;
      if (holeLoc == Location.INTERIOR) return Location.EXTERIOR;
      // if in EXTERIOR of this hole keep checking the other ones
    }
    // If not in any hole must be inside polygon
    return Location.INTERIOR;
  }

  /**
   * Determines whether a point lies in a {@link Polygon}.
   * If the point lies on the polygon boundary it is 
   * considered to be inside.
   * 
   * @param p the point to test
   * @param poly the geometry to test
   * @return true if the point lies in or on the polygon  
   */
  public static boolean containsPointInPolygon(Coordinate p, Polygon poly) {
    return Location.EXTERIOR != locatePointInPolygon(p, poly);
  }
  
  /**
   * Determines whether a point lies in a LinearRing,
   * using the ring envelope to short-circuit if possible.
   * 
   * @param p the point to test
   * @param ring a linear ring
   * @return true if the point lies inside the ring
   */
  private static int locatePointInRing(Coordinate p, LinearRing ring)
  {
  	// short-circuit if point is not in ring envelope
  	if (! ring.getEnvelopeInternal().intersects(p))
  		return Location.EXTERIOR;
  	return PointLocation.locateInRing(p, ring.getCoordinates());
  }

	private Geometry geom;

	public SimplePointInAreaLocator(Geometry geom) {
		this.geom = geom;
	}

	public int locate(Coordinate p) {
		return SimplePointInAreaLocator.locate(p, geom);
	}

}
