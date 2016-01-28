/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jts.triangulate;


import org.locationtech.jts.geom.Coordinate;

/**
 * A simple split point finder which returns the midpoint of the split segment. This is a default
 * strategy only. Usually a more sophisticated strategy is required to prevent repeated splitting.
 * Other points which could be used are:
 * <ul>
 * <li>The projection of the encroaching point on the segment
 * <li>A point on the segment which will produce two segments which will not be further encroached
 * <li>The point on the segment which is the same distance from an endpoint as the encroaching
 * point
 * </ul>
 * 
 * @author Martin Davis
 */
public class MidpointSplitPointFinder implements ConstraintSplitPointFinder {
    /**
     * Gets the midpoint of the split segment
     */
    public Coordinate findSplitPoint(Segment seg, Coordinate encroachPt) {
        Coordinate p0 = seg.getStart();
        Coordinate p1 = seg.getEnd();
        return new Coordinate((p0.x + p1.x) / 2, (p0.y + p1.y) / 2);
    }

}
