/*
 * Copyright (c) 2026 Kevin Smith.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.densify;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

/**
 * Interpolator which generates points by dividing the segment in two and then
 * repeating recursively until all the segments are under the threshold.
 * 
 * @author Kevin Smith
 */
public abstract class SubdividingSegmentInterpolator implements SegmentInterpolator {
	@Override
	public Iterator<Coordinate> densifySegment(LineSegment seg, Coordinate[] pts, int i,  double distanceTolerance) {
		final Deque<LineSegment> stack = new ArrayDeque<LineSegment>();
		stack.push(seg);

		return new Iterator<Coordinate>() {

			@Override
			public boolean hasNext() {
				return stack.size() > 1 || stack.peek().getLength() > distanceTolerance;
			}

			@Override
			public Coordinate next() {

				LineSegment currentSeg = stack.pop();

				while (currentSeg.getLength() > distanceTolerance) {
					Coordinate midpoint = midpoint(currentSeg);
					stack.push(new LineSegment(midpoint, currentSeg.p1));
					currentSeg.p1 = midpoint;
				}

				return currentSeg.p1;
			}

		};
	}

	/**
	 * Generate a point midway along a segment
	 * 
	 * @param seg
	 * @return
	 */
	public abstract Coordinate midpoint(LineSegment seg);

	/**
	 * Fill in the z value of the midpoint of a segment if the both endpoints have z
	 * set.
	 * 
	 * @param seg
	 * @param p
	 */
	protected final void fillZ(LineSegment seg, Coordinate p) {
		if (!Double.isNaN(seg.p0.z) && !Double.isNaN(seg.p1.z)) {
			p.setZ((seg.p0.z + seg.p1.z) / 2.0);
		}
	}
}