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
 * Models a constraint segment which can be split in two in various ways, 
 * according to certain geometric constraints.
 * 
 * @author Martin Davis
 */
public class SplitSegment {
    /**
     * Computes the {@link Coordinate} that lies a given fraction along the line defined by the
     * reverse of the given segment. A fraction of <code>0.0</code> returns the end point of the
     * segment; a fraction of <code>1.0</code> returns the start point of the segment.
     * 
     * @param seg the LineSegment
     * @param segmentLengthFraction the fraction of the segment length along the line
     * @return the point at that distance
     */
    private static Coordinate pointAlongReverse(LineSegment seg, double segmentLengthFraction) {
        Coordinate coord = new Coordinate();
        coord.x = seg.p1.x - segmentLengthFraction * (seg.p1.x - seg.p0.x);
        coord.y = seg.p1.y - segmentLengthFraction * (seg.p1.y - seg.p0.y);
        return coord;
    }

    private LineSegment seg;
    private double      segLen;
    private Coordinate  splitPt;
    private double      minimumLen = 0.0;

    public SplitSegment(LineSegment seg) {
        this.seg = seg;
        segLen = seg.getLength();
    }

    public void setMinimumLength(double minLen) {
        minimumLen = minLen;
    }

    public Coordinate getSplitPoint() {
        return splitPt;
    }

    public void splitAt(double length, Coordinate endPt) {
        double actualLen = getConstrainedLength(length);
        double frac = actualLen / segLen;
        if (endPt.equals2D(seg.p0))
            splitPt = seg.pointAlong(frac);
        else
            splitPt = pointAlongReverse(seg, frac);
    }

    public void splitAt(Coordinate pt) {
        // check that given pt doesn't violate min length
        double minFrac = minimumLen / segLen;
        if (pt.distance(seg.p0) < minimumLen) {
            splitPt = seg.pointAlong(minFrac);
            return;
        }
        if (pt.distance(seg.p1) < minimumLen) {
            splitPt = pointAlongReverse(seg, minFrac);
            return;
        }
        // passes minimum distance check - use provided point as split pt
        splitPt = pt;
    }

    private double getConstrainedLength(double len) {
        if (len < minimumLen)
            return minimumLen;
        return len;
    }

}
