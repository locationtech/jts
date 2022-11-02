/*
 * Copyright (c) 2022 Martin Davis, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayarea;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.RayCrossingCounter;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;

/**
 * Computes the result area of an overlay usng the Overlay-Area, for simple polygons.
 * Simple polygons have no holes.
 * No indexing is used. 
 * This is faster than {@link OverlayArea} for polygons with low vertex count.
 * 
 * @author mdavis
 *
 */
public class SimpleOverlayArea {
  /**
   * Computes the area of intersection of two polygons with no holes.
   * 
   * @param poly0 a polygon
   * @param poly1 a polygon
   * @return the area of the intersection of the polygons
   */
  public static double intersectionArea(Polygon poly0, Polygon poly1) {
    SimpleOverlayArea area = new SimpleOverlayArea(poly0, poly1);
    return area.getArea();
  }
  
  private Polygon geomA;
  private Polygon geomB;

  public SimpleOverlayArea(Polygon geom0, Polygon geom1) {
    this.geomA = geom0;
    this.geomB = geom1;
    //TODO: error if polygon has holes
  }
  
  public double getArea() {
    if (geomA.getNumInteriorRing() > 0
        || geomB.getNumInteriorRing() > 0) {
      throw new IllegalArgumentException("Polygons wtih holes are not supported");
    }
    
    CoordinateSequence ringA = getVertices(geomA);
    CoordinateSequence ringB = getVertices(geomB);
    
    boolean isCCWA = Orientation.isCCW(ringA);
    boolean isCCWB = Orientation.isCCW(ringB);

    double areaInt = areaForIntersections(ringA, isCCWA, ringB, isCCWB);
    double areaVert0 = areaForInteriorVertices(ringA, isCCWA, ringB);
    double areaVert1 = areaForInteriorVertices(ringB, isCCWB, ringA);
    
    return (areaInt + areaVert1 + areaVert0) / 2;
  }

  private static CoordinateSequence getVertices(Geometry geom) {
    Polygon poly = (Polygon) geom;
    CoordinateSequence seq = poly.getExteriorRing().getCoordinateSequence();
    return seq;
  }
  
  private double areaForIntersections(CoordinateSequence ringA, boolean isCCWA, CoordinateSequence ringB, boolean isCCWB) {
    //TODO: use fast intersection computation?
    
    // Compute rays for all intersections
    LineIntersector li = new RobustLineIntersector();
    
    double area = 0;
    for (int i = 0; i < ringA.size()-1; i++) {
      Coordinate a0 = ringA.getCoordinate(i);
      Coordinate a1 = ringA.getCoordinate(i+1);
      
      if (isCCWA) {
        // flip segment orientation
        Coordinate temp = a0; a0 = a1; a1 = temp;
      }
      
      for (int j = 0; j < ringB.size()-1; j++) {
        Coordinate b0 = ringB.getCoordinate(j);
        Coordinate b1 = ringB.getCoordinate(j+1);
        
        if (isCCWB) {
          // flip segment orientation
          Coordinate temp = b0; b0 = b1; b1 = temp;
        }
        
        li.computeIntersection(a0, a1, b0, b1);
        if (li.hasIntersection()) {
          
          /**
           * With both rings oriented CW (effectively)
           * There are two situations for segment intersections:
           * 
           * 1) A entering B, B exiting A => rays are IP-A1:R, IP-B0:L
           * 2) A exiting B, B entering A => rays are IP-A0:L, IP-B1:R
           * (where :L/R indicates result is to the Left or Right).
           * 
           * Use full edge to compute direction, for accuracy.
           */
          Coordinate intPt = li.getIntersection(0);
          
          boolean isAenteringB = Orientation.COUNTERCLOCKWISE == Orientation.index(a0, a1, b1);
          
          if ( isAenteringB ) {
            area += EdgeVector.area2Term(intPt, a0, a1, true);
            area += EdgeVector.area2Term(intPt, b1, b0, false);
          }
          else {
            area += EdgeVector.area2Term(intPt, a1, a0, false);
            area += EdgeVector.area2Term(intPt, b0, b1, true);
          }
        }
      }
    }
    return area;
  }
    
  private double areaForInteriorVertices(CoordinateSequence ring, boolean isCCW, CoordinateSequence ring2) {
    double area = 0;
    /**
     * Compute rays originating at vertices inside the resultant
     * (i.e. A vertices inside B, and B vertices inside A)
     */
    for (int i = 0; i < ring.size() - 1; i++) {
      Coordinate vPrev = i == 0 ? ring.getCoordinate(ring.size()-2) : ring.getCoordinate(i-1);
      Coordinate v = ring.getCoordinate(i);
      Coordinate vNext = ring.getCoordinate(i+1);
      int loc = RayCrossingCounter.locatePointInRing(v, ring2);
      if (loc == Location.INTERIOR) {
        area += EdgeVector.area2Term(v, vPrev, isCCW);
        area += EdgeVector.area2Term(v, vNext, ! isCCW);
      }
    }
    return area;
  }
  
}
