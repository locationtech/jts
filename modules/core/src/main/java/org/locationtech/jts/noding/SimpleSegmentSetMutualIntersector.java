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

import java.util.Collection;
import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;



/**
 * Intersects two sets of {@link SegmentString}s using 
 * brute-force comparison.
 *
 * @version 1.7
 */
public class SimpleSegmentSetMutualIntersector implements SegmentSetMutualIntersector
{
  private final Collection baseSegStrings;

  /**
   * Constructs a new intersector for a given set of {@link SegmentString}s.
   * 
   * @param segStrings the base segment strings to intersect
   */
  public SimpleSegmentSetMutualIntersector(Collection segStrings)
  {
	  this.baseSegStrings = segStrings;
  }

  /**
   * Calls {@link SegmentIntersector#processIntersections(SegmentString, int, SegmentString, int)} 
   * for all <i>candidate</i> intersections between
   * the given collection of SegmentStrings and the set of base segments. 
   * 
   * @param a set of segments to intersect
   * @param the segment intersector to use
   */
  public void process(Collection segStrings, SegmentIntersector segInt) {
    for (Iterator i = baseSegStrings.iterator(); i.hasNext(); ) {
    	SegmentString baseSS = (SegmentString) i.next();
    	for (Iterator j = segStrings.iterator(); j.hasNext(); ) {
	      	SegmentString ss = (SegmentString) j.next();
	      	intersect(baseSS, ss, segInt);
	        if (segInt.isDone()) 
	        	return;
    	}
    }
  }

  /**
   * Processes all of the segment pairs in the given segment strings
   * using the given SegmentIntersector.
   * 
   * @param ss0 a Segment string
   * @param ss1 a segment string
   * @param segInt the segment intersector to use
   */
  private void intersect(SegmentString ss0, SegmentString ss1, SegmentIntersector segInt)
  {
    Coordinate[] pts0 = ss0.getCoordinates();
    Coordinate[] pts1 = ss1.getCoordinates();
    for (int i0 = 0; i0 < pts0.length - 1; i0++) {
      for (int i1 = 0; i1 < pts1.length - 1; i1++) {
        segInt.processIntersections(ss0, i0, ss1, i1);
        if (segInt.isDone()) 
        	return;
      }
    }

  }

}
