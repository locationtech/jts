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
package org.locationtech.jts.algorithm;

import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Computes the topological ({@link Location})
 * of a single point to a {@link Geometry}.
 * A {@link BoundaryNodeRule} may be specified 
 * to control the evaluation of whether the point lies on the boundary or not
 * The default rule is to use the the <i>SFS Boundary Determination Rule</i>
 * <p>
 * Notes:
 * <ul>
 * <li>{@link LinearRing}s do not enclose any area - points inside the ring are still in the EXTERIOR of the ring.
 * </ul>
 * Instances of this class are not reentrant.
 *
 * @version 1.7
 */
public class PointLocator
{
  // default is to use OGC SFS rule
  private BoundaryNodeRule boundaryRule = 
  	//BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE; 
  	BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE;

  private boolean isIn;         // true if the point lies in or on any Geometry element
  private int numBoundaries;    // the number of sub-elements whose boundaries the point lies in

  public PointLocator() {
  }

  public PointLocator(BoundaryNodeRule boundaryRule)
  {
    if (boundaryRule == null)
      throw new IllegalArgumentException("Rule must be non-null");
    this.boundaryRule = boundaryRule;
  }

  /**
   * Convenience method to test a point for intersection with
   * a Geometry
   * @param p the coordinate to test
   * @param geom the Geometry to test
   * @return <code>true</code> if the point is in the interior or boundary of the Geometry
   */
  public boolean intersects(Coordinate p, Geometry geom)
  {
    return locate(p, geom) != Location.EXTERIOR;
  }

  /**
   * Computes the topological relationship ({@link Location}) of a single point
   * to a Geometry.
   * It handles both single-element
   * and multi-element Geometries.
   * The algorithm for multi-part Geometries
   * takes into account the SFS Boundary Determination Rule.
   *
   * @return the {@link Location} of the point relative to the input Geometry
   */
  public int locate(Coordinate p, Geometry geom)
  {
    if (geom.isEmpty()) return Location.EXTERIOR;

    if (geom instanceof LineString) {
      return locate(p, (LineString) geom);
    }
    else if (geom instanceof Polygon) {
      return locate(p, (Polygon) geom);
    }

    isIn = false;
    numBoundaries = 0;
    computeLocation(p, geom);
    if (boundaryRule.isInBoundary(numBoundaries))
      return Location.BOUNDARY;
    if (numBoundaries > 0 || isIn)
      return Location.INTERIOR;

    return Location.EXTERIOR;
  }

  private void computeLocation(Coordinate p, Geometry geom)
  {
    if (geom instanceof Point) {
      updateLocationInfo(locate(p, (Point) geom));
    }
    if (geom instanceof LineString) {
      updateLocationInfo(locate(p, (LineString) geom));
    }
    else if (geom instanceof Polygon) {
      updateLocationInfo(locate(p, (Polygon) geom));
    }
    else if (geom instanceof MultiLineString) {
      MultiLineString ml = (MultiLineString) geom;
      for (int i = 0; i < ml.getNumGeometries(); i++) {
        LineString l = (LineString) ml.getGeometryN(i);
        updateLocationInfo(locate(p, l));
      }
    }
    else if (geom instanceof MultiPolygon) {
      MultiPolygon mpoly = (MultiPolygon) geom;
      for (int i = 0; i < mpoly.getNumGeometries(); i++) {
        Polygon poly = (Polygon) mpoly.getGeometryN(i);
        updateLocationInfo(locate(p, poly));
      }
    }
    else if (geom instanceof GeometryCollection) {
      Iterator geomi = new GeometryCollectionIterator((GeometryCollection) geom);
      while (geomi.hasNext()) {
        Geometry g2 = (Geometry) geomi.next();
        if (g2 != geom)
          computeLocation(p, g2);
      }
    }
  }

  private void updateLocationInfo(int loc)
  {
    if (loc == Location.INTERIOR) isIn = true;
    if (loc == Location.BOUNDARY) numBoundaries++;
  }

  private int locate(Coordinate p, Point pt)
  {
  	// no point in doing envelope test, since equality test is just as fast
  	
    Coordinate ptCoord = pt.getCoordinate();
    if (ptCoord.equals2D(p))
      return Location.INTERIOR;
    return Location.EXTERIOR;
  }

  private int locate(Coordinate p, LineString l)
  {
  	// bounding-box check
  	if (! l.getEnvelopeInternal().intersects(p)) return Location.EXTERIOR;
  	
    Coordinate[] pt = l.getCoordinates();
    if (! l.isClosed()) {
      if (p.equals(pt[0])
          || p.equals(pt[pt.length - 1]) ) {
        return Location.BOUNDARY;
      }
    }
    if (CGAlgorithms.isOnLine(p, pt))
      return Location.INTERIOR;
    return Location.EXTERIOR;
  }

  private int locateInPolygonRing(Coordinate p, LinearRing ring)
  {
  	// bounding-box check
  	if (! ring.getEnvelopeInternal().intersects(p)) return Location.EXTERIOR;

  	return CGAlgorithms.locatePointInRing(p, ring.getCoordinates());
  }

  private int locate(Coordinate p, Polygon poly)
  {
    if (poly.isEmpty()) return Location.EXTERIOR;

    LinearRing shell = (LinearRing) poly.getExteriorRing();

    int shellLoc = locateInPolygonRing(p, shell);
    if (shellLoc == Location.EXTERIOR) return Location.EXTERIOR;
    if (shellLoc == Location.BOUNDARY) return Location.BOUNDARY;
    // now test if the point lies in or on the holes
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      LinearRing hole = (LinearRing) poly.getInteriorRingN(i);
      int holeLoc = locateInPolygonRing(p, hole);
      if (holeLoc == Location.INTERIOR) return Location.EXTERIOR;
      if (holeLoc == Location.BOUNDARY) return Location.BOUNDARY;
    }
    return Location.INTERIOR;
  }



}
