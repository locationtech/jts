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
import org.locationtech.jts.algorithm.PolygonNodeTopology;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentString;

class InvalidSegmentDetector implements SegmentIntersector {
  private double distanceTol;

  /**
   * Creates a invalid segment detector.
   */
  public InvalidSegmentDetector(double distanceTol) {
    this.distanceTol = distanceTol;
  }

  @Override
  public void processIntersections(SegmentString ss0, int index0, SegmentString ss1, int index1) {
    // note the source of the edges is important
    CoverageRing target = (CoverageRing) ss1;
    int iTarget =  index1;
    CoverageRing adj = (CoverageRing) ss0;
    int iAdj =  index0;
    
    //-- don't check a target segment with known status
    if (target.isKnown(iTarget)) return;
    
    Coordinate t0 = target.getCoordinate(iTarget);
    Coordinate t1 = target.getCoordinate(iTarget + 1);
    Coordinate adj0 = adj.getCoordinate(iAdj);
    Coordinate adj1 = adj.getCoordinate(iAdj + 1);
    /*
    System.out.println("checking target= " + WKTWriter.toLineString(t0, t1)
     + "   adj= " + WKTWriter.toLineString(adj0, adj1));
    //*/
    
    //-- don't check zero-length segments
    if (t0.equals2D(t1) || adj0.equals2D(adj1))
      return;

    //-- don't check segments not within distance tolerance
    if (distanceTol < Distance.segmentToSegment(t0, t1, adj0, adj1)) {
      return;
    } 
    
    boolean isInvalid = isInvalid(t0, t1, adj0, adj1, adj, iAdj);
    if (isInvalid) {
      target.markInvalid(iTarget);
    }
  }

  private boolean isInvalid(Coordinate tgt0, Coordinate tgt1, 
      Coordinate adj0, Coordinate adj1, CoverageRing adj, int indexAdj) {

    //-- segments that are collinear (but not matching) or are interior are invalid
    if (isCollinearOrInterior(tgt0, tgt1, adj0, adj1, adj, indexAdj))
      return true;

    //-- segments which are nearly parallel for a significant length are invalid
    if (distanceTol > 0 && isNearlyParallel(tgt0, tgt1, adj0, adj1, distanceTol))
      return true;
    
    return false;
  }

  /**
   * Checks if the segments are collinear, or if the target segment 
   * intersects the interior of the adjacent ring.
   * Matching segments have already been marked as valid and are skipped.
   * Thus collinear segments must not match, and hence are invalid. 
   * 
   * @param tgt0
   * @param tgt1
   * @param adj0
   * @param adj1
   * @return
   */
  private boolean isCollinearOrInterior(Coordinate tgt0, Coordinate tgt1, 
      Coordinate adj0, Coordinate adj1, CoverageRing adj, int indexAdj) {
    RobustLineIntersector li = new RobustLineIntersector();
    li.computeIntersection(tgt0, tgt1, adj0, adj1);
    
    //-- segments do not interact
    if (! li.hasIntersection())
      return false;
    
    /**
     * If the segments are collinear,
     * they do not match, so are invalid.
     */
    if (li.getIntersectionNum() == 2) {
      //TODO: assert segments are not equal?
      return true;
    }
    
    //-- target segment crosses, or segments touch at non-endpoint
    if (li.isProper() || li.isInteriorIntersection()) {
      return true;
    }
    
    /**
     * At this point the segments have a single intersection point 
     * which is an endpoint of both segments.
     * 
     * Check if the target segment lies in the interior of the adj ring.
     */
    Coordinate intVertex = li.getIntersection(0);
    boolean isInterior = isInteriorSegment(intVertex, tgt0, tgt1, adj, indexAdj);
    return isInterior;
  }

  private boolean isInteriorSegment(Coordinate intVertex, Coordinate tgt0, Coordinate tgt1, 
      CoverageRing adj, int indexAdj) {
    //-- find target segment endpoint which is not the intersection point
    Coordinate tgtEnd = intVertex.equals2D(tgt0) ? tgt1 : tgt0;

    //-- find adjacent ring vertices on either side of intersection vertex
    Coordinate adjPrev = adj.findVertexPrev(indexAdj, intVertex);
    Coordinate adjNext = adj.findVertexNext(indexAdj, intVertex);
    //-- if needed, re-orient corner to have interior on right
    if (! adj.isInteriorOnRight()) {
      Coordinate temp = adjPrev;
      adjPrev = adjNext;
      adjNext = temp;
    }
    
    boolean isInterior = PolygonNodeTopology.isInteriorSegment(intVertex, adjPrev, adjNext, tgtEnd);
    return isInterior;
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
