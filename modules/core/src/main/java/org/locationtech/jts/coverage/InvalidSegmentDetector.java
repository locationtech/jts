/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentString;

class InvalidSegmentDetector implements SegmentIntersector {
  private double distanceTol;
  private LineIntersector li;

  /**
   * Creates a invalid segment detector.
   */
  public InvalidSegmentDetector(double distanceTol) {
    this.distanceTol = distanceTol;
    li = new RobustLineIntersector();
  }

  @Override
  public void processIntersections(SegmentString ss0, int segIndex1, SegmentString ss1, int segIndex0) {
    CoverageEdge target = (CoverageEdge) ss1;
    if (target.isValid(segIndex0)) return;
    CoverageEdge test = (CoverageEdge) ss0;
    if (test.isValid(segIndex1)) return;
    
    Coordinate p00 = target.getCoordinate(segIndex0);
    Coordinate p01 = target.getCoordinate(segIndex0 + 1);
    Coordinate p10 = test.getCoordinate(segIndex1);
    Coordinate p11 = test.getCoordinate(segIndex1 + 1);
    
    //-- zero-length segment, no need to check
    if (p00.equals2D(p01) || p10.equals2D(p11))
      return;

    boolean isInvalid = isInvalid(p00, p01, p10, p11);
    if (isInvalid) {
      target.markInvalid(segIndex0);
    }
  }

  private boolean isInvalid(Coordinate p00, Coordinate p01, Coordinate p10, Coordinate p11) {

    //-- segments are not within distance tolerance
    if (distanceTol < Distance.segmentToSegment(p00, p01, p10, p11)) {
      return false;
    }  
    
    //-- segments that cross or are collinear are invalid
    if (isCrossingOrCollinear(p00, p01, p10, p11))
      return true;

    //-- segments which are nearly parallel for a significant length 
    if (distanceTol > 0 && isNearlyParallel(p00, p01, p10, p11, distanceTol))
      return true;
    
    return false;
  }

  private boolean isCrossingOrCollinear(Coordinate p00, Coordinate p01, Coordinate p10, Coordinate p11) {
    li.computeIntersection(p00, p01, p10, p11);
    if (! li.hasIntersection())
      return false;
    //-- crosses
    if (li.isProper())
      return true;
    //-- one segment is a proper subset of the other (since if equal is already valid)
    if (li.getIntersectionNum() == 2)
      return true;
    return false;
  }
  
  private static boolean isNearlyParallel(Coordinate p00, Coordinate p01, 
      Coordinate p10, Coordinate p11, double distanceTol) {
    LineSegment line0 = new LineSegment(p00, p01);
    LineSegment line1 = new LineSegment(p10, p11);
    LineSegment proj0 = line0.project(line1);
    if (proj0 ==null)
      return false;
    LineSegment proj1 = line1.project(line0);
    if (proj1 ==null)
      return false;
    
    if (proj0.getLength() <= distanceTol
        || proj1.getLength() <= distanceTol)
      return false;
    
    if (proj0.p0.distance(proj1.p1) < proj0.p0.distance(proj1.p0)) {
      proj1.reverse();
    }
    return proj0.p0.distance(proj1.p0) <= distanceTol
        && proj0.p1.distance(proj1.p1) <= distanceTol;
  }

  @Override
  public boolean isDone() {
    // process all segments
    return false;
  }
}
