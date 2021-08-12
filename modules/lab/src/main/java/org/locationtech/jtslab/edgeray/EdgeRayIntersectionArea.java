/*
 * Copyright (c) 2018 Martin Davis, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtslab.edgeray;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;

public class EdgeRayIntersectionArea {
  
  public static double area(Geometry geom0, Geometry geom1) {
    EdgeRayIntersectionArea area = new EdgeRayIntersectionArea(geom0, geom1);
    return area.getArea();
  }
  
  private Geometry geomA;
  private Geometry geomB;
  double area = 0;

  public EdgeRayIntersectionArea(Geometry geom0, Geometry geom1) {
    this.geomA = geom0;
    this.geomB = geom1;
  }
  
  public double getArea() {
    // TODO: for now assume poly is CW and has no holes
    
    addIntersections();
    addResultVertices(geomA, geomB);
    addResultVertices(geomB, geomA);
    return area;
  }

  private void addIntersections() {
    CoordinateSequence seqA = getVertices(geomA);
    boolean[] isIntersected0 = new boolean[seqA.size()-1];
    CoordinateSequence seqB = getVertices(geomB);
    boolean[] isIntersected1 = new boolean[seqB.size()-1];
    
    boolean isCCWA = Orientation.isCCW(seqA);
    boolean isCCWB = Orientation.isCCW(seqB);
    
    // Compute rays for all intersections
    LineIntersector li = new RobustLineIntersector();
    
    for (int i = 0; i < seqA.size()-1; i++) {
      Coordinate a0 = seqA.getCoordinate(i);
      Coordinate a1 = seqA.getCoordinate(i+1);
      
      if (isCCWA) {
        // flip segment orientation
        Coordinate temp = a0; a0 = a1; a1 = temp;
      }
      
      for (int j = 0; j < seqB.size()-1; j++) {
        Coordinate b0 = seqB.getCoordinate(j);
        Coordinate b1 = seqB.getCoordinate(j+1);
        
        if (isCCWB) {
          // flip segment orientation
          Coordinate temp = b0; b0 = b1; b1 = temp;
        }
        
        li.computeIntersection(a0, a1, b0, b1);
        if (li.hasIntersection()) {
          isIntersected0[i] = true;
          isIntersected1[j] = true;
          
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
            area += EdgeRay.areaTerm(intPt, a0, a1, true);
            area += EdgeRay.areaTerm(intPt, b1, b0, false);
          }
          else {
            area += EdgeRay.areaTerm(intPt, a1, a0, false);
            area += EdgeRay.areaTerm(intPt, b0, b1, true);
          }
        }
      }
    }
  }
    
  private void addResultVertices(Geometry geom0, Geometry geom1) {
    /**
     * Compute rays originating at vertices inside the resultant
     * (i.e. A vertices inside B, and B vertices inside A)
     */
    IndexedPointInAreaLocator locator = new IndexedPointInAreaLocator(geom1);
    CoordinateSequence seq = getVertices(geom0);
    boolean isCW = ! Orientation.isCCW(seq);
    for (int i = 0; i < seq.size()-1; i++) {
      Coordinate vPrev = i == 0 ? seq.getCoordinate(seq.size()-2) : seq.getCoordinate(i-1);
      Coordinate v = seq.getCoordinate(i);
      Coordinate vNext = seq.getCoordinate(i+1);
      if (Location.INTERIOR == locator.locate(v)) {
        area += EdgeRay.areaTerm(v, vPrev, ! isCW);
        area += EdgeRay.areaTerm(v, vNext, isCW);
      }
    }
  }
  
  private CoordinateSequence getVertices(Geometry geom) {
    Polygon poly = (Polygon) geom;
    CoordinateSequence seq = poly.getExteriorRing().getCoordinateSequence();
    return seq;
  }
}
