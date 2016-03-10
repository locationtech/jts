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
package org.locationtech.jts.noding;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.geom.Coordinate;

/**
 * Finds <b>interior</b> intersections between line segments in {@link NodedSegmentString}s,
 * and adds them as nodes
 * using {@link NodedSegmentString#addIntersection(LineIntersector, int, int, int)}.
 * <p>
 * This class is used primarily for Snap-Rounding.  
 * For general-purpose noding, use {@link IntersectionAdder}.
 *
 * @version 1.7
 * @see IntersectionAdder
 * @deprecated see InteriorIntersectionFinderAdder
 */
public class IntersectionFinderAdder
    implements SegmentIntersector
{
  private LineIntersector li;
  private final List interiorIntersections;


  /**
   * Creates an intersection finder which finds all proper intersections
   *
   * @param li the LineIntersector to use
   */
  public IntersectionFinderAdder(LineIntersector li)
  {
    this.li = li;
    interiorIntersections = new ArrayList();
  }

  public List getInteriorIntersections()  {    return interiorIntersections;  }

  /**
   * This method is called by clients
   * of the {@link SegmentIntersector} class to process
   * intersections for two segments of the {@link SegmentString}s being intersected.
   * Note that some clients (such as <code>MonotoneChain</code>s) may optimize away
   * this call for segment pairs which they have determined do not intersect
   * (e.g. by an disjoint envelope test).
   */
  public void processIntersections(
      SegmentString e0,  int segIndex0,
      SegmentString e1,  int segIndex1
      )
  {
    // don't bother intersecting a segment with itself
    if (e0 == e1 && segIndex0 == segIndex1) return;

    Coordinate p00 = e0.getCoordinates()[segIndex0];
    Coordinate p01 = e0.getCoordinates()[segIndex0 + 1];
    Coordinate p10 = e1.getCoordinates()[segIndex1];
    Coordinate p11 = e1.getCoordinates()[segIndex1 + 1];

    li.computeIntersection(p00, p01, p10, p11);
//if (li.hasIntersection() && li.isProper()) Debug.println(li);

    if (li.hasIntersection()) {
      if (li.isInteriorIntersection()) {
        for (int intIndex = 0; intIndex < li.getIntersectionNum(); intIndex++) {
          interiorIntersections.add(li.getIntersection(intIndex));
        }
        ((NodedSegmentString) e0).addIntersections(li, segIndex0, 0);
        ((NodedSegmentString) e1).addIntersections(li, segIndex1, 1);
      }
    }
  }
  
  /**
   * Always process all intersections
   * 
   * @return false always
   */
  public boolean isDone() { return false; }

}