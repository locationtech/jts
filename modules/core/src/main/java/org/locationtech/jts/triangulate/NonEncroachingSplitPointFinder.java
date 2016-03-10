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

package org.locationtech.jts.triangulate;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

/**
 * A strategy for finding constraint split points which attempts to maximise the length of the split
 * segments while preventing further encroachment. (This is not always possible for narrow angles).
 * 
 * @author Martin Davis
 */
public class NonEncroachingSplitPointFinder implements ConstraintSplitPointFinder {

    public NonEncroachingSplitPointFinder() {}

    /**
     * A basic strategy for finding split points when nothing extra is known about the geometry of
     * the situation.
     * 
     * @param seg the encroached segment
     * @param encroachPt the encroaching point
     * @return the point at which to split the encroached segment
     */
    public Coordinate findSplitPoint(Segment seg, Coordinate encroachPt) {
        LineSegment lineSeg = seg.getLineSegment();
        double segLen = lineSeg.getLength();
        double midPtLen = segLen / 2;
        SplitSegment splitSeg = new SplitSegment(lineSeg);

        Coordinate projPt = projectedSplitPoint(seg, encroachPt);
        /**
         * Compute the largest diameter (length) that will produce a split segment which is not
         * still encroached upon by the encroaching point (The length is reduced slightly by a
         * safety factor)
         */
        double nonEncroachDiam = projPt.distance(encroachPt) * 2 * 0.8; // .99;
        double maxSplitLen = nonEncroachDiam;
        if (maxSplitLen > midPtLen) {
            maxSplitLen = midPtLen;
        }
        splitSeg.setMinimumLength(maxSplitLen);

        splitSeg.splitAt(projPt);

        return splitSeg.getSplitPoint();
    }

    /**
     * Computes a split point which is the projection of the encroaching point on the segment
     * 
     * @param seg
     * @param encroachPt
     * @return a split point on the segment
     */
    public static Coordinate projectedSplitPoint(Segment seg, Coordinate encroachPt) {
        LineSegment lineSeg = seg.getLineSegment();
        Coordinate projPt = lineSeg.project(encroachPt);
        return projPt;
    }
}
