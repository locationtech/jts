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
package org.locationtech.jts.operation.relateng;

import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentString;

/**
 * Tests segments of {@link RelateSegmentString}s 
 * and if they intersect adds the intersection(s)
 * to the {@link TopologyComputer}.
 * 
 * @author Martin Davis
 *
 */
class EdgeSegmentIntersector implements SegmentIntersector 
{
  private RobustLineIntersector li = new RobustLineIntersector();
  private TopologyComputer topoComputer;

  public EdgeSegmentIntersector(TopologyComputer topoBuilder) {
    this.topoComputer = topoBuilder;
  }

  @Override
  public boolean isDone() {
    return topoComputer.isResultKnown();
  }
  
  public void processIntersections(SegmentString ss0, int segIndex0, 
      SegmentString ss1, int segIndex1) {
    // don't intersect a segment with itself
    if (ss0 == ss1 && segIndex0 == segIndex1) return;
    
    RelateSegmentString rss0 = (RelateSegmentString) ss0;
    RelateSegmentString rss1 = (RelateSegmentString) ss1;
    //TODO: move this ordering logic to TopologyBuilder
    if (rss0.isA()) {
      addIntersections(rss0, segIndex0, rss1, segIndex1);
    }
    else {
      addIntersections(rss1, segIndex1, rss0, segIndex0);
    }
  }

  private void addIntersections(RelateSegmentString ssA, int segIndexA, 
      RelateSegmentString ssB, int segIndexB) {
    
    Coordinate a0 = ssA.getCoordinate(segIndexA);
    Coordinate a1 = ssA.getCoordinate(segIndexA + 1);
    Coordinate b0 = ssB.getCoordinate(segIndexB);
    Coordinate b1 = ssB.getCoordinate(segIndexB + 1);
    
    li.computeIntersection(a0, a1, b0, b1);
    
    if (! li.hasIntersection())
      return;
    
    for (int i = 0; i < li.getIntersectionNum(); i++) {
      Coordinate intPt = li.getIntersection(i);
      /**
       * Ensure endpoint intersections are added once only, for their canonical segments.
       * Proper intersections lie on a unique segment so do not need to be checked.
       * And it is important that the Containing Segment check not be used, 
       * since due to intersection computation roundoff, 
       * it is not reliable in that situation. 
       */
      if (li.isProper() 
          || (ssA.isContainingSegment(segIndexA, intPt)
                && ssB.isContainingSegment(segIndexB, intPt))) {
        NodeSection nsa = ssA.createNodeSection(segIndexA, intPt);
        NodeSection nsb = ssB.createNodeSection(segIndexB, intPt);
        topoComputer.addIntersection(nsa, nsb);
      }
    }
  }
  
}
